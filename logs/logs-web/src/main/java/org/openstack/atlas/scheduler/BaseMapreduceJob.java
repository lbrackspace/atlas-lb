package org.openstack.atlas.scheduler;

import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.constants.Constants;
import org.openstack.atlas.data.LogDateFormat;
import org.openstack.atlas.service.domain.logs.repository.StateRepository;
import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.util.DateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public abstract class BaseMapreduceJob extends QuartzJobBean {

    private static final Log LOG = LogFactory.getLog(BaseMapreduceJob.class);

    protected LogDateFormat format;

    protected JobScheduler jobScheduler;

    protected StateRepository stateDao;

    private org.openstack.atlas.cfg.Configuration configuration;

    /*private RawLogsService logService;*/

    //private XmlRpcJobClient jobClient;
    //protected UserTransactionImp atomikosUserTransaction;
//    /protected JtaTransactionManager txManager;
    //private UserTransactionImp atomikosTransactionManager;

    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.configuration = conf;
    }

    public void setJobScheduler(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    public void setLogDateFormat(LogDateFormat logDateFormat) {
        this.format = logDateFormat;
    }

    /*public void setRawLogsService(RawLogsService rawLogsService) {
        this.logService = rawLogsService;
    }
*/
    public void setStateDao(StateRepository stateDao) {
        this.stateDao = stateDao;
    }

    protected String getFileName() {
        return configuration.getString(LbLogsConfigurationKeys.rawlogs_part);
    }

    /*protected String getLogString(String logFormat) {
        return logService.getLogName(logFormat);
    }

    protected String getPathForLog(WebRecord record) {
        return logService.getPath(record);
    }*/

    /*protected List<Text> getRawLogsData(Text key, MapFile.Reader[] readers,
                                        Partitioner<Text, Text> partitioner) throws IOException {
        return logService.getValuesForKey(key, readers, partitioner);
    }*/

    protected String getRuntime(JobExecutionContext context) {
        if (context.getJobDetail().getJobDataMap().containsKey(Constants.BaseMapreduceJob.FORMATTED_RUNTIME)) {
            return context.getJobDetail().getJobDataMap().getString(
                    Constants.BaseMapreduceJob.FORMATTED_RUNTIME);
        } else if (context.getJobDetail().getJobDataMap().containsKey(Constants.BaseMapreduceJob.RUNTIME)) {
            return format.format(new DateTime(context.getJobDetail().getJobDataMap().getString(
                    Constants.BaseMapreduceJob.RUNTIME))
                    .getCalendar().getTime());
        } else {
            return format.format(context.getScheduledFireTime());
        }
    }

    protected String createRuntime(JobExecutionContext context) {
        return format.format(context.getScheduledFireTime());
    }

    protected HadoopRunner getRunner(JobExecutionContext context) {
        HadoopRunner runner = HadoopRunner.createRunnerFromValues(context.getJobDetail().getJobDataMap());
        if (runner.getInputString() == null) {
            runner.setInputString(getRuntime(context));
        }

        return runner;
    }

    /*protected void moveFile(WebRecord record, String newLogName) {
        logService.moveFile(record, newLogName);
    }
*/
   /* public void setXmlRpcJobClient(XmlRpcJobClient jobClient) {
        this.jobClient = jobClient;
    }*/

    protected JobScheduler createSchedulerInstance(JobExecutionContext context) {
        JobScheduler scheduler = new JobScheduler();
        scheduler.setSchedulerFactoryBean(context.getScheduler());
        //scheduler.setXmlRpcJobClient(jobClient);
        return scheduler;
    }

    /*public void setAtomikosUserTransaction(UserTransactionImp atomikosUserTransaction) {
        this.atomikosUserTransaction = atomikosUserTransaction;
    }

    public void setAtomikosTransactionManager(UserTransactionImp atomikosTransactionManager) {
        this.atomikosTransactionManager = atomikosTransactionManager;
    }
*/
    /*public void setTransactionManager(JtaTransactionManager txManager) {
        this.txManager = txManager;
    }*/

    protected long startTimer() {
        return System.currentTimeMillis();
    }

    protected String timeTaken(long start) {
        return (System.currentTimeMillis() - start) + " ms";
    }
}
