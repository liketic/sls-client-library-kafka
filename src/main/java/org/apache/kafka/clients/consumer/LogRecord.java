package org.apache.kafka.clients.consumer;

import java.util.List;

public class LogRecord {

    private String topic;
    private String source;
    private int timestamp;
    private List<LogField> fields;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public List<LogField> getFields() {
        return fields;
    }

    public void setFields(List<LogField> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "topic='" + topic + '\'' +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                ", fields=" + fields +
                '}';
    }
}
