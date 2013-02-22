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

    public String getCacheDir() {
        return configuration.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
    }

    public String getBackupDir() {
        return configuration.getString(LbLogsConfigurationKeys.rawlogs_backup_dir);
    }

    public FileStatus[] ls(Configuration conf, String dfsPath) throws IOException {
        return FileSystem.get(conf).listStatus(new Path(dfsPath));
    }

    public void makeDirectories(Configuration conf, String remotePath) throws IOException {
        makeDirectories(FileSystem.get(conf), remotePath);
    }

    public void makeDirectories(FileSystem fs, String remotePath) throws IOException {
        fs.mkdirs(new Path(remotePath));
    }

    public void placeFileOnDFS(Configuration conf, String localPath, String remotePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        fs.copyFromLocalFile(new Path(localPath),new Path(remotePath));
    }

    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.configuration = conf;
    }

    public Path moveLocal(Configuration conf, Path path) throws IOException {
        String base = getTempLocation();
        String generateRandomBase = generateRandomBase();
        Path local = new Path(base + path.getName() + generateRandomBase);
        FileSystem.get(conf).copyToLocalFile(path, local);
        return local;
    }

    public SequenceFile.Reader getLocalReader(Configuration conf, Path localPath) throws IOException {
        FileSystem fs = FileSystem.getLocal(conf);
        return new SequenceFile.Reader(fs, localPath, fs.getConf());
    }

    private synchronized String generateRandomBase() {
        return "-" + r.nextLong() + ".tmp";
    }

    public String getTempLocation() {
        return configuration.getString(LbLogsConfigurationKeys.rawlogs_cache_dir);
    }

    /**
     * Tries to exec the command, then it reads the output of the file to the logger and closes all streams.
     *
     * @param urchinCmd
     * @throws java.io.IOException
     */
    public void runAndClose(String urchinCmd) {
        Process process = null;
        LOG.debug(urchinCmd);
        try {
            process = Runtime.getRuntime().exec(urchinCmd);
        } catch (IOException e) {
            LOG.error(e);
        }
        if (process == null) {
            return;
        }

        //now try to close all the streams
        //first print the input if we need to
        try {
            if (LOG.isDebugEnabled()) {
                logBuffer(process.getInputStream());
                logBuffer(process.getErrorStream());
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        //close all 3
        try {
            process.getInputStream().close();
        } catch (IOException ex) {
        }
        try {
            process.getOutputStream().close();
        } catch (IOException e) {
        }
        try {
            process.getErrorStream().close();
        } catch (IOException e) {
        }
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

    public void deleteLocalFile(Path local) {
        new File(local.toUri().getPath()).delete();
        deleteCrc(local);
    }

    public void deleteCrc(Path local) {
        //Check for the .NAME.crc file to deleted.
        //The CRC is after the last /, and its a "." followed by the entire filename, and ".crc"
        String path = local.toUri().getPath();
        int indexOfParent = path.lastIndexOf("/");
        if (indexOfParent >= 0 && indexOfParent < path.length()) {
            // we assume there is a parent, grab it and manupilate the pate
            path = path.substring(0, indexOfParent + 1) + "." + path.substring(indexOfParent + 1) + ".crc";
            File pathFile = new File(path);
            if (pathFile.exists()) {
                pathFile.delete();
            }
        }
    }

    public String makeUnique(String filename) {
        return filename + "-" + r.nextLong();
    }
}
