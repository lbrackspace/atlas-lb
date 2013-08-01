package org.openstack.atlas.logs.hadoop.reducers;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.mapreduce.Reducer;
import org.openstack.atlas.logs.hadoop.counters.LogCounters;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.CRC32;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.logs.hadoop.util.StaticLogUtils;
import org.openstack.atlas.logs.hadoop.util.LogFileNameBuilder;

public class LogReducer extends Reducer<LogMapperOutputKey, LogMapperOutputValue, LogReducerOutputKey, LogReducerOutputValue> {

    private static final int BUFFER_SIZE = 1024 * 128;
    private int fileHour = -1;
    private static final short REPL_COUNT = 3;
    private static final int HDFS_BLOCK_SIZE = 1024 * 1024 * 64;
    private String hdfsUserName;
    private String hdfsZipDir;
    private String workDir;
    private FileSystem fs;

    @Override
    public void setup(Context ctx) throws IOException, InterruptedException {
        ctx.getCounter(LogCounters.REDUCER_SETUP_CALLS).increment(1);
        String fileHourString = ctx.getConfiguration().get("fileHour");
        fileHour = Integer.parseInt(fileHourString);
        hdfsUserName = ctx.getConfiguration().get("hdfs_user_name");
        hdfsZipDir = ctx.getConfiguration().get("hdfs_zip_dir");
        URI defaultHdfsUri = FileSystem.getDefaultUri(ctx.getConfiguration());
        fs = FileSystem.get(defaultHdfsUri, ctx.getConfiguration(), hdfsUserName);
        workDir = FileOutputFormat.getWorkOutputPath(ctx).toUri().getRawPath();
    }

    @Override
    public void reduce(LogMapperOutputKey rKey, Iterable<LogMapperOutputValue> rVals, Context ctx) throws IOException, InterruptedException {
        int accountId = rKey.getAccountId();
        int loadbalancerId = rKey.getLoadbalancerId();

        LogReducerOutputKey oKey = new LogReducerOutputKey();
        LogReducerOutputValue oVal = new LogReducerOutputValue();

        oKey.setAccountId(accountId);
        oKey.setLoadbalancerId(loadbalancerId);

        oVal.setAccountId(accountId);
        oVal.setLoadbalancerId(loadbalancerId);
        String zipFileName = LogFileNameBuilder.getZipFileName(loadbalancerId, fileHour);
        String zipContentsName = LogFileNameBuilder.getZipContentsName(loadbalancerId, fileHour);
        CRC32 crc = new CRC32();
        String partitionZipPath = StaticFileUtils.joinPath(hdfsZipDir, zipFileName);
        String fullZipPath = getTempWorkZipPath(workDir, "zips", zipFileName); // This one will replace fullZipPath
        FSDataOutputStream os = fs.create(new Path(fullZipPath), true, BUFFER_SIZE, REPL_COUNT, HDFS_BLOCK_SIZE);
        ZipOutputStream zos = new ZipOutputStream(os);
        String comment = String.format("Produced by HadoopJob %s: JobId=%s", ctx.getJobName(), ctx.getJobID().toString());
        zos.setComment(comment);
        zos.putNextEntry(new ZipEntry(zipContentsName));
        byte[] bytes = null;
        int nLines = 0;
        long fileSize = 0;
        for (LogMapperOutputValue rVal : rVals) {
            String logLine = rVal.getLogLine();
            bytes = logLine.getBytes("utf-8");
            fileSize += bytes.length;
            zos.write(bytes);
            crc.update(bytes);
            ctx.getCounter(LogCounters.REDUCER_REDUCTIONS).increment(1);
            ctx.getCounter(LogCounters.LOG_BYTE_COUNT).increment(bytes.length);
            nLines++;
        }
        zos.closeEntry(); // Closes the zip Contents
        zos.finish();     // Marks this as the last file in the zip arvhive
        zos.close();      // Closes the zipFile
        os.close();       // Just incase the Hdfs file is still open this closes it too.
        oVal.setnLines(nLines);
        oVal.setLogFile(partitionZipPath);
        oVal.setCrc(crc.getValue());
        oVal.setFileSize(fileSize);
        ctx.getCounter(LogCounters.REDUCER_WRITES).increment(1);
        ctx.write(oKey, oVal);
    }

    private String getTempWorkZipPath(String workDir, String midDir, String zipFileName) {
        List<String> pathComps = new ArrayList<String>();
        pathComps.add(workDir);
        pathComps.add(midDir);
        pathComps.add(zipFileName);
        return StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
    }
}
