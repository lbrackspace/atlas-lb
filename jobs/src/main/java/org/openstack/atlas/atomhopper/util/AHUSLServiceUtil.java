package org.openstack.atlas.atomhopper.util;

import org.apache.commons.logging.Log;
import org.openstack.atlas.atomhopper.exception.AtomHopperUSLJobExecutionException;
import org.openstack.atlas.usage.thread.service.RejectedExecutionHandler;
import org.openstack.atlas.usage.thread.service.ThreadPoolExecutorService;
import org.openstack.atlas.restclients.atomhopper.AtomHopperClient;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import org.openstack.atlas.usage.thread.service.ThreadPoolMonitorService;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
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
            LOG.error("There was an error shutting down threadPool: " + AtomHopperUtil.getStackTrace(e));
            throw new AtomHopperUSLJobExecutionException("There was an error destroying thread monitors and task executors: " + e);
        }

        LOG.debug("Destroying the AHUSL Client");
        atomHopperClient.destroy();
        return true;
    }
    /**
     * This method returns XMLGregorinanCalendar based on milliseconds
     *
     * @param calendar
     * @return
     * @throws javax.xml.datatype.DatatypeConfigurationException
     */
    public static XMLGregorianCalendar processCalendar(Calendar calendar) throws DatatypeConfigurationException {
        //TODO: find a better way to transform.............

        Calendar retcal = convertCalendar(calendar, TimeZone.getTimeZone("UTC"));

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(retcal.getTimeInMillis());
        XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        xgc.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
        xgc.setTimezone(0);
//        System.out.println("XMLGREGORIAN:: " + xgc);
        return xgc;
    }

    public static Calendar convertCalendar(final Calendar calendar, final TimeZone timeZone) {
        Calendar ret = new GregorianCalendar(timeZone);
        ret.setTimeInMillis(calendar.getTimeInMillis() +
                timeZone.getOffset(calendar.getTimeInMillis()) -
                TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
        ret.getTime();
        return ret;
    }

}
