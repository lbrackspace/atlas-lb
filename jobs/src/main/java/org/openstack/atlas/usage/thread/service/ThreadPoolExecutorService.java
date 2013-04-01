package org.openstack.atlas.usage.thread.service;


import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPoolExecutorService {
 
    public ThreadPoolExecutor createNewThreadPool();

    public ThreadPoolExecutor createNewThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, int queryCapacity, RejectedExecutionHandler rejectedExecutionHandler);
 
    public int getCorePoolSize();
 
    public void setCorePoolSize(int corePoolSize);
 
    public int getMaxPoolSize();
 
    public void setMaxPoolSize(int maximumPoolSize);
 
    public long getKeepAliveTime();
 
    public void setKeepAliveTime(long keepAliveTime);
 
    public int getQueueCapacity();
 
    public void setQueueCapacity(int queueCapacity);
 
    public RejectedExecutionHandler getRejectedExecutionHandler();
 
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler);

}