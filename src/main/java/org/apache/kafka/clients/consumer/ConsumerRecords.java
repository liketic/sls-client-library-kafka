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
import org.apache.kafka.common.utils.AbstractIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A container that holds the list {@link ConsumerRecord} per partition for a
 * particular topic. There is one {@link ConsumerRecord} list for every topic
 * partition returned by a {@link Consumer#poll(java.time.Duration)} operation.
 */
public class ConsumerRecords<V> implements Iterable<ConsumerRecord<V>> {

    @SuppressWarnings("unchecked")
    public static final ConsumerRecords<Object> EMPTY = new ConsumerRecords<>(Collections.EMPTY_MAP);

    private final Map<TopicPartition, List<ConsumerRecord<V>>> records;

    public ConsumerRecords(Map<TopicPartition, List<ConsumerRecord<V>>> records) {
        this.records = records;
    }

    /**
     * Get just the records for the given partition
     *
     * @param partition The partition to get records for
     */
    public List<ConsumerRecord<V>> records(TopicPartition partition) {
        List<ConsumerRecord<V>> recs = this.records.get(partition);
        if (recs == null)
            return Collections.emptyList();
        else
            return Collections.unmodifiableList(recs);
    }

    /**
     * Get just the records for the given topic
     */
    public Iterable<ConsumerRecord<V>> records(String topic) {
        if (topic == null)
            throw new IllegalArgumentException("Topic must be non-null.");
        List<List<ConsumerRecord<V>>> recs = new ArrayList<>();
        for (Map.Entry<TopicPartition, List<ConsumerRecord<V>>> entry : records.entrySet()) {
            if (entry.getKey().topic().equals(topic))
                recs.add(entry.getValue());
        }
        return new ConcatenatedIterable<>(recs);
    }

    /**
     * Get the partitions which have records contained in this record set.
     * @return the set of partitions with data in this record set (may be empty if no data was returned)
     */
    public Set<TopicPartition> partitions() {
        return Collections.unmodifiableSet(records.keySet());
    }

    @Override
    public Iterator<ConsumerRecord<V>> iterator() {
        return new ConcatenatedIterable<>(records.values()).iterator();
    }

    /**
     * The number of records for all topics
     */
    public int count() {
        int count = 0;
        for (List<ConsumerRecord<V>> recs: this.records.values())
            count += recs.size();
        return count;
    }

    private static class ConcatenatedIterable<V> implements Iterable<ConsumerRecord<V>> {

        private final Iterable<? extends Iterable<ConsumerRecord<V>>> iterables;

        public ConcatenatedIterable(Iterable<? extends Iterable<ConsumerRecord<V>>> iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<ConsumerRecord<V>> iterator() {
            return new AbstractIterator<ConsumerRecord<V>>() {
                Iterator<? extends Iterable<ConsumerRecord<V>>> iters = iterables.iterator();
                Iterator<ConsumerRecord<V>> current;

                public ConsumerRecord<V> makeNext() {
                    while (current == null || !current.hasNext()) {
                        if (iters.hasNext())
                            current = iters.next().iterator();
                        else
                            return allDone();
                    }
                    return current.next();
                }
            };
        }
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <V> ConsumerRecords<V> empty() {
        return (ConsumerRecords<V>) EMPTY;
    }

}