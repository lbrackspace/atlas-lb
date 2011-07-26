package org.openstack.atlas.scheduler.execution;

import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.io.FileBytesWritable;
import org.openstack.atlas.mapreduce.LbStatsTool;
import org.openstack.atlas.scheduler.ArchiveLoadBalancerLogsJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.tools.HadoopRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.io.IOException;


public class SplitLoadBalancerLogsJobExecution extends LoggableJobExecution implements QuartzExecutable {
    private static final Log LOG = LogFactory.getLog(SplitLoadBalancerLogsJobExecution.class);

     private DirectoryTool tool;

     public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {

         JobState state = createJob(JobName.FILES_SPLIT, runner.getInputString());

         try {

             tool = createTool(LbStatsTool.class);
             tool.setupHadoopRun(runner);
             String directory = tool.getOutputDirectory();

             FileStatus[] files = new FileStatus[0];

             files = utils.ls(tool.getConfiguration().getJobConf(), tool.getOutputDirectory());
             LOG.info("No of files in " + directory + " is: " + files.length);

             for (int i = 0; i < files.length; i++) {
                 if (files[i].getPath().getName().startsWith("part-")) {
                     Path local = utils.moveLocal(tool.getConfiguration().getJobConf(), files[i].getPath());
                     SequenceFile.Reader reader = utils.getLocalReader(tool.getConfiguration().getJobConf(), local);

                     Text key = new Text();
                     int seqCount = 0;
                     String currentKey = null;

                     while (reader.next(key)) {
                         if (key.toString().equals(currentKey)) {
                             seqCount++;
                         } else {
                             currentKey = key.toString();
                             seqCount = 0;
                         }

                         String accountId = getAccount(key.toString());
                         String loadbalancerId = getLoadBalancerId(key.toString());

                         String cacheLocation = utils.getCacheDir() + "/" + runner.getInputString() + "/" + accountId;
                         new File(cacheLocation).mkdirs();
                         String filename = getFileName(loadbalancerId, runner.getRawlogsFileDate());

                         String cacheLocationAndFile = cacheLocation + "/" + filename;

                         File file = new File(cacheLocationAndFile);
                         if(file.exists()) {
                             LOG.warn("A file with name " + cacheLocationAndFile + " already exists. This can lead to missing log files as the old files can be overwritten.");
                             cacheLocationAndFile = cacheLocationAndFile + "_" + i;
                         }

                         cacheLocationAndFile = cacheLocationAndFile + ".zip";

                         FileBytesWritable val = new FileBytesWritable();
                         val.setOrder(seqCount);
                         val.setFileName(cacheLocationAndFile);
                         reader.getCurrentValue(val);
                         LOG.info("File Written to: " + cacheLocationAndFile);
                     }

                     reader.close();
                     utils.deleteLocalFile(local);

                 }

             }

             scheduleArchiveLoadBalancerLogsJob(scheduler, runner);
         } catch (IOException e) {
             e.printStackTrace();
             failJob(state);
             throw new ExecutionException(e);
         } catch (Exception e) {
             e.printStackTrace();
             failJob(state);
             throw new ExecutionException(e);
         }

         finishJob(state);
     }

     private String getFileName(String lbId, String rawlogsFileDate) {
         StringBuilder sb = new StringBuilder();
         sb.append("access log ");
         sb.append(lbId).append(" ");
         sb.append(rawlogsFileDate);
         return getFormattedName(sb.toString());
     }

     private String getFormattedName(String name) {
         return name.replaceAll(" ", "_");

     }

     private String getAccount(String key) {
         return key.split(":")[0];
     }

     private String getLoadBalancerId(String key) {
         return key.split(":")[1];
     }

     private void scheduleArchiveLoadBalancerLogsJob(JobScheduler scheduler, HadoopRunner runner) throws SchedulingException {
         scheduler.scheduleJob(ArchiveLoadBalancerLogsJob.class, runner);
     }

}