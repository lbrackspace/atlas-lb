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

    public void copyToLocalFile(Configuration conf, Path src, Path dest) throws IOException {
        copyToLocalFile(FileSystem.get(conf), src, dest);
    }

    public void copyToLocalFile(FileSystem fs, Path src, Path dest) throws IOException {
        fs.copyToLocalFile(src, dest);
    }

    public List<String> getLocalInputFiles() {
        List<String> logs = new LinkedList<String>();
        String znodeBase = configuration.getString(LbLogsConfigurationKeys.filesystem_root_dir);
        File znodesParent = new File(znodeBase);
        String[] znodes = znodesParent.list();
        for (int i = 0; i < znodes.length; i++) {
            String znode = znodes[i];
            File znodefile = new File(znodeBase + znode);
            if (znodefile.isDirectory()) {
                String[] logfiles = znodefile.list();
                for (int j = 0; j < logfiles.length; j++) {
                    String logfile = logfiles[j];
                    logs.add(znodeBase + znode + "/" + logfile);
                }
            } else if (znodefile.isFile()) {
                logs.add(znodeBase + znode);
            }
        }
        return logs;
    }

    public List<Path> getPathsForImport(Configuration conf, String outputDirectory) throws IOException {
        List<Path> paths = new LinkedList<Path>();

        FileStatus[] files;
        files = ls(conf, outputDirectory);
        for (int i = 0; i < files.length; i++) {
            FileStatus fileStatus = files[i];
            if (fileStatus.getPath().getName().endsWith(
                    configuration.getString(LbLogsConfigurationKeys.basemapreduce_log_suffix))) {
                continue;
            }
            paths.add(fileStatus.getPath());
        }
        return paths;

    }

    public MapFile.Reader[] getReaders(Configuration conf, Path outputDir) throws IOException {
        return MapFileOutputFormat.getReaders(FileSystem.get(conf), outputDir, conf);
    }

    public MapFile.Writer getMapWriter(Configuration conf, String output, Class keyClass, Class valClass) throws IOException {
        return new MapFile.Writer(conf, FileSystem.get(conf), output, keyClass, valClass);
    }

    public MapFile.Writer getLocalMapWriter(Configuration conf, String output, Class keyClass, Class valClass) throws IOException {
        FileSystem fs = FileSystem.getLocal(conf);
        return new MapFile.Writer(conf, fs, output, keyClass, valClass);
    }

    public SequenceFile.Writer getWriter(Configuration conf, String output, Class keyClass, Class valClass) throws IOException {
        return new SequenceFile.Writer(FileSystem.get(conf), conf, new Path(output), keyClass, valClass);
    }

    public String getRestOfFilename(String fullFilename) {
        if (fullFilename.contains("/")) {
            return fullFilename.substring(fullFilename.lastIndexOf("/") + 1);
        } else {
            return fullFilename;
        }
    }

    public FileStatus[] ls(Configuration conf, String dfsPath) throws IOException {
        return FileSystem.get(conf).listStatus(new Path(dfsPath));
    }

    public FileStatus[] ls(FileSystem fs, String dfsPath) throws IOException {
        return fs.listStatus(new Path(dfsPath));
    }

    public void makeDirectories(Configuration conf, String remotePath) throws IOException {
        makeDirectories(FileSystem.get(conf), remotePath);
    }

    public void makeDirectories(FileSystem fs, String remotePath) throws IOException {
        LOG.info("Path makeDirectories:" +  new Path(remotePath));
        fs.mkdirs(new Path(remotePath));
    }

    public void placeFileOnDFS(Configuration conf, String localPath, String remotePath) throws IOException {
        placeFileOnDFS(FileSystem.get(conf), localPath, remotePath);
    }

    public void placeFileOnDFS(FileSystem fs, String localPath, String remotePath) throws IOException {
        fs.copyFromLocalFile(new Path(localPath), new Path(remotePath));
    }

    public void putFileArgsOntoDFS(FileSystem fs, String inputDir, String[] files) throws IOException {
        if (files.length > 0) {
            // we assume these are files that need to be placed on the dfs
            // before running the job.
            for (String fileString : files) {
                File argFile = new File(fileString);
                if (argFile.isDirectory()) {
                    // get all the files in the directory
                    // argFile
                    putFileArgsOntoDFS(fs, inputDir, prependString(argFile.getAbsolutePath() + "/", argFile
                            .list()));
                } else {
                    makeDirectories(fs, inputDir);
                    placeFileOnDFS(fs, argFile.getAbsolutePath(), inputDir);
                }
            }
        }
    }

    /**
     * This puts a set of files and directories onto the DFS into the inputDir
     * using the configuration values for the DFS.
     *
     * @param conf
     * @param inputDir
     * @param files
     * @throws java.io.IOException
     */
    public void putLocalFilesOntoDFS(Configuration conf, String inputDir, String[] files) throws IOException {
        putFileArgsOntoDFS(FileSystem.get(conf), inputDir, files);
    }

    public FSDataInputStream readFileFromDFS(Configuration conf, Path dfsPath) throws IOException {
        return readFileFromDFS(FileSystem.get(conf), dfsPath);
    }

    public FSDataInputStream readFileFromDFS(Configuration conf, String dfsPath) throws IOException {
        return readFileFromDFS(FileSystem.get(conf), dfsPath);
    }

    public FSDataInputStream readFileFromDFS(FileSystem fs, Path dfsPath) throws IOException {
        return fs.open(dfsPath);
    }

    public FSDataInputStream readFileFromDFS(FileSystem fs, String dfsPath) throws IOException {
        return fs.open(new Path(dfsPath));
    }

    public SequenceFile.Reader readSequenceFileFromDFS(Configuration conf, Path dfsPath) throws IOException {
        return new SequenceFile.Reader(FileSystem.get(conf), dfsPath, conf);
    }

    public SequenceFile.Reader readSequenceFileFromDFS(FileSystem fs, Path dfsPath) throws IOException {
        return new SequenceFile.Reader(fs, dfsPath, fs.getConf());
    }

    public void removeFileFromDFS(Configuration conf, String remotePath) throws IOException {
        removeFileFromDFS(FileSystem.get(conf), remotePath, true);
    }

    public void removeFileFromDFS(FileSystem fs, String path, boolean recursive) throws IOException {
        fs.delete(new Path(path), recursive);
    }

    public String sanitizeDir(String dir) {
        String sanitized = dir;
        if (sanitized.contains("-*")) {
            sanitized = sanitized.replace("-*", "");
        }
        return sanitized;
    }

    public void setConf(org.openstack.atlas.cfg.Configuration conf) {
        this.configuration = conf;
    }

    public void swallowAndClose(FSDataInputStream file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void swallowAndClose(SequenceFile.Reader file) {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] prependString(String prepend, String[] list) {
        String[] newItems = new String[list.length];
        for (int i = 0; i < newItems.length; i++) {
            newItems[i] = prepend + list[i];
        }
        return newItems;
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

    /**
     * Do not use this method anymore. Use moveLocal and getLocalReader, so you can use deleteLocalFile as well
     *
     * @param conf
     * @param path
     * @return
     * @throws java.io.IOException
     */
    @Deprecated
    public SequenceFile.Reader moveLocalAndReadSequenceFile(Configuration conf, Path path) throws IOException {
        Path local = moveLocal(conf, path);
        FileSystem fs = FileSystem.getLocal(conf);
        return new SequenceFile.Reader(fs, local, fs.getConf());
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
