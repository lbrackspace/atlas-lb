package org.openstack.atlas.usage.thread.util;

import org.apache.commons.logging.Log;
import org.openstack.atlas.atomhopper.exception.AtomHopperUSLJobExecutionException;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.usage.thread.service.RejectedExecutionHandler;
import org.openstack.atlas.usage.thread.service.ThreadPoolExecutorService;
import org.openstack.atlas.usage.thread.service.ThreadPoolMonitorService;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.logging.LogFactory.getLog;

public class ThreadServiceUtil {
    private static final Log LOG = getLog(ThreadServiceUtil.class);

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
            LOG.error("There was an error shutting down threadPool: " + AtomHopperUtil.getStackTrace(e));
            throw new AtomHopperUSLJobExecutionException("There was an error destroying thread monitors and task executors: " + e);
        }

        LOG.debug("Destroying the AHUSL Client");
        atomHopperClient.destroy();
        return true;
    }
}
