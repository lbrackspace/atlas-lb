package org.openstack.atlas.logs.hadoop.util;

import java.io.FileNotFoundException;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.config.HadoopLogsConfigs;
import com.hadoop.compression.lzo.LzopCodec;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.io.SequenceFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.joda.time.DateTime;
import org.openstack.atlas.util.common.exceptions.DebugException;
import org.openstack.atlas.exception.ReflectionException;
import org.openstack.atlas.logs.hadoop.sequencefiles.EndOfIteratorException;
import org.openstack.atlas.logs.hadoop.sequencefiles.SequenceFileEntry;
import org.openstack.atlas.logs.hadoop.sequencefiles.SequenceFileIterator;
import org.openstack.atlas.logs.hadoop.sequencefiles.SequenceFileReaderException;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo.AdminStates;

public class HdfsUtils {

    private final Log LOG = LogFactory.getLog(HdfsUtils.class);
    private final VerboseLogger vlog = new VerboseLogger(HdfsUtils.class, VerboseLogger.LogLevel.INFO);
    public static final Pattern sequenceFilePattern = Pattern.compile("^(.*)(part-r-[0-9]+)$");
    public static final Pattern hdfsZipPattern = StaticLogUtils.zipLogPattern;
    public static final Pattern hdfsLzoPattern = Pattern.compile("^[0-9]-([0-9]{10})-access_log.aggregated.lzo$");
    public static final Pattern hdfsLzoPatternPre = Pattern.compile("^([0-9]{10})-access_log.aggregated.lzo$");
    public static final Pattern dateHourPattern = Pattern.compile("[0-9]{10}");
    public static final String HADOOP_USER_NAME = "HADOOP_USER_NAME"; // Silly isn't it.
    public static final String LB_LOGS_SPLIT = "lb_logs_split";
    protected int bufferSize = 256 * 1024;
    protected Configuration conf;
    protected String user;
    protected FileSystem remoteFileSystem;
    protected FileSystem localFileSystem;

    public HdfsUtils() {
    }

    @Override
    public String toString() {
        short repCount = (short) HadoopLogsConfigs.getReplicationCount();
        long blockSize = (long) HadoopLogsConfigs.getHdfsBlockSize() * 1024L * 1024L;
        return "HdfsUtils{bufferSize=" + bufferSize + ", user=" + user
                + ", repCount=" + repCount + ", blockSize="
                + blockSize + ", nameNode=" + getNameNode() + "}";
    }

    public HdfsUtils(Configuration conf, String user) {
        this.user = user;
        this.conf = conf;
    }

    public void init() throws IOException, InterruptedException {
        URI defaultUri = FileSystem.getDefaultUri(conf);
        localFileSystem = FileSystem.getLocal(conf).getRawFileSystem();
        localFileSystem.setVerifyChecksum(false); // This avoids the .crc files

        if (user == null) {
            remoteFileSystem = FileSystem.get(defaultUri, conf);
        } else {
            remoteFileSystem = FileSystem.get(defaultUri, conf, user);
            System.setProperty(HADOOP_USER_NAME, user);
        }
    }

    // For debugging Not used by the Application
    public Map<String, String> getConfigurationMap() {
        return getConfigurationMap(conf);
    }

    public List<LogReducerOutputValue> filterZipFileInfoList(List<LogReducerOutputValue> valueListIn, Integer accountId, Integer loadbalancerId) {
        List<LogReducerOutputValue> valueListOut = new ArrayList<LogReducerOutputValue>();
        for (LogReducerOutputValue val : valueListIn) {
            if (accountId != null && accountId != val.getAccountId()) {
                continue;
            }
            if (loadbalancerId != null && loadbalancerId != val.getLoadbalancerId()) {
                continue;
            }
            valueListOut.add(val);
        }
        return valueListOut;
    }

    public List<LogReducerOutputValue> getZipFileInfoList(String reducerOutputDirectory) throws SequenceFileReaderException {
        List<LogReducerOutputValue> zipFileInfoList = new ArrayList<LogReducerOutputValue>();
        List<Path> sequencePaths;
        SequenceFileIterator<LogReducerOutputKey, LogReducerOutputValue> zipIterator;
        try {
            sequencePaths = listSequenceFiles(reducerOutputDirectory, false);
        } catch (IOException ex) {
            String msg = "Error fetching list of sequence files";
            String excMsg = String.format("%s: %s", msg, Debug.getExtendedStackTrace(ex));
            LOG.error(excMsg, ex);
            throw new SequenceFileReaderException(msg, ex);
        }
        for (Path sequencePath : sequencePaths) {
            String sequenceFileName = sequencePath.toUri().toString();
            try {
                vlog.printf("Scanning reduceroutput directory %s", sequenceFileName);
                zipIterator = new SequenceFileIterator<LogReducerOutputKey, LogReducerOutputValue>(sequencePath, remoteFileSystem);
            } catch (SequenceFileReaderException ex) {
                String excMsg = Debug.getExtendedStackTrace(ex);
                LOG.error(excMsg, ex);
                throw ex;
            }
            while (true) {
                try {
                    SequenceFileEntry<LogReducerOutputKey, LogReducerOutputValue> zipFileEntry;
                    zipFileEntry = zipIterator.getNextEntry();
                    LogReducerOutputValue val = zipFileEntry.getValue();
                    zipFileInfoList.add(val);
                } catch (SequenceFileReaderException ex) {
                    String excMsg = Debug.getExtendedStackTrace(ex);
                    LOG.error(excMsg, ex);
                    zipIterator.close();
                    throw ex;
                } catch (EndOfIteratorException ex) {
                    zipIterator.close();
                    break;
                }
            }
        }
        Collections.sort(zipFileInfoList);
        return zipFileInfoList;
    }

    // For debugging Not used by the Application
    public static Map<String, String> getConfigurationMap(Configuration conf) {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<String, String> ent : conf) {
            map.put(ent.getKey(), ent.getValue());
        }
        return map;
    }

    public List<Path> listSequenceFiles(String dirPath, boolean useLocal) throws IOException {
        List<Path> reducerOutputPaths = listPaths(dirPath, useLocal);
        List<Path> sequenceFiles = new ArrayList<Path>();
        Matcher sequenceFileMatcher = sequenceFilePattern.matcher("");

        for (Path reducedOutputPath : reducerOutputPaths) {
            sequenceFileMatcher.reset(reducedOutputPath.toUri().getPath());
            if (sequenceFileMatcher.find()) {
                sequenceFiles.add(reducedOutputPath);
            }
        }
        return sequenceFiles;
    }

    // Silly but this will open an LZO decompress and then recompresse it with Indexing
    public void recompressAndIndexLzoStream(InputStream lzoInputStream, OutputStream lzoOutputStream, OutputStream lzoIndexedOutputStream, PrintStream ps) throws IOException {
        Configuration codecConf = new Configuration();
        codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(codecConf);
        CompressionInputStream cis = codec.createInputStream(lzoInputStream);
        CompressionOutputStream cos = codec.createIndexedOutputStream(lzoOutputStream, new DataOutputStream(lzoIndexedOutputStream));
        StaticFileUtils.copyStreams(cis, cos, ps, bufferSize);
        cis.close();
        cos.close();
    }

    public CompressionInputStream openLzoDecompressionStream(String file_name) throws FileNotFoundException, IOException {
        Configuration codecConf = new Configuration();
        codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        String fullPath = StaticFileUtils.expandUser(file_name);
        codec.setConf(codecConf);
        FileInputStream fis = new FileInputStream(fullPath);
        CompressionInputStream cis = codec.createInputStream(fis);
        return cis;
    }

    public void compressAndIndexStreamToLzo(InputStream uncompressedInputStream, OutputStream lzoOutputStream, OutputStream lzoIndexedOutputStream, int buffSize, PrintStream ps) throws IOException {
        Configuration codecConf = new Configuration();
        codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(codecConf);
        CompressionOutputStream cos = codec.createIndexedOutputStream(lzoOutputStream, new DataOutputStream(lzoIndexedOutputStream));
        StaticFileUtils.copyStreams(uncompressedInputStream, cos, ps, bufferSize);
        cos.close();
    }

    public Integer getLzoHourKey(String lzoPath) {
        String lzoName = StaticFileUtils.stripDirectoryFromFileName(lzoPath);
        Matcher m = hdfsLzoPatternPre.matcher(lzoName);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (ArithmeticException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<FileStatus> listHdfsLzoStatus(String dateHourSearch) {
        List<FileStatus> lzoStatusList = new ArrayList<FileStatus>();
        String hdfsInputPrefix = HadoopLogsConfigs.getMapreduceInputPrefix();
        FileStatus[] inDateDirArray;
        try {
            inDateDirArray = remoteFileSystem.listStatus(new Path(hdfsInputPrefix));
        } catch (IOException ex) {
            LOG.error(String.format("Unable to read directory %s:%s", hdfsInputPrefix, Debug.getExtendedStackTrace(ex)));
            return lzoStatusList;
        }
        if (inDateDirArray == null) {
            LOG.error(String.format("Unable to read directory %s: listStatus returned null", hdfsInputPrefix));
            return lzoStatusList;
        }
        for (FileStatus dateDirFileStatus : inDateDirArray) {
            String dateHourDir = StaticFileUtils.pathTail(dateDirFileStatus.getPath().toUri().getRawPath());
            Matcher dateHourMatcher = dateHourPattern.matcher(dateHourDir);
            if (!dateDirFileStatus.isDir() || !dateHourMatcher.find()) {
                continue; // This isn't a date Directory
            }
            if (dateHourSearch != null && !dateHourDir.startsWith(dateHourSearch)) {
                continue;
            }
            String lzoDir = StaticFileUtils.joinPath(hdfsInputPrefix, dateHourDir);
            FileStatus[] lzoDirStatusArray;
            try {
                lzoDirStatusArray = remoteFileSystem.listStatus(new Path(lzoDir));
            } catch (IOException ex) {
                LOG.error(String.format("Error reading directory %s: %s SKIPPING", lzoDir, Debug.getExtendedStackTrace(ex)));
                continue;
            }
            if (lzoDirStatusArray == null) {
                LOG.error(String.format("Error reading directory %s listStatus returned null skipping", lzoDir));
            }
            for (FileStatus lzoFileStatus : lzoDirStatusArray) {
                String lzoFileName = StaticFileUtils.pathTail(lzoFileStatus.getPath().toUri().getRawPath());
                Matcher m = hdfsLzoPattern.matcher(lzoFileName);
                if (!lzoFileStatus.isDir() && m.find()) {
                    lzoStatusList.add(lzoFileStatus);
                }
            }
        }
        return lzoStatusList;
    }

    public List<String> getZipsOlderThan(int daysAgo) throws Exception {
        List<String> out = new ArrayList<String>();
        List<Long> hourKeysList = new ArrayList<Long>();
        List<String> dirComps = new ArrayList<String>();
        String lbLogSplitDir = StaticFileUtils.mergePathString(HadoopLogsConfigs.getMapreduceOutputPrefix(), LB_LOGS_SPLIT);
        FileStatus[] dateDirsStats = getFileSystem().listStatus(new Path(lbLogSplitDir));
        DateTime now = StaticDateTimeUtils.nowDateTime(true);
        Long daysAgoLong = StaticDateTimeUtils.dateTimeToHourLong(
                StaticDateTimeUtils.nowDateTime(
                true).minusDays(daysAgo));
        for (FileStatus fileStatus : dateDirsStats) {
            Long hourLong;
            String pathStr;
            try {
                pathStr = HdfsUtils.pathTailString(fileStatus);
                hourLong = Long.parseLong(pathStr);
                if (hourLong < daysAgoLong) {
                    hourKeysList.add(hourLong);
                }
            } catch (NumberFormatException ex) {
                // This non number directory will not contain an LZO so skip it.
                continue;
            }
        }
        Collections.sort(hourKeysList);
        for (Long hourLong : hourKeysList) {
            dirComps.clear();
            dirComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
            dirComps.add(LB_LOGS_SPLIT);
            dirComps.add(hourLong.toString());
            String pathStr = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(dirComps));
            out.add(pathStr);
        }
        return out;
    }

    public List<DeleteDirectoryResponse> rmZipsOlderThan(int daysAgo) throws Exception {
        List<DeleteDirectoryResponse> resp = new ArrayList<DeleteDirectoryResponse>();
        List<String> doomedZIPs = getZipsOlderThan(daysAgo);
        for (String doomedZip : doomedZIPs) {
            try {
                boolean status = remoteFileSystem.delete(new Path(doomedZip), true);
                resp.add(new DeleteDirectoryResponse(doomedZip, status, null));
            } catch (Exception ex) {
                String fmt = "Error could not delete HDFS %s %s\n";
                String excMsg = Debug.getEST(ex);
                LOG.error(String.format(fmt, doomedZip, excMsg), ex);
                resp.add(new DeleteDirectoryResponse(doomedZip, false, ex));
            }
        }
        return resp;
    }

    public List<DeleteDirectoryResponse> rmLZOsOlderThan(int daysAgo) throws IOException {
        List<DeleteDirectoryResponse> resp = new ArrayList<DeleteDirectoryResponse>();
        List<String> doomedLZOs = getLZOsOlderThan(daysAgo);
        for (String doomedLZO : doomedLZOs) {
            try {
                boolean status = remoteFileSystem.delete(new Path(doomedLZO), true);
                resp.add(new DeleteDirectoryResponse(doomedLZO, status, null));
            } catch (Exception ex) {
                String fmt = "Error could not delete HDFS %s %s\n";
                String excMsg = Debug.getEST(ex);
                LOG.error(String.format(fmt, doomedLZO, excMsg), ex);
                resp.add(new DeleteDirectoryResponse(doomedLZO, false, ex));
            }
        }
        return resp;
    }

    public List<String> getLZOsOlderThan(int daysAgo) throws IOException {
        List<String> out = new ArrayList<String>();
        List<Long> hourDirs = new ArrayList<Long>();
        int nFiles = 0;

        FileStatus[] stats = getFileSystem().listStatus(new Path(HadoopLogsConfigs.getMapreduceInputPrefix()));
        Long daysAgoLimit = StaticDateTimeUtils.dateTimeToHourLong(
                StaticDateTimeUtils.nowDateTime(
                true).minusDays(daysAgo));
        for (FileStatus stat : stats) {
            try {
                String pathStr = pathTailString(stat);
                long hourLong = Long.parseLong(pathStr);
                if (hourLong < daysAgoLimit) {
                    nFiles++;
                    hourDirs.add(hourLong);
                }
            } catch (Exception ex) {
                continue;
            }
        }
        Collections.sort(hourDirs);
        for (Long hourLong : hourDirs) {
            String pathStr = StaticFileUtils.joinPath(HadoopLogsConfigs.getMapreduceInputPrefix(), hourLong.toString());
            out.add(pathStr);
        }
        return out;
    }

    public List<FileStatus> listHdfsZipsStatus(String dateHourSearch, String lidSearch, boolean showMissingDirsOnly) {
        List<FileStatus> statusList = new ArrayList<FileStatus>();
        List<String> lbSplitDirComponents = new ArrayList<String>();
        lbSplitDirComponents.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
        lbSplitDirComponents.add(LB_LOGS_SPLIT);
        FileStatus[] logDirStatusArray;
        String logSplitDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(lbSplitDirComponents));
        try {
            logDirStatusArray = remoteFileSystem.listStatus(new Path(logSplitDir));
        } catch (IOException ex) {
            LOG.error(String.format("Unable to read directory %s: %s", logSplitDir, Debug.getExtendedStackTrace(ex)), ex);
            return statusList;
        }
        if (logDirStatusArray == null) {
            LOG.error(String.format("Unable to read directory %s: listStatus returned null", logSplitDir));
            return statusList;
        }
        for (FileStatus logDirStatus : logDirStatusArray) {
            if (logDirStatus.isDir()) {
                String foundDateKey = StaticFileUtils.pathTail(logDirStatus.getPath().toUri().getRawPath());
                if (!isDateHourKey(foundDateKey)) {
                    continue; // This directory must be something else
                }
                if (dateHourSearch != null && !foundDateKey.startsWith(dateHourSearch)) {
                    continue; // Skip this entry since a search was on the parameters but no match was found
                }
                List<String> zipDirComps = new ArrayList<String>(lbSplitDirComponents);
                zipDirComps.add(foundDateKey);
                zipDirComps.add("zips");
                FileStatus[] zipDirStatusArray;
                String zipDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(zipDirComps));
                try {
                    zipDirStatusArray = remoteFileSystem.listStatus(new Path(zipDir));
                } catch (IOException ex) {
                    LOG.error(String.format("Unable to scan directory %s: %s", zipDir, Debug.getExtendedStackTrace(ex), ex));
                    continue;
                }
                if (zipDirStatusArray == null) {
                    LOG.error(String.format("Unable to read directory %s: listStatus returned null", zipDir));
                    if (showMissingDirsOnly) {
                        statusList.add(logDirStatus);
                    }
                    continue;
                }
                for (FileStatus zipStatus : zipDirStatusArray) {
                    String zipFileName = StaticFileUtils.pathTail(zipStatus.getPath().toUri().getRawPath());
                    if (!zipStatus.isDir()) {
                        Matcher zipMatcher = hdfsZipPattern.matcher(zipFileName);
                        if (!zipMatcher.find()) {
                            continue; // This isn't a zip file
                        }

                        if (lidSearch != null && !lidSearch.equals(zipMatcher.group(2))) {
                            continue; // This isn't what where looking for.
                        }
                        if (showMissingDirsOnly) {
                            continue; // We only care about dates that are missing.
                        }
                        statusList.add(zipStatus);
                    }
                }
            }

        }
        return statusList;
    }

    public boolean isDateHourKey(String dateKey) {
        Matcher m = dateHourPattern.matcher(dateKey);
        return m.matches();
    }

    public FileStatus[] listStatuses(String filePath, boolean useLocal) throws IOException {
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        return fs.listStatus(new Path(filePath));
    }

    public List<String> getLocalInputFiles(String znodeBase) {
        List<String> logs = new ArrayList<String>();
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

    public List<Path> listPaths(String inPath, boolean useLocal) throws IOException {
        List<Path> paths = new ArrayList<Path>();

        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        FileStatus[] stats = fs.listStatus(new Path(inPath));
        if (stats == null) {
            throw new IOException(String.format("could not list status for Directory %s", inPath));
        }
        for (FileStatus stat : stats) {
            if (stat.isDir()) {
                continue; // Don't follow directorys
            }
            paths.add(stat.getPath());
        }
        return paths;
    }

    public FSDataOutputStream openHdfsOutputFile(String path, boolean useLocal, boolean allowOveride) throws IOException {
        return openHdfsOutputFile(new Path(path), useLocal, allowOveride);
    }

    public FSDataOutputStream openHdfsOutputFile(Path path, boolean useLocal, boolean allowOveride) throws IOException {
        short repCount = (short) HadoopLogsConfigs.getReplicationCount();
        long blockSize = (long) HadoopLogsConfigs.getHdfsBlockSize() * 1024L * 1024L;
        if (useLocal) {
            return localFileSystem.create(path, allowOveride, bufferSize, repCount, blockSize);
        } else {
            return remoteFileSystem.create(path, allowOveride, bufferSize, repCount, blockSize);
        }
    }

    public FSDataInputStream openHdfsInputFile(String path, boolean useLocal) throws IOException {
        return openHdfsInputFile(new Path(path), useLocal);
    }

    public FSDataInputStream openHdfsInputFile(Path path, boolean useLocal) throws IOException {
        if (useLocal) {
            return localFileSystem.open(path, bufferSize);
        } else {
            return remoteFileSystem.open(path, bufferSize);
        }
    }

    public FileOwner getFileOwner(String fileName, boolean useLocal) throws IOException {
        return getFileOwner(new Path(fileName), useLocal);
    }

    public FileOwner getFileOwner(Path path, boolean useLocal) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : remoteFileSystem;
        FileStatus status = fs.getFileStatus(path);
        return new FileOwner(status.getOwner(), status.getGroup());
    }

    public FileOwner getDirectoryOwner(Path path, boolean useLocal) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : remoteFileSystem;
        FileStatus status = fs.getFileStatus(path);
        if (status.isDir()) {
            return new FileOwner(status.getOwner(), status.getGroup());
        }
        status = fs.getFileStatus(status.getPath().getParent());
        return new FileOwner(status.getOwner(), status.getGroup());
    }

    public void mkDirs(String dirPath, boolean useLocal) throws IOException {
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        fs.mkdirs(new Path(dirPath));
    }

    public void mkDirs(String dirPath, FsPermission perms, boolean useLocal) throws IOException {
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        fs.mkdirs(new Path(dirPath), perms);
    }

    public FileOwner getDirectoryOwner(String fileName, boolean useLocal) throws IOException {
        return getDirectoryOwner(new Path(fileName), useLocal);
    }

    public void setFileOwner(String fileName, boolean useLocal, String user, String group) throws IOException {
        setFileOwner(new Path(fileName), useLocal, user, group);
    }

    public void setFileOwner(Path path, boolean useLocal, String user, String group) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : remoteFileSystem;
        fs.setOwner(path, user, group);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setConf(Configuration conf) throws IOException, InterruptedException {
        this.conf = conf;
    }

    public Configuration getConf() {
        return conf;
    }

    public void setUser(String user) {
        this.user = user;
        System.setProperty(HADOOP_USER_NAME, user);
    }

    public String getUser() {
        return user;
    }

    public FileSystem getFileSystem() {
        return remoteFileSystem;
    }

    public FileSystem getLocalFileSystem() {
        return localFileSystem;
    }

    public Path moveToLocalCacheDir(Path path) throws IOException {
        String base = HadoopLogsConfigs.getCacheDir();
        String generateRandomBase = StaticFileUtils.generateRandomBase();
        Path local = new Path(base + path.getName() + generateRandomBase);
        FileSystem.get(conf).copyToLocalFile(path, local);
        return local;
    }

    public SequenceFile.Reader getReader(Path path, boolean useLocal) throws IOException {
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        return new SequenceFile.Reader(fs, path, fs.getConf());
    }

    public String getNameNode() {
        if (remoteFileSystem == null) {
            return null;
        }

        Configuration fsConf = remoteFileSystem.getConf();
        if (fsConf == null) {
            return null;
        }
        String nameNode = fsConf.get("fs.default.name", null);
        return nameNode;
    }

    public static String pathUriString(Path path) {
        return path.toUri().getPath();
    }

    public static void deleteLocalFile(Path filePath) {
        new File(filePath.toUri().getPath()).delete();
        deleteCrc(filePath);
    }

    public static void deleteCrc(Path filePath) {
        //Check for the .NAME.crc file to deleted.
        //The CRC is after the last /, and its a "." followed by the entire filename, and ".crc"
        String path = filePath.toUri().getPath();
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

    public List<FileStatus> listFileStatusRecursively(String mntPathIn, boolean useLocal) {
        List<FileStatus> statusList = new ArrayList<FileStatus>();
        String mntPath = StaticFileUtils.expandUser(mntPathIn);
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        FileStatus[] statusArray;
        try {
            statusArray = fs.listStatus(new Path(mntPath));
        } catch (IOException ex) {
            vlog.printf("Error reading directory %s: %s", mntPath);
            return statusList;
        }
        if (statusArray == null) {
            return statusList;
        }
        for (FileStatus fileStatus : statusArray) {
            String subFilePath = mntPath + File.separatorChar + fileStatus.getPath().getName();
            statusList.add(fileStatus);
            if (fileStatus.isDir()) {
                if (useLocal) {
                    try {
                        if (StaticFileUtils.isSymLink(subFilePath)) {
                            continue; // Don't follow symLinks
                        }
                    } catch (IOException ex) {
                    }
                }
                List<FileStatus> subDirectoryStats = listFileStatusRecursively(subFilePath, useLocal);
                statusList.addAll(subDirectoryStats);
            }
        }
        return statusList;
    }

    public List<String> listFilesRecursively(String mntPathIn, boolean useLocal) throws IOException {
        LOG.info(String.format("Scanning %s\n", mntPathIn));
        String mntPath = StaticFileUtils.expandUser(mntPathIn);
        List<String> fileNames = new ArrayList<String>();
        FileSystem fs;

        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = remoteFileSystem;
        }
        FileStatus[] fileStatuses = fs.listStatus(new Path(mntPath));
        if (fileStatuses == null) {
            throw new IOException("Error reading directory " + mntPath);
        }
        for (FileStatus fileStatus : fileStatuses) {
            String subFilePath = mntPath + File.separatorChar + fileStatus.getPath().getName();

            if (fileStatus.isDir()) {
                if (useLocal) { // Make sure where not following a symlink. Cause circuler links are dangours
                    try {
                        if (StaticFileUtils.isSymLink(subFilePath)) {
                            continue; // Refuse to follow SymLinks.
                        }
                    } catch (IOException ex) {
                    }
                }
                List<String> subDirectoryList = listFilesRecursively(subFilePath, useLocal);
                fileNames.addAll(subDirectoryList);
            } else {
                fileNames.add(subFilePath);
            }
        }
        return fileNames;
    }

    public static String rawPath(FileStatus fileStatus) {
        return fileStatus.getPath().toUri().getRawPath();
    }

    public static Object newUtils(Class utilClass, String user, String... confFiles) throws ReflectionException {
        Configuration conf = new Configuration();
        for (String confFile : confFiles) {
            Path confPath = new Path(StaticFileUtils.expandUser(confFile));
            conf.addResource(confPath);
        }
        Object obj;
        Method confSetter;
        Method userSetter;
        Method initMethod;
        try {
            obj = utilClass.newInstance();
            confSetter = utilClass.getMethod("setConf", Configuration.class);
            userSetter = utilClass.getMethod("setUser", String.class);
            initMethod = utilClass.getMethod("init");
            confSetter.invoke(obj, conf);
            userSetter.invoke(obj, user);
            initMethod.invoke(obj);
        } catch (IllegalArgumentException ex) {
            throw new ReflectionException(ex);
        } catch (InvocationTargetException ex) {
            throw new ReflectionException(ex);
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException(ex);
        } catch (SecurityException ex) {
            throw new ReflectionException(ex);
        } catch (InstantiationException ex) {
            throw new ReflectionException(ex);
        } catch (IllegalAccessException ex) {
            throw new ReflectionException(ex);
        }
        return obj;
    }

    public static String pathTailString(Path path) {
        return StaticFileUtils.pathTail(path.toUri().getRawPath());
    }

    public static String pathTailString(FileStatus fileStatus) {
        return pathTailString(fileStatus.getPath());
    }
}
