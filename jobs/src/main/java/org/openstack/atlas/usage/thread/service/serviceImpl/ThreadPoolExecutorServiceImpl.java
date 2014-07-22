package org.openstack.atlas.usage.thread.service.serviceImpl;

import org.openstack.atlas.usage.thread.service.RejectedExecutionHandler;
import org.openstack.atlas.usage.thread.service.ThreadPoolExecutorService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ThreadPoolExecutorServiceImpl implements ThreadPoolExecutorService {

    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private int queueCapacity;
    private long poolDelay;
    RejectedExecutionHandler rejectedExecutionHandler;


    public ScheduledThreadPoolExecutor createNewThreadPool() {
        return new ScheduledThreadPoolExecutor(corePoolSize, rejectedExecutionHandler);
    }

    public ScheduledThreadPoolExecutor createNewThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, int queryCapacity, int poolDelay, RejectedExecutionHandler rejectedExecutionHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queryCapacity;
        this.poolDelay = poolDelay;
        this.rejectedExecutionHandler = rejectedExecutionHandler;

        return createNewThreadPool();
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public long getPoolDelay() {
        return poolDelay;
    }

    public void setPoolDelay(long poolDelay) {
        this.poolDelay = poolDelay;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }
}