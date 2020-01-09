package org.apache.kafka.clients.consumer;

import com.aliyun.openservices.loghub.client.interfaces.ILogHubProcessor;
import com.aliyun.openservices.loghub.client.interfaces.ILogHubProcessorFactory;

import java.util.concurrent.BlockingQueue;


public class LogHubProcessorTestFactory implements ILogHubProcessorFactory {

    private BlockingQueue<SlsDataChunk> dataQueue;
    private String logstore;

    public LogHubProcessorTestFactory(BlockingQueue<SlsDataChunk> dataQueue,
                                      String logstore) {
        this.dataQueue = dataQueue;
        this.logstore = logstore;
    }

    @Override
    public ILogHubProcessor generatorProcessor() {
        return new LogHubTestProcessor(dataQueue, logstore);
    }

}
