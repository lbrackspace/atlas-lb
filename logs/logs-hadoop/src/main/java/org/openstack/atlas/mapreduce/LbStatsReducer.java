package org.openstack.atlas.mapreduce;

import org.openstack.atlas.io.FileBytesWritable;
import org.openstack.atlas.io.LbLogsAccountDateKey;
import org.openstack.atlas.io.LbLogsWritable;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.openstack.atlas.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Deprecated
public class LbStatsReducer implements Reducer<LbLogsAccountDateKey, LbLogsWritable, Text, FileBytesWritable> {
    
    private static String fileDate;
    private static final Log LOG = LogFactory.getLog(LbStatsReducer.class);
    private static ZipArchiveOutputStream stream = null;

    @Override
    public void close() throws IOException {
        //remote possibility of having 0 entries in the reducer (small runs)
        if (currentAccountId != null) {
            closeStream();
            writeToOutput(output);
        }
    }

    public void configure(JobConf job) {
        fileDate = job.get(Constants.FILEDATE).replaceAll(" ", "_");
    }

    private static String currentAccountId;
    private static long currentCount = 0;
    private static OutputCollector<Text, FileBytesWritable> output;

    public void reduce(LbLogsAccountDateKey key, Iterator<LbLogsWritable> values,
                       OutputCollector<Text, FileBytesWritable> output, Reporter reporter) throws IOException {

        if (this.output == null ){
            this.output = output;
        }
        try {
            if (currentAccountId == null) {
                LOG.info("currentLoadBalancerId == null");
                currentAccountId = key.getAccountId() + ":" + key.getLoadBalancerId();
                //fileDate = key.getDate();

                createStream();
            } else if (!currentAccountId.equals(key.getAccountId()+ ":" + key.getLoadBalancerId())) {
                LOG.info("!currentAccountId.equals(key.getAccountId())");
                closeStream();
                writeToOutput(output);
                currentAccountId = key.getAccountId()+ ":" + key.getLoadBalancerId();
                //fileDate = key.getDate();
                
                currentCount = 0;
                createStream();
            }
            LOG.info("No of values for account Id: " + key.getAccountId() + " is " + currentCount);
            
            writeToFile(values);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToOutput(OutputCollector<Text, FileBytesWritable> output) throws IOException {
        int numSpun = 0;
        String filename = createZipName(createPath(currentAccountId));
        LOG.info("Writing to Output: " + filename);
        File file = new File(filename);
        long filelen = file.length();
        long numBytesToWrite = file.length();
        while (filelen >= 0) {
            // split them up
            filelen -= FileBytesWritable.MAXSIZE;
            FileBytesWritable wr = new FileBytesWritable();
            wr.setMaxSize(numBytesToWrite);
            wr.setOrder(numSpun);
            wr.setFileName(filename);

            output.collect(new Text(currentAccountId), wr);
            numSpun++;
        }
        file.delete();

    }

    private void writeToFile(Iterator<LbLogsWritable> values) {
        LOG.info("reading values for " + currentAccountId);
        currentCount++;
        while (values.hasNext()) {
            LbLogsWritable item = values.next();
            try {
                String string = item.getLogline() + "\n";
                LOG.info("Writing the log line for : " + item.getAccountId() + ": \n" + item.getLogline());
                stream.write(string.getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void closeStream() throws IOException {
        LOG.info("closing stream for " + currentAccountId);
        stream.closeArchiveEntry();
        stream.close();
    }

    private void createStream() throws IOException, CompressorException {
        LOG.info("creating stream for " + currentAccountId);
        String path = createPath(currentAccountId);

        String filename = createFilename();
        String outputPath = createZipName(path);


        LOG.info("Writing to the file: " + outputPath);
        LOG.info("from Filename: " + filename);
        stream = new ZipArchiveOutputStream(new FileOutputStream(outputPath));
        stream.setLevel(9);
        stream.putArchiveEntry(new ZipArchiveEntry(filename));
    }
 
    private String createPath(String fqdn) {
        //return "./" + fqdn;
        return fqdn.split(":")[1];


    }

//    private String createFilename(CommonFqdnKey key) {
//        String path = "./" + key.getFqdn();
//        return createZipName(path);
//    }

    private String createFilename() {
        return "access_log_" + currentAccountId.split(":")[1] + "_"+ fileDate;
    }

    private String createZipName(String path) {
        return path + ".zip";
    }

}