package org.openstack.atlas.usage.thread.service;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPoolExecutorService {
 
    public ScheduledThreadPoolExecutor createNewThreadPool();

    public ScheduledThreadPoolExecutor createNewThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, int queryCapacity, int poolDelay, RejectedExecutionHandler rejectedExecutionHandler);
 
    public int getCorePoolSize();
 
    public void setCorePoolSize(int corePoolSize);
 
    public int getMaxPoolSize();
 
    public void setMaxPoolSize(int maximumPoolSize);
 
    public long getKeepAliveTime();
 
    public void setKeepAliveTime(long keepAliveTime);
 
    public int getQueueCapacity();
 
    public void setQueueCapacity(int queueCapacity);

    public long getPoolDelay();

    public void setPoolDelay(long poolDelay);

    public RejectedExecutionHandler getRejectedExecutionHandler();
 
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler);

}