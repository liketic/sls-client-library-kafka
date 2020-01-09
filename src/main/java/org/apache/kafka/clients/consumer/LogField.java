package org.apache.kafka.clients.consumer;

public class LogField {
    private String key;
    private String value;

    public LogField(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LogField{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
