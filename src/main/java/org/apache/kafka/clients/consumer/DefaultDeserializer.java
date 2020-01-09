package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.log.common.FastLog;
import com.aliyun.openservices.log.common.FastLogContent;
import com.aliyun.openservices.log.common.FastLogGroup;

import java.util.ArrayList;
import java.util.List;

public class DefaultDeserializer implements Deserializer<LogRecord> {

    @Override
    public LogRecord deserialize(FastLogGroup logGroup, FastLog fastLog) {
        LogRecord record = new LogRecord();
        record.setSource(logGroup.getSource());
        record.setTopic(logGroup.getTopic());
        record.setTimestamp(fastLog.getTime());
        List<LogField> fields = new ArrayList<>(fastLog.getContentsCount());
        int n = fastLog.getContentsCount();
        for (int i = 0; i < n; i++) {
            FastLogContent f = fastLog.getContents(i);
            fields.add(new LogField(f.getKey(), f.getValue()));
        }
        record.setFields(fields);
        return record;
    }
}
