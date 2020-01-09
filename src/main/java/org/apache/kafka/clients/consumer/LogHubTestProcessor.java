package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.log.common.LogGroupData;
import com.aliyun.openservices.loghub.client.ILogHubCheckPointTracker;
import com.aliyun.openservices.loghub.client.exceptions.LogHubCheckPointException;
import com.aliyun.openservices.loghub.client.interfaces.ILogHubProcessor;

import java.util.List;
import java.util.concurrent.BlockingQueue;


public class LogHubTestProcessor implements ILogHubProcessor {
    private int mShard;
    private long mLastCheckTime = 0;

    private BlockingQueue<SlsDataChunk> dataQueue;
    private String logstore;

    public LogHubTestProcessor(BlockingQueue<SlsDataChunk> dataQueue,
                               String logstore) {
        this.dataQueue = dataQueue;
        this.logstore = logstore;
    }

    @Override
    public void initialize(int shardId) {
        System.out.println("initialize shard: " + shardId);
        mShard = shardId;
    }

    @Override
    public String process(List<LogGroupData> logGroups,
                          ILogHubCheckPointTracker checkPointTracker) {
        // TODO
        String cursor = checkPointTracker.getCheckPoint();
        // Fix-sized blocking queue
        dataQueue.add(new SlsDataChunk(mShard, cursor, logGroups));
        return null;
    }

    @Override
    public void shutdown(ILogHubCheckPointTracker checkPointTracker) {

    }


}
