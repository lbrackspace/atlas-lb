package org.openstack.atlas.util;

import java.beans.XMLEncoder;
import com.hadoop.compression.lzo.LzoIndex;
import java.io.PrintStream;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileSystem.Statistics;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import com.hadoop.compression.lzo.LzopCodec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;

public class HdfsCli {

    private static final String HDUNAME = "HADOOP_USER_NAME";
    private static final int PAGESIZE = 4096;
    private static final int HDFSBUFFSIZE = 1024 * 64;
    private static final int ONEMEG = 1024 * 1024;
    private static List<String> jarFiles = new ArrayList<String>();
    private static URLClassLoader jobClassLoader = null;
    private static String jobJarName = "";

    public static void main(String[] argv) throws IOException, InterruptedException {
        System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
        HdfsUtils hdfsUtils = HadoopLogsConfigs.getHdfsUtils();
        String user = HadoopLogsConfigs.getHdfsUserName();
        String cmdLine;
        String args[];
        Configuration conf = HadoopLogsConfigs.getHadoopConfiguration();

        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);
        FileSystem fs = hdfsUtils.getFileSystem();
        System.setProperty(HDUNAME, user);
        FileSystem lfs = hdfsUtils.getLocalFileSystem();

        BufferedReader stdin = HdfsCliHelpers.inputStreamToBufferedReader(System.in);
        System.out.printf("\n");

        while (true) {
            try {
                System.out.printf("hdfs %s$ ", fs.getWorkingDirectory().toUri().toString());
                cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break; // EOF
                }
                args = stripBlankArgs(cmdLine);
                if (args.length < 1) {
                    System.out.printf("Usage is help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("\n");
                    System.out.printf("Usage is\n");
                    System.out.printf("help\n");
                    System.out.printf("chuser <userName>\n");
                    System.out.printf("whoami\n");
                    System.out.printf("mem\n");
                    System.out.printf("gc\n");
                    System.out.printf("exit\n");
                    System.out.printf("ls [path]\n");
                    System.out.printf("cat <path>\n");
                    System.out.printf("runMain <class> args0..N");
                    System.out.printf("runJob <jobDriverClass>");
                    System.out.printf("chmod <octPerms> <path>\n");
                    System.out.printf("chown <user> <group> <path>\n");
                    System.out.printf("mkdir <path>\n");
                    System.out.printf("rm <path>\n");
                    System.out.printf("rmdir <path>\n");
                    System.out.printf("homedir\n");
                    System.out.printf("cd <path>  #Change remote directory\n");
                    System.out.printf("pwd   #print remote current directory\n");
                    System.out.printf("cpfl <srcPath 1local> <dstPath remote> [reps] [blocksize]#copy from local\n");
                    System.out.printf("cptl <srcPath remote> <dstPath local> #Copy to Local\n");
                    System.out.printf("cpld <srcDir> <dstDir>  args [reps] [blocksize]\n");
                    System.out.printf("findCp <className> #find class path via reflection\n");
                    System.out.printf("showCl <className> #Show class loader info via reflection\n");
                    System.out.printf("setJobJar <jobJar> #set Jar file to classLoader\n");
                    System.out.printf("cpLocal <localSrc> <localDst> [buffsize] #None hadoop file copy\n");
                    System.out.printf("countLines <zeusFile> <nTicks> [buffSize]\n");
                    System.out.printf("indexLzo <FileName>\n");
                    System.out.printf("du #Get number of free space on HDFS\n");
                    System.out.printf("setReplCount <FilePath> <nReps> #Set the replication count for this file\n");
                    System.out.printf("compressLzo <srcPath> <dstPath> [buffSize]#Compress lzo file\n");
                    System.out.printf("dumpConfig <outFile.xml> <confIn.xml..> #Dump config to outfile\n");
                    System.out.printf("diffConfig <confA.xml> <confB.xml># Compare the differences between the configs\n");
                    System.out.printf("lineIndex <fileName> #Index the line numbers in the file\n");
                    System.out.printf("printLineNumber <fileName> <lineNumberN> #Print the line number based on the index file\n");
                    System.out.printf("\n");
                    continue;
                }
                if (cmd.equals("whoami")) {
                    System.out.printf("your supposed to be %s\n", user);
                    continue;
                }
                if (cmd.equals("chuser") && args.length >= 2) {
                    user = args[1];
                    fs = FileSystem.get(defaultHdfsUri, conf, user);
                    System.setProperty(HDUNAME, user);
                    System.out.printf("Switched to user %s\n", user);
                    continue;
                }
                if (cmd.equals("mem")) {
                    System.out.printf("Memory\n=================================\n%s\n", Debug.showMem());
                    continue;
                }
                if (cmd.equals("runJob") && args.length >= 2) {
                    Class<? extends Configured> jobDriverClass;

                    String jobDriverClassName = "com.rackspace.cloud.sum.hadoop.jobs." + args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("No jobJar set cannot load class searching class Path\n");
                        jobDriverClass = (Class<? extends Configured>) Class.forName(jobDriverClassName);
                    } else {
                        jobDriverClass = (Class<? extends Configured>) Class.forName(jobDriverClassName, true, jobClassLoader);
                    }
                    Configured jobDriver = jobDriverClass.newInstance();
                    jobDriver.setConf(conf);
                    String[] jobArgs = new String[args.length - 1];
                    for (int i = 0; i < args.length - 2; i++) {
                        jobArgs[i] = args[i + 2];
                    }
                    // Run job
                    double startTime = Debug.getEpochSeconds();
                    Method m = jobDriverClass.getMethod("run", String[].class);
                    Integer exitCode = (Integer) m.invoke(jobDriver, new Object[]{jobArgs});
                    //jobDriver.run(jobArgs);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("took %f seconds running job %s\n", endTime - startTime, jobDriverClassName);
                    System.out.printf("Exit status = %d\n", exitCode);
                    continue;
                }

                if (cmd.equals("runMain") && args.length >= 2) {
                    String className = args[1];
                    String[] mainArgs = new String[args.length - 2];
                    System.out.printf("Running %s\n", className);
                    for (int i = 0; i < args.length - 2; i++) {
                        mainArgs[i] = args[i + 2];
                    }
                    Class mainClass = Class.forName(args[1]);
                    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
                    mainMethod.invoke(null, (Object) mainArgs);
                    continue;
                }
                if (cmd.equals("gc")) {
                    System.out.printf("Calling garbage collector\n");
                    Debug.gc();
                    continue;
                }
                if (cmd.equals("ls")) {
                    Path path = (args.length >= 2) ? new Path(args[1]) : fs.getWorkingDirectory();
                    FileStatus[] files = fs.listStatus(path);
                    if (files == null) {
                        System.out.printf("Error got null when trying to retrieve file statuses\n");
                    }
                    for (int i = 0; i < files.length; i++) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(files[i]));
                    }
                    continue;
                }
                if (cmd.equals("exit")) {
                    break;
                }
                if (cmd.equals("cd") && args.length >= 2) {
                    Path path = new Path(args[1]);
                    fs.setWorkingDirectory(path);
                    continue;
                }
                if (cmd.equals("pwd")) {
                    System.out.printf("%s\n", fs.getWorkingDirectory().toUri().toString());
                    continue;
                }
                if (cmd.equals("cat") && args.length >= 2) {
                    String pathStr = args[1];
                    Path filePath = new Path(pathStr);
                    FSDataInputStream is = fs.open(filePath);
                    StaticFileUtils.copyStreams(is, System.out, null, PAGESIZE);
                    is.close();
                    continue;
                }
                if (cmd.equals("chmod") && args.length >= 3) {
                    String octMal = args[1];
                    Path path = new Path(args[2]);
                    short oct = (short) Integer.parseInt(octMal, 8);
                    fs.setPermission(path, new FsPermission(oct));
                    System.out.printf("Setting permisions on file %s\n", path.toUri().toString());
                    continue;
                }
                if (cmd.equals("chown") && args.length >= 4) {
                    String fUser = args[1];
                    String fGroup = args[2];
                    String fPath = args[3];
                    fs.setOwner(new Path(fPath), fUser, fGroup);
                    System.out.printf("Setting owner of %s to %s:%s\n", fPath, fUser, fGroup);
                    continue;
                }
                if (cmd.equals("mkdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.mkdirs(new Path(fPath));
                    System.out.printf("rm %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("rm") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), false);
                    System.out.printf("rm %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("rmdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), true);
                    System.out.printf("rmdir %s = %s\n", fPath, resp);
                    continue;
                }
                if (cmd.equals("homedir")) {
                    System.out.printf("%s\n", fs.getHomeDirectory().toUri().toString());
                    continue;
                }
                if (cmd.equals("cpld") && args.length >= 3) {
                    String inDirName = StaticFileUtils.expandUser(args[1]);
                    String outDir = args[2];
                    short nReplications = (args.length > 3) ? (short) Integer.parseInt(args[3]) : fs.getDefaultReplication();
                    long blockSize = (args.length > 4) ? Long.parseLong(args[4]) : fs.getDefaultBlockSize();
                    File inDir = new File(inDirName);
                    File[] files = inDir.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        String inFileName = file.getName();
                        String fullInPath = inDirName + "/" + inFileName;
                        if (file.isDirectory() || !file.isFile()) {
                            System.out.printf("Skipping %s since its not a file\n", fullInPath);
                            continue;
                        }
                        String fullOutPath = String.format("%s/%s", outDir, inFileName);
                        System.out.printf("Copying %s to %s\n", fullInPath, fullOutPath);
                        long fSize = new File(fullInPath).length();
                        InputStream is = StaticFileUtils.openDataInputStreamFile(fullInPath);
                        FSDataOutputStream os = fs.create(new Path(fullOutPath), true, HDFSBUFFSIZE, nReplications, blockSize);
                        StaticFileUtils.copyStreams(is, os, System.out, fSize, ONEMEG);
                        System.out.printf("Finished with file\n");
                        is.close();
                        os.close();
                        continue;
                    }
                    continue;
                }
                if (cmd.equals("cpfl") && args.length >= 3) {
                    String outPathStr = args[2];
                    String inPathStr = args[1];
                    long fSize = new File(StaticFileUtils.expandUser(inPathStr)).length();
                    InputStream is = StaticFileUtils.openInputFile(inPathStr);
                    FSDataOutputStream os;
                    short nReplications = (args.length > 3) ? (short) Integer.parseInt(args[3]) : fs.getDefaultReplication();
                    long blockSize = (args.length > 4) ? Long.parseLong(args[4]) : fs.getDefaultBlockSize();
                    System.out.printf("Copying with %d replications and blocksize of %d\n", nReplications, blockSize);
                    os = fs.create(new Path(outPathStr), true, HDFSBUFFSIZE, nReplications, blockSize);
                    StaticFileUtils.copyStreams(is, os, System.out, fSize, ONEMEG);
                    System.out.printf("copyed %s -> %s\n", inPathStr, outPathStr);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("cptl") && args.length >= 3) {
                    FSDataInputStream is = fs.open(new Path(args[1]), HDFSBUFFSIZE);
                    OutputStream os = StaticFileUtils.openOutputFile(args[2]);
                    StaticFileUtils.copyStreams(is, os, System.out, HDFSBUFFSIZE);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("findCp")) {
                    if (args.length >= 2) {
                        String className = args[1];
                        String classPath = Debug.findClassPath(className, jobClassLoader);
                        System.out.printf("%s classpath = %s\n", className, classPath);
                        continue;
                    }
                    String classPath = System.getProperties().getProperty("java.class.path");
                    System.out.printf("classpath = %s\n", classPath);
                    continue;
                }
                if (cmd.equals("setJobJar") && args.length >= 2) {
                    String jarName = StaticFileUtils.expandUser(args[1]);
                    if (jobClassLoader != null) {
                        System.out.printf("jobJar already set to %s\n", jobJarName);
                        continue;
                    }
                    File jarFile = new File(jarName).getAbsoluteFile();
                    if (!jarFile.canRead()) {
                        System.out.printf("Can't read file %s\n", jarFile.getAbsolutePath());
                        continue;
                    }
                    URL jarUrl = jarFile.toURI().toURL();
                    jobClassLoader = new URLClassLoader(new URL[]{jarUrl}, HdfsCli.class.getClassLoader());
                    System.out.printf("Loaded %s as jobJar\n", jarName);
                    continue;
                }
                if (cmd.equals("showCl") && args.length >= 2) {
                    String className = args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("jobJar not yet set\n");
                    }
                    Class classIn = Class.forName(className, true, jobClassLoader);
                    String classLoaderInfo = Debug.classLoaderInfo(className);
                    System.out.printf("%s\n", classLoaderInfo);
                    continue;
                }
                if (cmd.equals("countLines") && args.length >= 3) {
                    String fileName = args[1];
                    int nTicks = Integer.valueOf(args[2]);
                    int buffSize = (args.length > 3) ? Integer.valueOf(args[3]) : PAGESIZE * 4;
                    System.out.printf("Counting the lines from file %s with %d ticks", fileName, nTicks);
                    double startTime = Debug.getEpochSeconds();
                    long nLines = HdfsCliHelpers.countLines(fileName, nTicks, buffSize);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to count %d lines\n", endTime - startTime, nLines);
                    continue;
                }


                if (cmd.equals("compressLzo") && args.length >= 3) {
                    String srcFileName = args[1];
                    String dstFileName = args[2];
                    int buffsize = (args.length >= 5) ? Integer.parseInt(args[4]) : 4096;
                    InputStream fis = StaticFileUtils.openInputFile(srcFileName);
                    OutputStream fos = StaticFileUtils.openOutputFile(dstFileName);
                    System.out.printf("Attempting to compress %s to file %s\n", srcFileName, dstFileName);
                    LzopCodec codec = new LzopCodec();
                    codec.setConf(conf);
                    CompressionOutputStream cos = codec.createOutputStream(fos);
                    double startTime = Debug.getEpochSeconds();
                    StaticFileUtils.copyStreams(fis, cos, System.out, 1024 * 1024 * 64);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Compression took %f seconds\n", endTime - startTime);
                    fis.close();
                    cos.finish();
                    cos.close();
                    fos.close();
                    continue;
                }
                if (cmd.equals("indexLzo") && args.length >= 2) {
                    String srcFileName = args[1];
                    Path filePath = new Path(StaticFileUtils.expandUser(srcFileName));
                    System.out.printf("Indexing file %s\n", srcFileName);
                    double startTime = Debug.getEpochSeconds();
                    LzoIndex.createIndex(lfs, filePath);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to index file %s\n", endTime - startTime, srcFileName);
                    continue;
                }
                if (cmd.equals("du")) {
                    long used = fs.getUsed();
                    System.out.printf("Used bytes: %s\n", Debug.humanReadableBytes(used));
                    continue;
                }
                if (cmd.equals("setReplCount") && args.length >= 3) {
                    String fileName = args[1];
                    Path filePath = new Path(fileName);
                    short replCount = Short.parseShort(args[2]);
                    System.out.printf("Setting Replication count for file %s to %d\n", fileName, replCount);
                    fs.setReplication(filePath, replCount);
                    continue;
                }
                if (cmd.equals("dumpConfig") && args.length >= 2) {
                    System.out.printf("Dumping configs\n");
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(StaticFileUtils.expandUser(args[1]))), HDFSBUFFSIZE);
                    Configuration dumpConf = new Configuration();
                    for (int i = 2; i < args.length; i++) {
                        dumpConf.addResource(new Path(StaticFileUtils.expandUser(args[i])));
                    }
                    dumpConf.writeXml(bos);
                    bos.close();
                    dumpConf.writeXml(System.out);
                    continue;
                }
                if (cmd.equals("lineIndex") && args.length >= 2) {
                    String inFileName = StaticFileUtils.expandUser(args[1]);
                    String outFileName = inFileName + ".idx";
                    InputStream is = StaticFileUtils.openInputFile(inFileName);
                    DataOutputStream os = StaticFileUtils.openDataOutputStreamFile(outFileName);
                    System.out.printf("Indexling file %s -> %s\n", inFileName, outFileName);
                    HdfsCliHelpers.indexFile(is, os, PAGESIZE * 8);
                    is.close();
                    os.close();
                    continue;
                }
                if (cmd.equals("readLineNumber") && args.length > 3) {
                    String inFileName = StaticFileUtils.expandUser(args[1]);
                    String outFileName = inFileName + ".idx";
                    InputStream is = StaticFileUtils.openInputFile(inFileName);
                    DataInputStream dis = StaticFileUtils.openDataInputStreamFile(inFileName, PAGESIZE * 8);
                    System.out.printf("Printing line number\n");
                    String lineStr = HdfsCliHelpers.printLineNumber(is, dis, PAGESIZE * 8);
                    continue;
                }
                if (cmd.equals("cpLocal") && args.length >= 3) {
                    String inFileName = StaticFileUtils.expandUser(args[1]);
                    String outFileName = StaticFileUtils.expandUser(args[2]);
                    int buffSize = (args.length >= 4) ? Integer.parseInt(args[3]) : ONEMEG;
                    System.out.printf("Copy %s -> %s with a buffer of %d\n", inFileName, outFileName, buffSize);
                    long inputFileLength = new File(inFileName).length();
                    InputStream fis = new FileInputStream(new File(inFileName));
                    OutputStream fos = new FileOutputStream(new File(outFileName));
                    StaticFileUtils.copyStreams(fis, fos, System.out, inputFileLength, buffSize);
                    fis.close();
                    fos.close();
                    continue;
                }
                continue;
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", StaticStringUtils.getExtendedStackTrace(ex));
            }
        }
        System.out.printf("Exiting\n");
    }

    private static String chop(String line) {
        return line.replace("\r", "").replace("\n", "");
    }

    private static String[] stripBlankArgs(String line) {
        int nargs = 0;
        int i;
        int j;
        String[] argsIn = line.replace("\r", "").replace("\n", "").split(" ");
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                nargs++;
            }
        }
        String[] argsOut = new String[nargs];
        j = 0;
        for (i = 0; i < argsIn.length; i++) {
            if (argsIn[i].length() > 0) {
                argsOut[j] = argsIn[i];
                j++;
            }
        }
        return argsOut;
    }
}
