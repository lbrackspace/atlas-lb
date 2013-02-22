package org.openstack.atlas.util;

import com.hadoop.compression.lzo.LzopCodec;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.openstack.atlas.util.exceptions.ReflectionException;

public class HdfsUtils {

    private final Log LOG = LogFactory.getLog(HdfsUtils.class);
    public static final Pattern sequenceFilePattern = Pattern.compile("^(.*)(part-r-[0-9]+)$");
    protected int bufferSize = 256 * 1024;
    protected Configuration conf;
    protected String user;
    protected FileSystem fileSystem;
    protected FileSystem localFileSystem;
    protected short repCount = 3;
    protected long blockSize = 64 * 1024 * 1024;

    public HdfsUtils() {
    }

    @Override
    public String toString() {
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
            fileSystem = FileSystem.get(defaultUri, conf);
        } else {
            fileSystem = FileSystem.get(defaultUri, conf, user);
        }

    }

    // For debugging
    public Map<String, String> getConfigurationMap() {
        return getConfigurationMap(conf);
    }

    // For debugging
    public static Map<String, String> getConfigurationMap(Configuration conf) {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<String, String> ent : conf) {
            map.put(ent.getKey(), ent.getValue());
        }
        return map;
    }

    public List<Path> listSequenceFiles(String dirPath, boolean useLocal) throws IOException {
        List<Path> reducerOutputPaths = listFiles(dirPath, useLocal);
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

    public void compressAndIndexStreamToLzo(InputStream uncompressedIS, OutputStream lzoOutputStream, OutputStream lzoIndexedOutputStream, int buffSize) throws IOException {
        Configuration codecConf = new Configuration();
        codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(codecConf);
        CompressionOutputStream cos = codec.createIndexedOutputStream(lzoOutputStream, new DataOutputStream(lzoIndexedOutputStream));
        StaticFileUtils.copyStreams(uncompressedIS, cos, null, bufferSize);
        cos.close();
    }

    public List<Path> listFiles(String inPath, boolean useLocal) throws IOException {
        List<Path> paths = new ArrayList<Path>();
        FileStatus[] stats = fileSystem.listStatus(new Path(inPath));
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = fileSystem;
        }
        if (stats == null) {
            throw new IOException(String.format("could not list status for Directory %s\n", inPath));
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
        if (useLocal) {
            return localFileSystem.create(path, allowOveride, bufferSize, repCount, blockSize);
        } else {
            return fileSystem.create(path, allowOveride, bufferSize, repCount, blockSize);
        }
    }

    public FSDataInputStream openHdfsInputFile(String path, boolean useLocal) throws IOException {
        return openHdfsInputFile(new Path(path), useLocal);
    }

    public FSDataInputStream openHdfsInputFile(Path path, boolean useLocal) throws IOException {
        if (useLocal) {
            return localFileSystem.open(path, bufferSize);
        } else {
            return fileSystem.open(path, bufferSize);
        }
    }

    public FileOwner getFileOwner(String fileName, boolean useLocal) throws IOException {
        return getFileOwner(new Path(fileName), useLocal);
    }

    public FileOwner getFileOwner(Path path, boolean useLocal) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : fileSystem;
        FileStatus status = fs.getFileStatus(path);
        return new FileOwner(status.getOwner(), status.getGroup());
    }

    public FileOwner getDirectoryOwner(Path path, boolean useLocal) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : fileSystem;
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
            fs = fileSystem;
        }
        fs.mkdirs(new Path(dirPath));
    }
    
    public void mkDirs(String dirPath, FsPermission perms,boolean useLocal) throws IOException {
        FileSystem fs;
        if (useLocal) {
            fs = localFileSystem;
        } else {
            fs = fileSystem;
        }
        fs.mkdirs(new Path(dirPath),perms);
    }

    public FileOwner getDirectoryOwner(String fileName, boolean useLocal) throws IOException {
        return getDirectoryOwner(new Path(fileName), useLocal);
    }

    public void setFileOwner(String fileName, boolean useLocal, String user, String group) throws IOException {
        setFileOwner(new Path(fileName), useLocal, user, group);
    }

    public void setFileOwner(Path path, boolean useLocal, String user, String group) throws IOException {
        FileSystem fs = (useLocal) ? localFileSystem : fileSystem;
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
    }

    public String getUser() {
        return user;
    }

    public short getRepCount() {
        return repCount;
    }

    public void setRepCount(short repCount) {
        this.repCount = repCount;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public FileSystem getLocalFileSystem() {
        return localFileSystem;
    }

    public String getNameNode() {
        if (fileSystem == null) {
            return null;
        }

        Configuration fsConf = fileSystem.getConf();
        if (fsConf == null) {
            return null;
        }
        String nameNode = fsConf.get("fs.default.name", null);
        return nameNode;
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
}
