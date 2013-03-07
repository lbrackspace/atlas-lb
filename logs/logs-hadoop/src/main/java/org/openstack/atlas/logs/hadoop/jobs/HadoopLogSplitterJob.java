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
import org.openstack.atlas.logs.hadoop.comparators.LogGroupComparator;
import org.openstack.atlas.logs.hadoop.comparators.LogSortComparator;
import org.openstack.atlas.logs.hadoop.mappers.LogMapper;
import org.openstack.atlas.logs.hadoop.partitioners.LogPartitioner;
import org.openstack.atlas.logs.hadoop.reducers.LogReducer;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.util.VerboseLogger;

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

        System.setProperty("HADOOP_USER_NAME", userName);
        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);

        Job job = new Job(conf);

        FileSystem fs = FileSystem.get(defaultHdfsUri, conf, userName);
        DistributedCache.addCacheFile(jarPath.toUri(), job.getConfiguration());
        DistributedCache.addFileToClassPath(jarPath, job.getConfiguration(), fs);
        DistributedCache.createSymlink(job.getConfiguration());

        job.setJobName(String.format("%s:%s", "LB_STATS", fileHour));
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

        job.getConfiguration().set("hadoop.jop.history.user.location", histDir);


        for (String lzoFileName : lzoFiles) {
            LzoTextInputFormat.addInputPath(job, new Path(lzoFileName));
        }
        FileOutputFormat.setOutputPath(job, new Path(outDir));

        String codecClassName = "com.hadoop.compression.lzo.LzopCodec";
        Class codecClass = Class.forName(codecClassName);
        job.getConfiguration().setClass("mapred.map.output.compression.codec", codecClass, CompressionCodec.class);
        job.getConfiguration().setBoolean("mapred.compress.map.output", true);

        job.setNumReduceTasks(nReducers);

        job.setJarByClass(HadoopLogSplitterJob.class);

        int exit;

        if (job.waitForCompletion(true)) {
            exit = 0;
            vlog.log("LogSplitter job finished");
        } else {
            exit = -1;
            vlog.log("LogSplitter job failed");
        }

        return exit;
    }
}
