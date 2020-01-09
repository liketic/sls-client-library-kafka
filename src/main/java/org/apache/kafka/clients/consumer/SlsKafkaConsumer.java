package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.FastLog;
import com.aliyun.openservices.log.common.FastLogGroup;
import com.aliyun.openservices.log.common.LogGroupData;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.loghub.client.ClientWorker;
import com.aliyun.openservices.loghub.client.config.LogHubConfig;
import com.aliyun.openservices.loghub.client.exceptions.LogHubClientWorkerException;
import org.apache.commons.codec.binary.Base64;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SlsKafkaConsumer<T> implements Consumer<T> {

    private Properties props;
    private ExecutorService executorService;
    private BlockingQueue<SlsDataChunk> dataQueue;
    private BlockingQueue<InternalOffset> offsets;
    private List<ClientWorker> workers;
    private Client client;
    private Deserializer<T> deserializer;
    private Set<String> subscription;

    public SlsKafkaConsumer(Properties props) {
        this.props = props;
        this.executorService = Executors.newCachedThreadPool();
        this.offsets = new LinkedBlockingQueue<>();
        this.workers = new ArrayList<>();
        // fixme
        this.dataQueue = new LinkedBlockingQueue<>();
        this.subscription = new HashSet<>();
    }

    @Override
    public Set<TopicPartition> assignment() {
        return null;
    }

    @Override
    public Set<String> subscription() {
        return null;
    }

    private LogHubConfig.ConsumePosition parse(OffsetResetStrategy strategy) {
        switch (strategy) {
            case LATEST:
                return LogHubConfig.ConsumePosition.END_CURSOR;
            default:
                return LogHubConfig.ConsumePosition.BEGIN_CURSOR;
        }
    }

    @Override
    public void subscribe(Collection<String> topics) {
        client = new Client(props.getProperty(SlsConfig.ENDPOINT_CONFIG),
                props.getProperty(SlsConfig.ACCESS_KEY_ID_CONFIG),
                props.getProperty(SlsConfig.ACCESS_KEY_CONFIG));
        for (String topic : topics) {
            LogHubConfig logHubConfig = new LogHubConfig(
                    props.getProperty(ConsumerConfig.GROUP_ID_CONFIG),
                    props.getProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG),
                    props.getProperty(SlsConfig.ENDPOINT_CONFIG),
                    props.getProperty(SlsConfig.PROJECT_CONFIG),
                    topic,
                    props.getProperty(SlsConfig.ACCESS_KEY_ID_CONFIG),
                    props.getProperty(SlsConfig.ACCESS_KEY_CONFIG),
                    LogHubConfig.ConsumePosition.BEGIN_CURSOR);
            try {
                ClientWorker worker = new ClientWorker(new LogHubProcessorTestFactory(dataQueue, topic), logHubConfig);
                executorService.submit(worker);
                workers.add(worker);
            } catch (LogHubClientWorkerException e) {
                throw new KafkaException(e);
            }
        }
        subscription.addAll(topics);
    }

    private static class InternalOffset {
        private String topic;
        private int partition;
        private String cursor;

        public InternalOffset(String topic, int partition, String cursor) {
            this.topic = topic;
            this.partition = partition;
            this.cursor = cursor;
        }
    }

    @Override
    public void subscribe(Collection<String> topics, ConsumerRebalanceListener callback) {
        // TODO
    }

    @Override
    public void assign(Collection<TopicPartition> partitions) {
        // TODO
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
    }

    @Override
    public void subscribe(Pattern pattern) {
    }

    @Override
    public void unsubscribe() {
        for (ClientWorker worker : workers) {
            worker.shutdown();
        }
        executorService.shutdown();
    }

    @Override
    public ConsumerRecords<T> poll(long timeout) {
        try {
            SlsDataChunk chunk = dataQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (chunk == null) {
                return ConsumerRecords.empty();
            }
//            long offset = Long.parseLong(new String(Base64.decodeBase64(chunk.getCursor().getBytes())));
            InternalOffset internalOffset = new InternalOffset(chunk.getLogstore(),
                    chunk.getShard(), chunk.getCursor());
            offsets.add(internalOffset);
            if (deserializer == null) {
                Class keyDeserializerClass = DefaultDeserializer.class;
                String className = props.getProperty(SlsConfig.DESERIALIZER_CLASS_CONFIG);
                if (className != null) {
                    try {
                        keyDeserializerClass = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new KafkaException(e);
                    }
                }
                try {
                    this.deserializer = (Deserializer<T>) keyDeserializerClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    throw new KafkaException(e);
                }
            }
            Map<TopicPartition, List<ConsumerRecord<T>>> topicToRecords = new HashMap<>();
            TopicPartition partition = new TopicPartition(
                    chunk.getLogstore(),
                    chunk.getShard()
            );
            List<ConsumerRecord<T>> records = new ArrayList<>();
            for (LogGroupData item : chunk.getData()) {
                FastLogGroup group = item.GetFastLogGroup();
                int n = group.getLogsCount();
                for (int i = 0; i < n; i++) {
                    FastLog log = group.getLogs(i);
                    T v = deserializer.deserialize(group, log);
                    ConsumerRecord<T> record = new ConsumerRecord<>(
                            chunk.getLogstore(),
                            chunk.getShard(),
                            Long.parseLong(new String(Base64.decodeBase64(chunk.getCursor().getBytes()))),
                            log.getTime(),
                            v);
                    records.add(record);
                }
            }
            topicToRecords.put(partition, records);
            return new ConsumerRecords<>(topicToRecords);
        } catch (InterruptedException e) {
            return ConsumerRecords.empty();
        }
    }

    @Override
    public void commitSync() {
        for (InternalOffset offset : offsets) {
            try {
                client.UpdateCheckPoint(
                        props.getProperty(SlsConfig.PROJECT_CONFIG),
                        offset.topic,
                        props.getProperty(ConsumerConfig.GROUP_ID_CONFIG),
                        props.getProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG),
                        offset.partition,
                        offset.cursor
                );
            } catch (LogException ex) {
                throw new KafkaException(ex);
            }
        }
    }

    @Override
    public void commitSync(Duration timeout) {
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
    }

    @Override
    public void commitAsync() {
    }

    @Override
    public void commitAsync(OffsetCommitCallback callback) {
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {

    }

    @Override
    public void seek(TopicPartition partition, long offset) {

    }

    @Override
    public void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata) {

    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> partitions) {

    }

    @Override
    public void seekToEnd(Collection<TopicPartition> partitions) {

    }

    @Override
    public long position(TopicPartition partition) {
        return 0;
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition) {
        return null;
    }

    @Override
    public Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions) {
        return null;
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions) {
        return null;
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions) {
        return null;
    }

    @Override
    public void close() {
    }
}
