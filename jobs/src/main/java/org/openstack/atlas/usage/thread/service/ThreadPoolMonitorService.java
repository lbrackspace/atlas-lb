package org.openstack.atlas.usage.thread.service;


import java.util.concurrent.ThreadPoolExecutor;

public interface ThreadPoolMonitorService extends Runnable {

    public void monitorThreadPool();

    public ThreadPoolExecutor getExecutor();

    public void setExecutor(ThreadPoolExecutor executor);

    public void shutDown();

}