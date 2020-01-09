/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.TopicPartition;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public interface Consumer<V> extends Closeable {

    Set<TopicPartition> assignment();

    Set<String> subscription();

    void subscribe(Collection<String> topics);

    void subscribe(Collection<String> topics, ConsumerRebalanceListener callback);

    void assign(Collection<TopicPartition> partitions);

    void subscribe(Pattern pattern, ConsumerRebalanceListener callback);

    void subscribe(Pattern pattern);

    void unsubscribe();

    ConsumerRecords<V> poll(long timeout);

//    ConsumerRecords<K, V> poll(Duration timeout);


    void commitSync();


    void commitSync(Duration timeout);


    void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets);

//    void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets, final Duration timeout);

    void commitAsync();


    void commitAsync(OffsetCommitCallback callback);


    void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback);


    void seek(TopicPartition partition, long offset);

    void seek(TopicPartition partition, OffsetAndMetadata offsetAndMetadata);

    void seekToBeginning(Collection<TopicPartition> partitions);


    void seekToEnd(Collection<TopicPartition> partitions);


    long position(TopicPartition partition);


//    long position(TopicPartition partition, final Duration timeout);

    @Deprecated
    OffsetAndMetadata committed(TopicPartition partition);

//    @Deprecated
//    OffsetAndMetadata committed(TopicPartition partition, final Duration timeout);

    Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions);


//    Map<TopicPartition, OffsetAndMetadata> committed(Set<TopicPartition> partitions, final Duration timeout);

//    /**
//     * @see KafkaConsumer#metrics()
//     */
//    Map<MetricName, ? extends Metric> metrics();
//
//    /**
//     * @see KafkaConsumer#partitionsFor(String)
//     */
//    List<PartitionInfo> partitionsFor(String topic);
//
//    /**
//     * @see KafkaConsumer#partitionsFor(String, Duration)
//     */
//    List<PartitionInfo> partitionsFor(String topic, Duration timeout);
//
//    /**
//     * @see KafkaConsumer#listTopics()
//     */
//    Map<String, List<PartitionInfo>> listTopics();
//
//    /**
//     * @see KafkaConsumer#listTopics(Duration)
//     */
//    Map<String, List<PartitionInfo>> listTopics(Duration timeout);
//
//    Set<TopicPartition> paused();
//
//    void pause(Collection<TopicPartition> partitions);
//
//
//    void resume(Collection<TopicPartition> partitions);

//    /**
//     * @see KafkaConsumer#offsetsForTimes(Map)
//     */
//    Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch);
//
//    /**
//     * @see KafkaConsumer#offsetsForTimes(Map, Duration)
//     */
//    Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch, Duration timeout);


    Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions);


//    Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions, Duration timeout);


    Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions);


//    Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions, Duration timeout);

    void close();

//    @Deprecated
//    void close(long timeout, TimeUnit unit);
//    void close(Duration timeout);
//    void wakeup();
}
