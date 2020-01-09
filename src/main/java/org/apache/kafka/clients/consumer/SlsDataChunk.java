package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.log.common.LogGroupData;

import java.util.List;

public class SlsDataChunk {

    private int shard;
    private String cursor;
    private List<LogGroupData> data;
    private String logstore;

    public SlsDataChunk(int shard, String cursor, List<LogGroupData> data) {
        this.shard = shard;
        this.cursor = cursor;
        this.data = data;
    }

    public int getShard() {
        return shard;
    }

    public void setShard(int shard) {
        this.shard = shard;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public List<LogGroupData> getData() {
        return data;
    }

    public void setData(List<LogGroupData> data) {
        this.data = data;
    }

    public String getLogstore() {
        return logstore;
    }

    public void setLogstore(String logstore) {
        this.logstore = logstore;
    }
}
