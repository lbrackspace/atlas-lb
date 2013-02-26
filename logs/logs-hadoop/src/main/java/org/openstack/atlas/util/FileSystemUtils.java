package org.openstack.atlas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.openstack.atlas.config.LbLogsConfigurationKeys;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FileSystemUtils {

    private static final Log LOG = LogFactory.getLog(FileSystemUtils.class);
    private org.openstack.atlas.cfg.Configuration configuration;
    private static final Random r = new Random();

    public void placeFileOnDFS(Configuration conf, String localPath, String remotePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        fs.copyFromLocalFile(new Path(localPath), new Path(remotePath));
    }

    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.configuration = conf;
    }

    public Path moveLocal(Configuration conf, Path path) throws IOException {
        String base = configuration.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
        String generateRandomBase = StaticFileUtils.generateRandomBase();
        Path local = new Path(base + path.getName() + generateRandomBase);
        FileSystem.get(conf).copyToLocalFile(path, local);
        return local;
    }

    public SequenceFile.Reader getLocalReader(Configuration conf, Path localPath) throws IOException {
        FileSystem fs = FileSystem.getLocal(conf);
        return new SequenceFile.Reader(fs, localPath, fs.getConf());
    }

    private void logBuffer(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInput = null;
        byte[] buffer = new byte[1024];
        BufferedInputStream is = new BufferedInputStream(inputStream);

        int bytesRead = 0;

        StringBuilder outputContents = new StringBuilder();
        while ((bytesRead = is.read(buffer)) != -1) {
            outputContents.append(new String(buffer, 0, bytesRead));
        }
        LOG.debug(outputContents);
    }

}
