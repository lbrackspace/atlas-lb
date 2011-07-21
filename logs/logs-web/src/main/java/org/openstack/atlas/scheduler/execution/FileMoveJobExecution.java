package org.openstack.atlas.scheduler.execution;

import com.hadoop.compression.lzo.LzoIndexer;
import org.openstack.atlas.mapreduce.LbStatsTool;
import org.openstack.atlas.scheduler.ArchiveLoadBalancerLogsJob;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.logs.entities.JobName;
import org.openstack.atlas.service.domain.logs.entities.JobStateVal;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.scheduler.OrderLoadBalancerLogsJob;
import org.openstack.atlas.tools.HadoopConfiguration;
import org.openstack.atlas.tools.HadoopRunner;
import org.openstack.atlas.tools.HadoopTool;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * In charge of moving files given to it onto the dfs, and scheduling jobs for
 * cloud files, stats and CC's. It creates a state for the filemove, and a state
 * for each file move to track all files moved to the dfs.
 *
 */
public class FileMoveJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(FileMoveJobExecution.class);

    protected JobScheduler jobScheduler;

    private HadoopTool hadoopTool;

    public void execute(JobScheduler scheduler, HadoopRunner runner) throws ExecutionException {

        // stupid manual set, this has to be done. a circular dep because of how
        // quartz must init its scheduler factory crap. currently u cannot have
        // a bean that has a dependency on a bean that is in the
        // schedulerFactoryBean#schedulerContextAsMap
        jobScheduler = scheduler;
        String runTime = runner.getInputString();

        JobState state = createJob(JobName.FILECOPY_PARENT, runTime);
        LOG.info("setting filemove run up for " + runTime);

        hadoopTool.setupHadoopRun(runTime);
        List<String> localInputFiles;

        try {
            localInputFiles = getLocalInputFiles(runner);

            // this is done so that any other watchdog runs will not pick up the
            // files. They may take a while before they make it onto the DFS,
            // especially if its a bunch of large files being uploaded.
            Map<String, JobState> fastValues = createStateForMovingFiles(runTime, localInputFiles);

            for (String filename : localInputFiles) {
                if (filename.endsWith(".lzo")) {
                    runner.setLzoInput(true);
                }
            }

            moveFilesOntoDFS(runner, fastValues);
            deleteIfFinished(fastValues);


//            scheduleMoveToCloudFiles(runner);
            scheduleFqdnJob(runner);

        } catch (Exception e) {
            LOG.error(e);
            failJob(state);
            throw new ExecutionException(e);
        }
        finishJob(state);
    }

    @Required
    private void scheduleFqdnJob(HadoopRunner runner) throws SchedulingException {
        jobScheduler.scheduleJob(OrderLoadBalancerLogsJob.class, runner);
    }

    private void scheduleMoveToCloudFiles(HadoopRunner runner)
            throws SchedulingException {
        String jobName;

        if (runner.getFileMoveInput() != null) {
            jobName = runner.getFileMoveInput();

        } else if (runner.getInputForMultiPathJobs() != null) {
            List files = (List) runner.getInputForMultiPathJobs();
            jobName = StringUtils.join(files, ",");

        } else {
            throw new SchedulingException("Could not find files to schedule");
        }

        jobScheduler.scheduleJob(jobName + ":" + runner.getInputString(), ArchiveLoadBalancerLogsJob.class, runner);
    }

    public void setLbStatsTool(LbStatsTool lbStatsTool) {
        this.hadoopTool = lbStatsTool;
    }

    private Map<String, JobState> createStateForMovingFiles(String inputString,
                                                         List<String> localInputFiles) {
        Map<String, JobState> fastValues = new HashMap<String, JobState>();
        for (String inputFile : localInputFiles) {

            JobState state = createJob(JobName.FILECOPY, inputString + ":" + inputFile);
            fastValues.put(inputFile, state);
        }
        return fastValues;
    }

    private List<String> getLocalInputFiles(HadoopRunner runner) throws Exception {
        List<String> localInputFiles = new LinkedList<String>();
        if (runner.getFileMoveInput() != null) {
            localInputFiles.add(runner.getFileMoveInput());
        } else if (runner.getInputForMultiPathJobs() != null) {
            localInputFiles = runner.getInputForMultiPathJobs();
        } else {
            // This is bad, we will error out. This job should never be
            // scheduled without a input file.
            throw new Exception(
                    "Could not find any files for the copy. This job was fired without a indicator as to what files to run.");
        }
        return localInputFiles;
    }

    private void deleteIfFinished(Map<String, JobState> fastValues) throws ExecutionException {
        for (Entry<String, JobState> inputEntry : fastValues.entrySet()) {
            if (inputEntry.getValue().getState() == JobStateVal.FINISHED) {
                new File(inputEntry.getKey()).delete();
                // also delete from the 2ndary store (somewhat nasty hack cuz the way the 2ndary node is set up)
                try {
                    // fileformat zxtm-2010-04-01-110101
                    String filename = inputEntry.getKey().substring(inputEntry.getKey().lastIndexOf("/") + 1);

                    // remove the seconds cuz it takes a few to write the logs sometimes
                    // only delete the files from the backup dir IFF they are named the same (sans the seconds)
                    String smallerFileName = filename.substring(0, filename.length() - 2);
                    File backupDir = new File(utils.getBackupDir());
                    if (backupDir.exists()) {
                        String[] files = backupDir.list();
                        for (String file : files) {
                            if (file.contains(smallerFileName)) {
                                // this is a backup file that needs to be deleted,
                                // its from the same hour as the regular file
                                LOG.info("deleting file " + utils.getBackupDir() + file);
                                new File(utils.getBackupDir() + file).delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("could not delete file from backup", e);
                }
            }
        }
    }

    private void moveFilesOntoDFS(HadoopRunner runner, Map<String, JobState> fastValues) throws ExecutionException {

        HadoopConfiguration conf = hadoopTool.getConfiguration();
        String inputDir = hadoopTool.getInputDirectory();
        int offset = 0;

        for (Entry<String, JobState> inputEntry : fastValues.entrySet()) {
            String inputFile = inputEntry.getKey();
            JobState state = inputEntry.getValue();
            try {
                LOG.info("putting file on the DFS at " + inputDir);

                utils.makeDirectories(conf.getConfiguration(),
                        inputDir);
                // The files will be the same, so we have to place it as the
                // named file, so we need a new method.
                File f = new File(inputFile);
                String placedFile = inputDir + "/" + offset + "-" + f.getName();
                utils.placeFileOnDFS(conf.getConfiguration(),
                        inputFile, placedFile);
                LzoIndexer lzoIndexer = new LzoIndexer(hadoopTool.getConfiguration().getJobConf());
                //if its a LZO file, index it
                if (placedFile.endsWith(".lzo")) {
                    lzoIndexer.index(new Path(placedFile));
//                    ToolRunner.run(new DistributedLzoIndexer(), new String[]{placedFile});
                }
                offset++;

                finishJob(state);
                //scheduleSplitJob(runner, placedFile);

            } catch (Exception e) {
                LOG.error(e);
                failJob(state);
                throw new ExecutionException(e);
            }
        }
    }
}
