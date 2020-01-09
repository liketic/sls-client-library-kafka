package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.log.common.FastLog;
import com.aliyun.openservices.log.common.FastLogGroup;
import com.aliyun.openservices.log.common.LogGroupData;

public interface Deserializer<T> {


    T deserialize(FastLogGroup logGroup, FastLog fastLog);
}
