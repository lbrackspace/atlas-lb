package org.openstack.atlas.scheduler.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.openstack.atlas.exception.ExecutionException;
import org.openstack.atlas.exception.SchedulingException;
import org.openstack.atlas.scheduler.JobScheduler;
import org.openstack.atlas.scheduler.MapReduceAggregateLogsJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.common.VerboseLogger;

public class FileMoveJobExecution extends LoggableJobExecution implements QuartzExecutable {

    private static final Log LOG = LogFactory.getLog(FileMoveJobExecution.class);
    private static final VerboseLogger vlog = new VerboseLogger(FileMoveJobExecution.class);
    protected String fileHour;
    protected JobScheduler jobScheduler;

    @Override
    public void execute(JobScheduler scheduler, QuartzSchedulerConfigs schedulerConfigs) throws ExecutionException {

        // stupid manual set, this has to be done. a circular dep because of how
        // quartz must init its scheduler factory crap. currently u cannot have
        // a bean that has a dependency on a bean that is in the
        // schedulerFactoryBean#schedulerContextAsMap
        jobScheduler = scheduler;
        fileHour = schedulerConfigs.getInputString();
        //hadoopTool.setupHadoopRun(runTime);
        //vlog.log(String.format("hadoopTool = %s", hadoopTool.toString()));

        try {
            List<String> localInputFiles = getLocalInputFiles(schedulerConfigs);
            vlog.log(String.format("calling createStateForMovingFiles(%s,%s)", fileHour, StaticStringUtils.collectionToString(localInputFiles, ",")));
            Map<String, JobState> fileNameStateMap = createStateForMovingFiles(fileHour, localInputFiles);
            for (String filename : localInputFiles) {
                if (filename.endsWith(".lzo")) {
                    schedulerConfigs.setLzoInput(true);
                }
            }
            vlog.log(String.format("about to move files onto DFS: schedulerConfis = %s fastValues= %s", schedulerConfigs.toString(), StaticStringUtils.mapToString(fileNameStateMap)));
            moveFilesOntoDFS(fileNameStateMap);
            deleteIfFinished(fileNameStateMap);
            scheduleMapReduceAggregateLogsJob(schedulerConfigs);

        } catch (Exception e) {
            LOG.error(e);
            throw new ExecutionException(e);
        }
    }

    @Required
    private void scheduleMapReduceAggregateLogsJob(QuartzSchedulerConfigs schedulerConfigs) throws SchedulingException {
        jobScheduler.scheduleJob(MapReduceAggregateLogsJob.class, schedulerConfigs);
    }

    private Map<String, JobState> createStateForMovingFiles(String inputString,
            List<String> localInputFiles) {
        Map<String, JobState> fileNameStateMap = new HashMap<String, JobState>();
        for (String inputFile : localInputFiles) {
            String jobInput = inputString + ":" + inputFile;
            vlog.log(String.format("calling createJob(FILECOPY,%s);", jobInput));
            JobState state = createJob(JobName.FILECOPY, jobInput);
            fileNameStateMap.put(inputFile, state);
            vlog.log(String.format("calling fastValues.put(%s,%s)", inputFile, state.toString()));
        }
        return fileNameStateMap;
    }

    private List<String> getLocalInputFiles(QuartzSchedulerConfigs schedulerConfigs) throws Exception {
        List<String> localInputFiles = new ArrayList<String>();
        if (schedulerConfigs.getFileMoveInput() != null) {
            localInputFiles.add(schedulerConfigs.getFileMoveInput());
        } else if (schedulerConfigs.getInputForMultiPathJobs() != null) {
            localInputFiles = schedulerConfigs.getInputForMultiPathJobs();
        } else {
            throw new Exception("Could not find any files for the copy. This job was fired without a indicator as to what files to run.");
        }
        return localInputFiles;
    }

    private void deleteIfFinished(Map<String, JobState> fastValues) throws ExecutionException {
        for (Entry<String, JobState> inputEntry : fastValues.entrySet()) {
            if (inputEntry.getValue().getState() == JobStateVal.FINISHED) {
                new File(inputEntry.getKey()).delete();
                try {
                    String filename = inputEntry.getKey().substring(inputEntry.getKey().lastIndexOf("/") + 1);

                    // remove the seconds cuz it takes a few to write the logs sometimes
                    // only delete the files from the backup dir IFF they are named the same (sans the seconds)
                    String smallerFileName = filename.substring(0, filename.length() - 2);
                    File backupDir = new File(HadoopLogsConfigs.getBackupDir());
                    if (backupDir.exists()) {
                        String[] files = backupDir.list();
                        for (String file : files) {
                            if (file.contains(smallerFileName)) {
                                // this is a backup file that needs to be deleted,
                                // its from the same hour as the regular file
                                LOG.info("deleting file " + HadoopLogsConfigs.getBackupDir() + file);
                                new File(HadoopLogsConfigs.getBackupDir() + file).delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("could not delete file from backup", e);
                }
            }
        }
    }

    private void moveFilesOntoDFS(Map<String, JobState> fileNameStateMap) throws ExecutionException {

        //HadoopConfiguration conf = hadoopTool.getConfiguration();
        //String inputDir = hadoopTool.getInputDirectory();
        List<String> inputDirList = new ArrayList<String>();
        inputDirList.add(HadoopLogsConfigs.getMapreduceInputPrefix());
        inputDirList.add(fileHour);
        String inputDir = StaticFileUtils.splitPathToString(inputDirList);
        int offset = 0;


        for (Entry<String, JobState> inputEntry : fileNameStateMap.entrySet()) {
            String inputFile = inputEntry.getKey();
            JobState state = inputEntry.getValue();
            try {
                LOG.info("putting file on the DFS at " + inputDir);

                hdfsUtils.mkDirs(inputDir, false);
                // The files will be the same, so we have to place it as the
                // named file, so we need a n
                String placedFile = inputDir + "/" + offset + "-" + StaticFileUtils.stripDirectoryFromFileName(inputFile);
                vlog.log(String.format("copying file %s -> to Hdfs %s", inputFile, placedFile));

                //utils.placeFileOnDFS(inputFile, placedFile);
                //if its a LZO file, index it
                if (placedFile.endsWith(".lzo")) {
                    vlog.log(String.format("file %s is an LZO recompressing and indexing", inputFile));
                    FSDataInputStream lzoIS = hdfsUtils.openHdfsInputFile(inputFile, true);
                    FSDataOutputStream lzoOS = hdfsUtils.openHdfsOutputFile(placedFile, false, true);
                    FSDataOutputStream idxOS = hdfsUtils.openHdfsOutputFile(placedFile + ".index", false, true);
                    hdfsUtils.recompressAndIndexLzoStream(lzoIS, lzoOS, idxOS, null);
                    idxOS.close();
                    lzoOS.close();
                    lzoIS.close();
                } else {
                    vlog.log(String.format("file %s is not compressed: Calling compression and indexer functions", inputFile));
                    FSDataInputStream uncompressedIS = hdfsUtils.openHdfsInputFile(inputFile, true);
                    FSDataOutputStream lzoOS = hdfsUtils.openHdfsOutputFile(placedFile + ".lzo", false, true);
                    FSDataOutputStream idxOS = hdfsUtils.openHdfsOutputFile(placedFile + ".lzo.index", false, true);
                    hdfsUtils.compressAndIndexStreamToLzo(uncompressedIS, lzoOS, lzoOS, hdfsUtils.getBufferSize(), null);
                    idxOS.close();
                    lzoOS.close();
                    uncompressedIS.close();
                }
                offset++;

                finishJob(state);

            } catch (Exception e) {
                LOG.error(e);
                failJob(state);
                throw new ExecutionException(e);
            }
        }
    }
}
