package org.openstack.atlas.usage.thread.service.serviceImpl;

import org.apache.log4j.Logger;
import org.openstack.atlas.usage.thread.service.ThreadPoolMonitorService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ThreadPoolMonitorServiceImpl implements ThreadPoolMonitorService {
    private final Logger log = Logger.getLogger(ThreadPoolMonitorServiceImpl.class);
    
    private ThreadPoolExecutor executor;
    private long monitoringPeriod = 5;
    private volatile boolean done = false;

    @Override
    public void run() {
        try {
            while (!done) {
                monitorThreadPool();
                Thread.sleep(monitoringPeriod * 1000);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void monitorThreadPool() {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("CurrentPoolSize : ").append(executor.getPoolSize());
        strBuff.append(" - CorePoolSize : ").append(executor.getCorePoolSize());
        strBuff.append(" - MaximumPoolSize : ").append(executor.getMaximumPoolSize());
        strBuff.append(" - ActiveTaskCount : ").append(executor.getActiveCount());
        strBuff.append(" - CompletedTaskCount : ").append(executor.getCompletedTaskCount());
        strBuff.append(" - TotalTaskCount : ").append(executor.getTaskCount());
        strBuff.append(" - isTerminated : ").append(executor.isTerminated());

        log.debug(strBuff.toString());
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void shutDown() {
        done = true;
    }
}