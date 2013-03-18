package org.openstack.atlas.atom.util;

import org.apache.commons.logging.Log;
import org.openstack.atlas.atom.client.AtomHopperClient;
import org.openstack.atlas.atom.exception.AtomHopperUSLJobExecutionException;
import org.openstack.atlas.atom.handler.RejectedExecutionHandler;
import org.openstack.atlas.atom.service.ThreadPoolExecutorService;
import org.openstack.atlas.atom.service.ThreadPoolMonitorService;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.logging.LogFactory.getLog;

public class AHUSLServiceUtil {
    private static final Log LOG = getLog(AHUSLServiceUtil.class);
    private static final int MONITOR_TIMEOUT = 300;
    private static final int QUERY_CAPACITY = 1000;

    public static ThreadPoolMonitorService startThreadMonitor(ThreadPoolExecutor taskExecutor, ThreadPoolMonitorService threadPoolMonitorService) {
        Thread monitor = null;
        try {
            threadPoolMonitorService.setExecutor(taskExecutor);
            monitor = new Thread(threadPoolMonitorService);
            monitor.start();
        } catch (Exception e) {
            LOG.error("There was an error initiating thread monitors and task executors: " + e);
            throw new AtomHopperUSLJobExecutionException("There was an error initiating thread monitors and task executors: " + e);

        }
        return threadPoolMonitorService;
    }

    public static ThreadPoolExecutor startThreadExecutor(ThreadPoolExecutor taskExecutor, ThreadPoolExecutorService threadPoolExecutorService, int corePoolSize, int maxPoolSize, long keepAliveTime) {
        try {
            LOG.debug("Setting up the threadPoolExecutor with " + maxPoolSize + " pools");
            taskExecutor = threadPoolExecutorService.createNewThreadPool(corePoolSize, maxPoolSize, keepAliveTime, QUERY_CAPACITY, new RejectedExecutionHandler());
        } catch (Exception e) {
            LOG.error("There was an error initiating thread monitors and task executors: " + e);
            throw new AtomHopperUSLJobExecutionException("There was an error initiating thread monitors and task executors: " + e);

        }
        return taskExecutor;
    }

    public static boolean shutDownAHUSLServices(ThreadPoolExecutor taskExecutor, ThreadPoolMonitorService threadPoolMonitorService, AtomHopperClient atomHopperClient) {
        try {
            LOG.debug("Shutting down the thread pool and monitors..");
            taskExecutor.shutdown();
            taskExecutor.awaitTermination(300, TimeUnit.SECONDS);
            threadPoolMonitorService.shutDown();
        } catch (InterruptedException e) {
            LOG.error("There was an error shutting down threadPool: " + AHUSLUtil.getStackTrace(e));
            throw new AtomHopperUSLJobExecutionException("There was an error destroying thread monitors and task executors: " + e);
        }

        LOG.debug("Destroying the AHUSL Client");
        atomHopperClient.destroy();
        return true;
    }
}
