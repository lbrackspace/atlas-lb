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
        moveTrigger.setJobName(uniqueJobName);
        moveTrigger.setName(uniqueJobName);
        moveTrigger.setVolatility(true);

        //RESCHEDULE IF THE TRIGGER EXIST
        try {
//            if (stdScheduler.getTriggerState(jobClass.toString(), Scheduler.DEFAULT_GROUP) == STATE_NONE) {
//                stdScheduler.rescheduleJob(uniqueJobName, Scheduler.DEFAULT_GROUP, moveTrigger);
//            } else {
                stdScheduler.scheduleJob(statsJobDetail, moveTrigger);
//            }
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    public void scheduleJob(String uniqueJobName, Class jobClass, HadoopRunner runner) throws SchedulingException {
        scheduleJob(uniqueJobName, jobClass, runner.createMapOutputOfValues());
    }

    public void scheduleJob(Class jobClass, HadoopRunner runner) throws SchedulingException {
        scheduleJob(createJobName(jobClass, runner), jobClass, runner.createMapOutputOfValues());
    }

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
