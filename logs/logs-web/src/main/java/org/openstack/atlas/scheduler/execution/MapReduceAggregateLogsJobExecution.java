package org.openstack.atlas.scheduler.execution;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.logs.hadoop.jobs.HadoopJob;
import org.openstack.atlas.logs.hadoop.jobs.HadoopLogSplitterJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.scheduler.SplitLoadBalancerLogsJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;

import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.logs.hadoop.util.DeleteDirectoryResponse;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.debug.Debug;
import org.springframework.beans.factory.annotation.Required;

public class MapReduceAggregateLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(MapReduceAggregateLogsJobExecution.class);
    private static final VerboseLogger vlog = new VerboseLogger(MapReduceAggregateLogsJobExecution.class);

    private static void logDeleteResults(List<DeleteDirectoryResponse> deletedDirs, String goodFmt, String badFmt) {
        String excMsg;
        String msg;
        for (DeleteDirectoryResponse resp : deletedDirs) {
            if (resp.getException() != null) {
                excMsg = Debug.getExtendedStackTrace(resp.getException());
                msg = String.format(badFmt, resp.getDirectory(), excMsg);
                LOG.error(msg, resp.getException());
            } else {
                vlog.printf(goodFmt, resp.getDirectory());
            }
        }
    }

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {
        JobState state = createJob(JobName.MAPREDUCE, schedulerConfigs.getInputString());
        List<DeleteDirectoryResponse> deletedDirs;
        int hadoopErrorCode = -1;
        String excMsg;
        try {
            deletedDirs = hdfsUtils.rmLZOsOlderThan(HadoopLogsConfigs.getDaysOfLZOsToKeep());
            logDeleteResults(deletedDirs, "deleted %s", "Error in attempt to delete HDFS old LZO %s: %s\n");
            deletedDirs = hdfsUtils.rmZipsOlderThan(HadoopLogsConfigs.getDaysOfZipsToKeep());
            logDeleteResults(deletedDirs, "deleted %s", "Error in attempt to delete HDFS old ZIPs %s: %s\n");
        } catch (Exception ex) {
            excMsg = Debug.getEST(ex);
            LOG.error(String.format("Error trying to delete old HDFS files:%s\n", excMsg), ex);
        }

        try {

            String dstJarPath = HadoopLogsConfigs.getHdfsJobsJarPath();

            // fileHour=2013021517
            String fileHour = schedulerConfigs.getInputString();

            // inDir = /user/lbass_prod/input/logs/2013021517
            List<String> inDirComps = new ArrayList<String>();
            inDirComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
            inDirComps.add(fileHour);
            String inDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(inDirComps));

            // outDir = /user/lbass_prod/output/logs/lb_logs_split/2013021517
            List<String> outDirComps = new ArrayList<String>();
            outDirComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
            outDirComps.add("lb_logs_split");
            outDirComps.add(fileHour);
            String outDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(outDirComps));

            // histDir = /user/lbaas_prod/output/logs/lb_logs_split/_logs/2013021517
            List<String> histDirComps = new ArrayList<String>();
            histDirComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
            histDirComps.add("lb_logs_split");
            histDirComps.add("_logs");
            histDirComps.add(fileHour);
            String histDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(histDirComps));

            String numReducers = HadoopLogsConfigs.getNumReducers();

            String userName = HadoopLogsConfigs.getHdfsUserName();

            List<String> lzoFiles = new ArrayList<String>();
            for (Path filePath : hdfsUtils.listPaths(inDir, false)) {
                String fileName = HdfsUtils.pathUriString(filePath);
                if (!fileName.endsWith(".lzo")) {
                    continue;
                }
                lzoFiles.add(fileName);
            }

            if (lzoFiles.isEmpty()) {
                throw new Exception("Can not start hadoop job as there are no input files");
            }

            List<String> argsList = new ArrayList<String>();
            argsList.add(dstJarPath);
            argsList.add(outDir);
            argsList.add(histDir);
            argsList.add(fileHour);
            argsList.add(numReducers);
            argsList.add(userName);

            for (String lzoFileName : lzoFiles) {
                argsList.add(lzoFileName);
            }
            HadoopJob hadoopClient = new HadoopLogSplitterJob();
            hadoopClient.setConfiguration(HadoopLogsConfigs.getHadoopConfiguration());
            hadoopErrorCode = hadoopClient.run(argsList);  // Actually runs the Hadoop Job
            if (hadoopErrorCode < 0) {
                LOG.error(String.format("Hadoop run FAILED with error code %d", hadoopErrorCode));
            } else {
                vlog.log(String.format("Hadoop run SUCCEEDED with code %d", hadoopErrorCode));
            }
            // Note that the SplitLoadBalancerLogsJob being called below is the quartz job that reads the zip files from HDFS.
            // This does not call hadoop job. The hadoop job was actually ran above via the hadoopClient.run(argsList)
            scheduler.scheduleJob(SplitLoadBalancerLogsJob.class, schedulerConfigs);
        } catch (Exception e) {
            LOG.error(e);
            failJob(state);
            throw new ExecutionException(e);
        }
        if (hadoopErrorCode < 0) {
            failJob(state);
        } else {
            finishJob(state);
        }

    }
}
