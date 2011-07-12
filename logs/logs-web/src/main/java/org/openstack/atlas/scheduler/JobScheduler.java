package org.openstack.atlas.scheduler;

import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.tools.HadoopRunner;
import org.quartz.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

public class JobScheduler {
    private static SimpleDateFormat format = new SimpleDateFormat("HHmmss");

    private Scheduler stdScheduler;

    //private XmlRpcJobClient xmlRpcJobClient;

    private Random random = new Random();

    public JobScheduler() {
    }

    public JobScheduler(Scheduler scheduler) {
        this.stdScheduler = scheduler;
    }

    public void scheduleJob(String uniqueJobName, Class jobClass, Map data) throws SchedulingException {
        JobDetail statsJobDetail = new JobDetail(uniqueJobName, Scheduler.DEFAULT_GROUP, jobClass);
        statsJobDetail.setDurability(true);
        statsJobDetail.setVolatility(true);

        statsJobDetail.getJobDataMap().putAll(data);
        Trigger moveTrigger = TriggerUtils.makeImmediateTrigger(0, 0);
        moveTrigger.setName(uniqueJobName);
        moveTrigger.setVolatility(true);

        try {
            stdScheduler.scheduleJob(statsJobDetail, moveTrigger);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    public void scheduleJob(String uniqueJobName, Class jobClass, HadoopRunner runner) throws SchedulingException {
        scheduleJob(uniqueJobName, jobClass, runner.createMapOutputOfValues());
    }

    public void scheduleJob(Class jobClass, HadoopRunner runner) throws SchedulingException {
        //the formatting of now() time will allow for uniqueness in the Quartz DB.


        scheduleJob(createJobName(jobClass, runner), jobClass, runner.createMapOutputOfValues());
    }

    /*public void scheduleRemoteJob(String uniqueJobName, Class jobClass, Map data) throws XmlRpcException {
        xmlRpcJobClient.scheduleRemoteJob(uniqueJobName, jobClass, data);
    }

    public void scheduleRemoteJob(Class jobClass, HadoopRunner runner) throws XmlRpcException {
        xmlRpcJobClient.scheduleRemoteJob(createJobName(jobClass, runner), jobClass, runner.createMapOutputOfValues());
    }

    public void scheduleRemoteJob(String uniqueJobName, Class jobClass, HadoopRunner runner) throws XmlRpcException {
        xmlRpcJobClient.scheduleRemoteJob(uniqueJobName, jobClass, runner.createMapOutputOfValues());
    }

    public void setXmlRpcJobClient(XmlRpcJobClient xmlRpcJobClient) {
        this.xmlRpcJobClient = xmlRpcJobClient;
    }*/

    public void setSchedulerFactoryBean(Scheduler schedulerFactoryBean) {
        this.stdScheduler = schedulerFactoryBean;
    }

    private String createJobName(Class jobClass, HadoopRunner runner) {
        String shrunkName = jobClass.getName();
        String totalName = random.nextLong() + "-" + shrunkName.substring(shrunkName.getClass().getPackage().getName().length() + 1)
                + runner.getInputString() + "_"
                + format.format(Calendar.getInstance().getTime());
        if (totalName.length() > 78) {
            totalName = totalName.substring(0, 78);
        }
        return totalName;
    }
}
