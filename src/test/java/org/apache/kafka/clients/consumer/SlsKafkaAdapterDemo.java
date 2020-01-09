package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SlsKafkaAdapterDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlsKafkaAdapterDemo.class);

    public static void main(String[] args) {
        String akid = "YOUR_AK_ID";
        String akKey = "YOUR_AK_KEY";
        String project = "YOUR_PROJECT";
        String logstore = "YOUR_LOGSTORE";
        // 消费组ID，用于记录offset和进行group rebalance
        String groupId = "YOUR_CONSUMER_GROUP_ID";
        String endpoint = "YOUR_ENDPOINT";

        Properties props = new Properties();
        props.setProperty(SlsConfig.ACCESS_KEY_ID_CONFIG, akid);
        props.setProperty(SlsConfig.ACCESS_KEY_CONFIG, akKey);
        props.setProperty(SlsConfig.PROJECT_CONFIG, project);
        props.setProperty(SlsConfig.ENDPOINT_CONFIG, endpoint);
        props.setProperty(SlsConfig.DESERIALIZER_CLASS_CONFIG, DefaultDeserializer.class.getName());
//        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, "myconsumer");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name());

        Consumer<LogRecord> consumer = new SlsKafkaConsumer<>(props);
        // 启动消费者
        consumer.subscribe(Collections.singleton(logstore));

        while (true) {
            try {
                // 默认格式是LogItem，也可以支持JSON,String
                ConsumerRecords<LogRecord> records = consumer.poll(Long.MAX_VALUE);
                System.out.println(consumer.assignment());
                System.out.println(consumer.subscription());
                if (!records.isEmpty()) {
                    for (TopicPartition partition : records.partitions()) {
                        List<ConsumerRecord<LogRecord>> partitionRecords = records.records(partition);
                        for (ConsumerRecord<LogRecord> record : partitionRecords) {
                            System.out.println(record.value());
                            LOGGER.info("Value [{}], Partition [{}], Offset [{}], Key [{}]",
                                    record.value(), record.partition(), record.offset(), record.topic());
                        }
                    }
                    // 异步提交，也可以同步提交
                    consumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                            if (e == null) {
                                LOGGER.debug("Success to commit offset [{}]", map);
                            } else {
                                LOGGER.error("Failed to commit offset [{}]", e.getMessage(), e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage(), e);
            }
        }
    }
}
