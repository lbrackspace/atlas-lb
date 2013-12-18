package org.openstack.atlas.logs.hadoop.jobs;

import com.hadoop.mapreduce.LzoTextInputFormat;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.joda.time.DateTime;
import org.openstack.atlas.logs.hadoop.comparators.LogGroupComparator;
import org.openstack.atlas.logs.hadoop.comparators.LogSortComparator;
import org.openstack.atlas.logs.hadoop.counters.CounterUtils;
import org.openstack.atlas.logs.hadoop.counters.LogCounters;
import org.openstack.atlas.logs.hadoop.mappers.LogMapper;
import org.openstack.atlas.logs.hadoop.partitioners.LogPartitioner;
import org.openstack.atlas.logs.hadoop.reducers.LogReducer;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.common.VerboseLogger;

public class HadoopLogSplitterJob extends HadoopJob {

    private static final VerboseLogger vlog = new VerboseLogger(HadoopLogSplitterJob.class);
    private static final Log LOG = LogFactory.getLog(HadoopLogSplitterJob.class);

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 6) {
            vlog.log("usage is <jarPath> <outDir> <histDir> <fileHour> <nReducers> <userName> <lzoFiles...>");
            return -1;
        }
        Path jarPath = new Path(args[0]);
        String outDir = args[1];
        String histDir = args[2];
        String fileHour = args[3];
        int nReducers = Integer.parseInt(args[4]);
        String userName = args[5];
        List<String> lzoFiles = new ArrayList<String>();
        for (int i = 6; i < args.length; i++) {
            lzoFiles.add(args[i]);
        }

        Job job = new Job(conf);
        System.setProperty("HADOOP_USER_NAME", userName);

        DateTime dt = StaticDateTimeUtils.nowDateTime(true);
        long dateOrd = StaticDateTimeUtils.dateTimeToOrdinalMillis(dt);
        String jobName = "LB_STATS" + ":" + fileHour + ":" + dateOrd;
        vlog.log(String.format("jobName=%s", jobName));
        job.setJarByClass(HadoopLogSplitterJob.class);
        job.setJobName(jobName);
        String hdfsZipDir = StaticFileUtils.joinPath(outDir, "zips");
        job.getConfiguration().set("fileHour", fileHour);
        job.getConfiguration().set("hdfs_user_name", userName);
        job.getConfiguration().set("hdfs_zip_dir", hdfsZipDir);
        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);
        FileSystem fs = FileSystem.get(defaultHdfsUri, conf, userName);
        //DistributedCache.addCacheFile(jarPath.toUri(), job.getConfiguration());
        DistributedCache.addFileToClassPath(jarPath, job.getConfiguration(), fs);
        //DistributedCache.createSymlink(job.getConfiguration());

        vlog.log(String.format("jobJar = %s", job.getJar()));

        job.setMapperClass(LogMapper.class);
        job.setMapOutputKeyClass(LogMapperOutputKey.class);
        job.setMapOutputValueClass(LogMapperOutputValue.class);

        job.setReducerClass(LogReducer.class);
        job.setOutputKeyClass(LogReducerOutputKey.class);
        job.setOutputValueClass(LogReducerOutputValue.class);

        job.setPartitionerClass(LogPartitioner.class);
        job.setSortComparatorClass(LogSortComparator.class);
        job.setGroupingComparatorClass(LogGroupComparator.class);

        job.setInputFormatClass(LzoTextInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        //job.getConfiguration().set("hadoop.jop.history.user.location", histDir);


        for (String lzoFileName : lzoFiles) {
            LzoTextInputFormat.addInputPath(job, new Path(lzoFileName));
        }
        FileOutputFormat.setOutputPath(job, new Path(outDir));

        String codecClassName = "com.hadoop.compression.lzo.LzopCodec";
        Class codecClass = Class.forName(codecClassName);
        job.getConfiguration().setClass("mapred.map.output.compression.codec", codecClass, CompressionCodec.class);
        job.getConfiguration().setBoolean("mapred.compress.map.output", true);

        job.setNumReduceTasks(nReducers);


        int exit;

        if (job.waitForCompletion(true)) {
            exit = 0;
            vlog.log("LogSplitter job finished");
        } else {
            exit = -1;
            vlog.log("LogSplitter job failed");
        }

        vlog.log(String.format("%s\n", CounterUtils.showCounters(job, LogCounters.values())));

        return exit;
    }
}
