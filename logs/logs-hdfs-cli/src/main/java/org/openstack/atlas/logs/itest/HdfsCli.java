package org.openstack.atlas.logs.itest;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.logs.hadoop.sequencefiles.SequenceFileReaderException;
import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.config.HadoopLogsConfigs;
import com.hadoop.compression.lzo.LzoIndex;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
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
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.logs.hadoop.jobs.HadoopJob;
import org.openstack.atlas.logs.hadoop.jobs.HadoopLogSplitterJob;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;
import org.openstack.atlas.logs.hadoop.writables.LogReducerOutputValue;
import org.openstack.atlas.util.debug.Debug;
import org.joda.time.DateTime;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.logs.hadoop.util.LogChopper;

public class HdfsCli {

    private static final Pattern zipPattern = Pattern.compile(".*\\.zip$");

    private static final int LARGEBUFFERSIZE = 8 * 1024 * 1024;
    private static final int PAGESIZE = 4096;
    private static final int HDFSBUFFSIZE = 512 * 1024;
    private static final int ONEMEG = 1024 * 1024;
    private static final int BUFFER_SIZE = 256 * 1024;
    private static final String LB_LOGS_SPLIT = "lb_logs_split";
    private static List<String> jarFiles = new ArrayList<String>();
    private static URLClassLoader jobClassLoader = null;
    private static String jobJarName = "";

    public static void main(String[] argv) throws IOException, InterruptedException {
        System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
        String cmdLine;
        String[] args;
        if (argv.length >= 1) {
            System.out.printf("Useing confFile %s\n", argv[0]);
            HadoopLogsConfigs.resetConfigs(argv[0]);
        } else {
            System.out.printf("useing confFile %s\n", LbLogsConfiguration.defaultConfigurationLocation);
        }
        HdfsUtils hdfsUtils = HadoopLogsConfigs.getHdfsUtils();
        String user = HadoopLogsConfigs.getHdfsUserName();
        Configuration conf = HadoopLogsConfigs.getHadoopConfiguration();
        HadoopLogsConfigs.markJobsJarAsAlreadyCopied();
        URI defaultHdfsUri = FileSystem.getDefaultUri(conf);
        FileSystem fs = hdfsUtils.getFileSystem();
        System.setProperty(CommonItestStatic.HDUNAME, user);
        FileSystem lfs = hdfsUtils.getLocalFileSystem();

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in,BUFFER_SIZE);
        System.out.printf("\n");

        List<WastedBytesBlock> wastedBlocks = new ArrayList<WastedBytesBlock>();


        while (true) {
            try {
                System.out.printf("lbaas_hadoop_client %s> ", fs.getWorkingDirectory().toUri().toString());
                cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break; // EOF
                }
                args = CommonItestStatic.stripBlankArgs(cmdLine);
                if (args.length < 1) {
                    System.out.printf("Usage is help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("\n");
                    System.out.printf("Usage is\n");
                    System.out.printf("help\n");
                    System.out.printf("cat <path>\n");
                    System.out.printf("classInfo <classPath>\n");
                    System.out.printf("cd <path>  #Change remote directory\n");
                    System.out.printf("cdin [dateKey] #Change to the input directory\n");
                    System.out.printf("cdout[dateKey] #Change to the output directory\n");
                    System.out.printf("chmod <octPerms> <path>\n");
                    System.out.printf("chown <user> <group> <path>\n");
                    System.out.printf("chuser <userName>\n");
                    System.out.printf("compressLzo <srcPath> <dstFile> [buffSize]#Compress lzo file\n");
                    System.out.printf("countLines <zeusFile> <nTicks> [buffSize]\n");
                    System.out.printf("cpfl <srcPath 1local> <dstPath remote> [reps] [blocksize]#copy from local\n");
                    System.out.printf("cpld <srcDir> <dstDir>  args [reps] [blocksize]\n");
                    System.out.printf("cpLocal <localSrc> <localDst> [buffsize] #None hadoop file copy\n");
                    System.out.printf("cptl <srcPath remote> <dstPath local> #Copy to Local\n");
                    System.out.printf("cpjj #Copy the jobs jar\n");
                    System.out.printf("cpjjf #Mark the jar as already copied\n");
                    System.out.printf("diffConfig <confA.xml> <confB.xml># Compare the differences between the configs\n");
                    System.out.printf("du #Get number of free space on HDFS\n");
                    System.out.printf("dumpConfig <outFile.xml> <confIn.xml..> #Dump config to outfile\n");
                    System.out.printf("exit\n");
                    System.out.printf("findCp <className> #find class path via reflection\n");
                    System.out.printf("gc\n");
                    System.out.printf("getlzo <DownloadDir> <dateHour> #Download the Lzo for the given hour\n");
                    System.out.printf("getzip <DownloadDir> <h=hourKey> <l=LoadbalancerId> #Download the zip file from Hdfs for the specifie day and loadbalancer\n");
                    System.out.printf("homedir\n");
                    System.out.printf("indexLzo <FileName>\n");
                    System.out.printf("joinPath <path1> ...<pathN> #Test the join the paths together skipping double slashes.\n");
                    System.out.printf("lineIndex <fileName> #Index the line numbers in the file\n");
                    System.out.printf("lslzo [hourKey] #List the lzos in the input directory\n");
                    System.out.printf("ls [path] #List hdfs files\n");
                    System.out.printf("lsin [hourKey]#List the hourkeys in the input directory usefull because ls prints long form\n");
                    System.out.printf("lsout #List the hourKeys in the output directory usefull because ls prints long form\n");
                    System.out.printf("lsr [path] #List hdfs files recursivly\n");
                    System.out.printf("lszip [l=lid] [h=hour] [m=missing]#List all zip files in the HDFS ourput directory for hourh or and the given lid\n");
                    System.out.printf("dlzip (<hourkey>|<startHour> <endHour>) [l=lid] [a=accoundId] #Download all zip files in local cache directory for the given keys\n");
                    System.out.printf("ullzo <file> #Upload the lzo file to hdfs\n");
                    System.out.printf("mem\n");
                    System.out.printf("mkdir <path>\n");
                    System.out.printf("printReducers <hdfsDir> #Display the contents of the reducer output\n");
                    System.out.printf("pwd   #print remote current directory\n");
                    System.out.printf("rebasePath <srcBase> <srcPath> <dstPath> #Show what the rebasePath method in StaticFileUtils would do\n");
                    System.out.printf("recompressIndex <srcFile> <hdfsDstFile> #Recompress and index lzo file and upload to hdfs\n");
                    System.out.printf("rmdir <path>\n");
                    System.out.printf("rmin (<hourKey>|<startHour> <endHour>) #Remove the input directories for the specified hours\n");
                    System.out.printf("rmout (<hourKey>|<startHour> <endHour>) #Remove the output directories for the specified hours\n");
                    System.out.printf("rm <path>\n");
                    System.out.printf("runJob <jobDriverClass>\n");
                    System.out.printf("runSplit <hourKey> #Run the HadoopSplitterJob for the specified hourkey\n");
                    System.out.printf("runMain <class> args0..N\n");
                    System.out.printf("uploadLzo <lzoFile> #Upload the the lzo file\n");
                    System.out.printf("scanLines <logFile> <nLines> <nTicks>\n");
                    System.out.printf("scanhdfszips <yyyymmddhh> <yyyymmddhh> [scanparts=<true|false>]#Scan the hadoop output directories and count how many zips where found between the 2 days\n");
                    System.out.printf("setJobJar <jobJar> #set Jar file to classLoader\n");
                    System.out.printf("setReplCount <FilePath> <nReps> #Set the replication count for this file\n");
                    System.out.printf("spon #Enable speculative execution\n");
                    System.out.printf("spoff #Disable speculative execution\n");
                    System.out.printf("showCl <className> #Show class loader info via reflection\n");
                    System.out.printf("showConfig #Show hadoop configs\n");
                    System.out.printf("showCrc <fileName> #Show crc value that would be reported by Zip\n");
                    System.out.printf("wb <size> #Wast nbytes to experiment with the Garbage colector\n");
                    System.out.printf("fb #Free all bytes wasted so far");
                    System.out.printf("wbs #List the number of bytes in the wasted byte Cuffer\n");
                    System.out.printf("whoami\n");
                } else if (cmd.equals("cpjjf")) {
                    System.out.printf("Marking the jobs jar as already copied\n");
                    HadoopLogsConfigs.markJobsJarAsAlreadyCopied();
                } else if (cmd.equals("spoff")) {
                    System.out.printf("Attempting to disable speculative execution\n");
                    Configuration editConf;
                    editConf = HadoopLogsConfigs.getHadoopConfiguration();
                    editConf.setBoolean("mapred.reduce.tasks.speculative.execution", false);
                    editConf.setBoolean("mapred.map.tasks.speculative.execution", false);
                    HadoopLogsConfigs.setHadoopConfiguration(editConf);
                } else if (cmd.equals("spon")) {
                    System.out.printf("Attempting to enable speculative execution\n");
                    Configuration editConf;
                    editConf = HadoopLogsConfigs.getHadoopConfiguration();
                    editConf.setBoolean("mapred.reduce.tasks.speculative.execution", true);
                    editConf.setBoolean("mapred.map.tasks.speculative.execution", true);
                    HadoopLogsConfigs.setHadoopConfiguration(editConf);
                } else if (cmd.equals("cpjj")) {
                    System.out.printf("Attempting to Copy jobs jar\n");
                    HadoopLogsConfigs.copyJobsJar();
                    System.out.printf("JobJar copied.\n");
                } else if (cmd.equals("wbs")) {
                    long totalWastedBytes = 0L;
                    for (WastedBytesBlock wastedBlock : wastedBlocks) {
                        totalWastedBytes += wastedBlock.size();
                    }
                    System.out.printf("Total wasted bytes: %d\n", totalWastedBytes);
                } else if (cmd.equals("wb") && args.length >= 2) {
                    int size = Integer.parseInt(args[1]);
                    double startTime = Debug.getEpochSeconds();
                    wastedBlocks.add(new WastedBytesBlock(size));
                    double stopTime = Debug.getEpochSeconds();
                    double delta = stopTime - startTime;
                    double rate = (double) size / delta;
                    String fmt = "Took %f seconds to wast %d bytes at a rate of %s bytes persecond\n";
                    System.out.printf(fmt, delta, size, Debug.humanReadableBytes(rate));
                } else if (cmd.equals("fb")) {
                    wastedBlocks = new ArrayList<WastedBytesBlock>();
                } else if (cmd.equals("classInfo") && args.length >= 2) {
                    String className = args[1];
                    System.out.printf("Looking up classinfo for %s\n", className);
                    String classInfo = Debug.classLoaderInfo(className);
                    System.out.printf("Class Info:\n%s\n", classInfo);
                } else if (cmd.equals("lsin")) {
                    String inputDir = HadoopLogsConfigs.getMapreduceInputPrefix();
                    String hourKey = (args.length >= 2) ? args[1] : null;
                    String fileDisplay = listHourKeyFiles(hdfsUtils, inputDir, hourKey);
                    System.out.printf("%s\n", fileDisplay);
                } else if (cmd.equals("lsout")) {
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    pathComps.add(LB_LOGS_SPLIT);
                    String outputDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
                    String hourKey = (args.length >= 2) ? args[1] : null;
                    String fileDisplay = listHourKeyFiles(hdfsUtils, outputDir, hourKey);
                    System.out.printf("%s\n", fileDisplay);
                } else if (cmd.equals("scanhdfszips")) {
                    Map<String, String> kw = CommonItestStatic.argMapper(args);
                    args = CommonItestStatic.stripKwArgs(args);
                    List<Long> hourKeysListL = new ArrayList<Long>();
                    String lbLogSplitDir = StaticFileUtils.mergePathString(HadoopLogsConfigs.getMapreduceOutputPrefix(), LB_LOGS_SPLIT);
                    FileStatus[] dateDirsStats = hdfsUtils.getFileSystem().listStatus(new Path(lbLogSplitDir));
                    for (FileStatus fileStatus : dateDirsStats) {
                        Long hourLong;
                        String pathStr;
                        try {
                            pathStr = pathTailString(fileStatus);
                            hourLong = Long.parseLong(pathStr);
                        } catch (Exception ex) {
                            continue;
                        }
                        hourKeysListL.add(hourLong);

                    }

                    Collections.sort(hourKeysListL);
                    DateTime startDt;
                    if (args.length > 2) {
                        startDt = StaticDateTimeUtils.hourKeyToDateTime(args[1], false);
                    } else {
                        startDt = StaticDateTimeUtils.hourKeyToDateTime(hourKeysListL.get(0), false);
                    }
                    DateTime endDt;
                    if (args.length > 3) {
                        endDt = StaticDateTimeUtils.hourKeyToDateTime(args[2], false);
                    } else {
                        endDt = StaticDateTimeUtils.hourKeyToDateTime(hourKeysListL.get(hourKeysListL.size() - 1), false);
                    }
                    DateTime curDt = new DateTime(startDt);
                    String fmt = "Scanning for zips in date range (%d,%d)\n";
                    System.out.printf(fmt, StaticDateTimeUtils.dateTimeToHourLong(startDt), StaticDateTimeUtils.dateTimeToHourLong(endDt));
                    System.out.printf("Press Enter to continue\n");
                    stdin.readLine();
                    hourKeysListL = new ArrayList<Long>();
                    Map<String, HdfsZipDirScan> zipDirMap = new HashMap<String, HdfsZipDirScan>();
                    boolean scanParts = false;
                    if (kw.containsKey("scanparts") && kw.get("scanparts").equalsIgnoreCase("true")) {
                        scanParts = true;
                    }

                    while (true) {
                        if (curDt.isAfter(endDt)) {
                            break;
                        }
                        Long hourKeyL = StaticDateTimeUtils.dateTimeToHourLong(curDt);
                        hourKeysListL.add(hourKeyL);
                        curDt = curDt.plusHours(1);
                    }
                    Collections.sort(hourKeysListL);
                    System.out.printf("scanning directorys:\n");
                    System.out.flush();
                    for (Long hourKeyL : hourKeysListL) {
                        System.out.printf(" %d", hourKeyL);
                        System.out.flush();
                        String key = hourKeyL.toString();
                        HdfsZipDirScan val = scanHdfsZipDirs(hdfsUtils, key, scanParts);
                        zipDirMap.put(key, val);
                    }
                    System.out.printf("\n");
                    for (Long hourKey : hourKeysListL) {
                        String key = hourKey.toString();
                        HdfsZipDirScan val = zipDirMap.get(key);
                        System.out.printf("%s ", val.displayString());
                        if (scanParts) {
                            Set<String> missingSet = new HashSet<String>(val.getZipsFound());
                            missingSet.removeAll(val.getZipsFound());
                            System.out.printf("found %s files in partitions but missing %d files", val.getZipsFound().size(), missingSet.size());
                        }
                        if (!val.isDateDirFound() || !val.isZipDirFound()) {
                            System.out.printf(" ******************\n");
                        } else {
                            System.out.printf("\n");
                        }
                    }


                } else if (cmd.equals("ullzo") && args.length >= 2) {
                    String localLzoFilePath = StaticFileUtils.expandUser(args[1]);
                    String localLzoFile = StaticFileUtils.pathTail(localLzoFilePath);
                    Matcher m = HdfsUtils.hdfsLzoPatternPre.matcher(localLzoFile);
                    if (!m.find()) {
                        System.out.printf("%s doesn't look like a properly name lzo file", localLzoFilePath);
                        continue;
                    }
                    String hourKey = m.group(1);

                    // upload the lzo file
                    List<String> hdfsLzoPathComps = new ArrayList<String>();
                    hdfsLzoPathComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
                    hdfsLzoPathComps.add(hourKey);
                    hdfsLzoPathComps.add("0-" + hourKey + "-access_log.aggregated.lzo");
                    String hdfsLzoPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(hdfsLzoPathComps));
                    String hdfsLzoIdxPath = hdfsLzoPath + ".index";

                    // Verify the user wants to upload this file
                    System.out.printf("Are you sure you want to upload %s to %s with index %s(Y/N)\n", localLzoFilePath, hdfsLzoPath, hdfsLzoIdxPath);
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        System.out.printf("Uploading lzo\n");
                    } else {
                        System.out.printf("Not uploading lzo\n");
                        continue;
                    }

                    Configuration codecConf = new Configuration();
                    codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
                    codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
                    LzopCodec codec = new LzopCodec();
                    codec.setConf(codecConf);
                    System.out.printf("Uploading lzo %s to %s with idx file %s\n", localLzoFile, hdfsLzoPath, hdfsLzoIdxPath);
                    InputStream lzoIs = StaticFileUtils.openInputFile(localLzoFilePath, BUFFER_SIZE);
                    OutputStream lzoOs = hdfsUtils.openHdfsOutputFile(hdfsLzoPath, false, false);
                    FSDataOutputStream lzoIdx = hdfsUtils.openHdfsOutputFile(hdfsLzoIdxPath, false, false);
                    CompressionInputStream cis = codec.createInputStream(lzoIs);
                    CompressionOutputStream cos = codec.createIndexedOutputStream(lzoOs, lzoIdx);
                    StaticFileUtils.copyStreams(cis, cos, null, BUFFER_SIZE);
                    cos.flush();
                    cos.finish();
                    StaticFileUtils.close(cis);
                    StaticFileUtils.close(cos);
                    StaticFileUtils.close(lzoIs);
                    StaticFileUtils.close(lzoOs);
                    StaticFileUtils.close(lzoIdx);

                } else if (cmd.equals("runSplit") && args.length >= 2) {
                    HadoopLogsConfigs.copyJobsJar();
                    String hourKey = args[1];

                    // Setup Inputfile based on hourKey
                    List<String> hdfsLzoPathComps = new ArrayList<String>();
                    hdfsLzoPathComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
                    hdfsLzoPathComps.add(hourKey);
                    hdfsLzoPathComps.add("0-" + hourKey + "-access_log.aggregated.lzo");
                    String hdfsLzoPath = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(hdfsLzoPathComps));

                    // Setup outputdir
                    List<String> outDirComps = new ArrayList<String>();
                    outDirComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    outDirComps.add(LB_LOGS_SPLIT);
                    outDirComps.add(hourKey);
                    String outDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(outDirComps));

                    List<String> logSplitArgs = new ArrayList<String>();
                    logSplitArgs.add(HadoopLogsConfigs.getHdfsJobsJarPath());
                    logSplitArgs.add(outDir);
                    logSplitArgs.add("");
                    logSplitArgs.add(hourKey);
                    logSplitArgs.add(HadoopLogsConfigs.getNumReducers());
                    logSplitArgs.add(HadoopLogsConfigs.getHdfsUserName());
                    logSplitArgs.add(hdfsLzoPath);
                    HadoopJob hadoopClient = new HadoopLogSplitterJob();
                    System.out.printf("Calling HadoopLogSplitterJob with args:\n");
                    for (int i = 0; i < logSplitArgs.size(); i++) {
                        System.out.printf("   arg[%d] = \"%s\"\n", i, logSplitArgs.get(i));
                    }
                    hadoopClient.setConfiguration(HadoopLogsConfigs.getHadoopConfiguration());
                    int errorCode = hadoopClient.run(logSplitArgs);  // Actually runs the Hadoop Job
                    System.out.printf("Hadoop tun response code was %d\n", errorCode);

                } else if (cmd.equals("getzip") && args.length > 1) {
                    Map<String, String> kw = CommonItestStatic.argMapper(args);
                    String lid = (kw.containsKey("l")) ? kw.get("l") : null;
                    String hourKey = (kw.containsKey("h")) ? kw.get("h") : null;
                    String downloadDir = args[1];
                    List<FileStatus> zipStatusList = hdfsUtils.listHdfsZipsStatus(hourKey, lid, false);
                    System.out.printf("Attempting to fetch zipfiles\n");
                    for (FileStatus zipFileStatus : zipStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(zipFileStatus));
                    }
                    System.out.printf("Are you sure you want to download the above files (Y/N)?");
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        for (FileStatus zipFileStatus : zipStatusList) {
                            String hdfsZipFileStr = zipFileStatus.getPath().toUri().getRawPath();
                            String dstZipFileStr = StaticFileUtils.joinPath(downloadDir, StaticFileUtils.pathTail(hdfsZipFileStr));
                            System.out.printf("Downloading %s to %s\n", zipFileStatus.getPath().toUri().toString(), dstZipFileStr);
                            InputStream is = hdfsUtils.openHdfsInputFile(zipFileStatus.getPath(), false);
                            OutputStream os = StaticFileUtils.openOutputFile(dstZipFileStr, BUFFER_SIZE);
                            StaticFileUtils.copyStreams(is, os, System.out, BUFFER_SIZE);
                            is.close();
                            os.close();
                        }
                    }
                } else if (cmd.equals("getlzo") && args.length > 2) {
                    String downloadDir = args[1];
                    String dateHour = args[2];
                    System.out.printf("Searching for lzo files matching %s\n", dateHour);
                    List<FileStatus> lzoFileStatusList = hdfsUtils.listHdfsLzoStatus(dateHour);
                    System.out.printf("Attempting to download lzos\n");
                    for (FileStatus lzoFileStatus : lzoFileStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(lzoFileStatus));
                    }
                    System.out.printf("Are you sure you want to download the lzo files above?(Y/N)?");
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        for (FileStatus lzoFileStatus : lzoFileStatusList) {
                            String srcLzoFileStr = StaticFileUtils.pathTail(lzoFileStatus.getPath().toUri().getRawPath());
                            Matcher m = HdfsUtils.hdfsLzoPattern.matcher(srcLzoFileStr);
                            if (!m.find()) {
                                System.out.printf("Error srcFile %s didn't match expected LZO file name");
                                continue;
                            }
                            String dstFileName = m.group(1) + "-access_log.aggregated.lzo";
                            String dstFilePath = StaticFileUtils.joinPath(downloadDir, dstFileName);
                            System.out.printf("Downloading %s to %s\n", lzoFileStatus.getPath().toUri().toString(), dstFilePath);
                            InputStream is = hdfsUtils.openHdfsInputFile(lzoFileStatus.getPath(), false);
                            OutputStream os = StaticFileUtils.openOutputFile(dstFilePath, BUFFER_SIZE);
                            StaticFileUtils.copyStreams(is, os, System.out, BUFFER_SIZE);
                            is.close();
                            os.close();
                        }
                    }
                } else if (cmd.equals("showConfig")) {
                    System.out.printf("HadoopLogsConfig=%s\n", HadoopLogsConfigs.staticToString());
                    System.out.printf("Hdfs workingDir = %s\n", fs.getWorkingDirectory().toUri().getRawPath().toString());
                    System.out.printf("Local workingDir = %s\n", lfs.getWorkingDirectory());

                } else if (cmd.equals("recompressIndex") && args.length >= 3) {
                    String srcLzo = StaticFileUtils.expandUser(args[1]);
                    String dstLzo = args[2];
                    String dstIdx = dstLzo + ".index";
                    FileInputStream lzoInputStream = new FileInputStream(srcLzo);
                    FSDataOutputStream dstLzoStream = hdfsUtils.openHdfsOutputFile(dstLzo, false, true);
                    FSDataOutputStream dstIdxStream = hdfsUtils.openHdfsOutputFile(dstIdx, false, true);
                    hdfsUtils.recompressAndIndexLzoStream(lzoInputStream, dstLzoStream, dstIdxStream, null);
                    System.out.printf("Recompressed and sent\n");
                    lzoInputStream.close();
                    dstLzoStream.close();
                    dstIdxStream.close();
                } else if (cmd.equals("whoami")) {
                    System.out.printf("your supposed to be %s\n", user);
                } else if (cmd.equals("chuser") && args.length >= 2) {
                    user = args[1];
                    fs = FileSystem.get(defaultHdfsUri, conf, user);
                    System.setProperty(CommonItestStatic.HDUNAME, user);
                    System.out.printf("Switched to user %s\n", user);
                } else if (cmd.equals("mem")) {
                    System.out.printf("Memory\n=================================\n%s\n", Debug.showMem());
                } else if (cmd.equals("runJob") && args.length >= 2) {
                    Class<? extends HadoopJob> jobDriverClass;

                    String jobDriverClassName = "org.openstack.atlas.logs.hadoop.jobs." + args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("No jobJar set cannot load class searching class Path\n");
                        jobDriverClass = (Class<? extends HadoopJob>) Class.forName(jobDriverClassName);
                    } else {
                        jobDriverClass = (Class<? extends HadoopJob>) Class.forName(jobDriverClassName, true, jobClassLoader);
                    }
                    HadoopJob jobDriver = jobDriverClass.newInstance();
                    jobDriver.setConfiguration(conf);
                    List<String> argsList = new ArrayList<String>();
                    for (int i = 2; i < args.length; i++) {
                        argsList.add(args[i]);
                    }
                    // Run job
                    double startTime = Debug.getEpochSeconds();
                    int exitCode = jobDriver.run(argsList);
                    //jobDriver.run(jobArgs);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("took %f seconds running job %s\n", endTime - startTime, jobDriverClassName);
                    System.out.printf("Exit status = %d\n", exitCode);
                } else if (cmd.equals("runMain") && args.length >= 2) {
                    String className = args[1];
                    String[] mainArgs = new String[args.length - 2];
                    System.out.printf("Running %s\n", className);
                    for (int i = 0; i < args.length - 2; i++) {
                        mainArgs[i] = args[i + 2];
                    }
                    Class mainClass = Class.forName(args[1]);
                    Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
                    mainMethod.invoke(null, (Object) mainArgs);
                } else if (cmd.equals("gc")) {
                    System.out.printf("Calling garbage collector\n");
                    Debug.gc();
                } else if (cmd.equals("lszip")) {
                    Map<String, String> kw = CommonItestStatic.argMapper(args);
                    String dateHour = (kw.containsKey("h")) ? kw.get("h") : null;
                    String lid = (kw.containsKey("l")) ? kw.get("l") : null;
                    System.out.printf("Scanning for zips on hour[%s] lid[%s]\n", dateHour, lid);
                    boolean onlyMissing = (kw.containsKey("m")) ? true : false;

                    List<FileStatus> zipStatusList = hdfsUtils.listHdfsZipsStatus(dateHour, lid, onlyMissing);
                    for (FileStatus zipStatus : zipStatusList) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(zipStatus));
                    }
                } else if (cmd.equals("lslzo")) {
                    String hourKey = (args.length >= 2) ? args[1] : null;
                    System.out.printf("Scanning lzos for hour[%s]\n", hourKey);
                    List<FileStatus> lzoFiles = hdfsUtils.listHdfsLzoStatus(hourKey);
                    for (FileStatus lzoFileStatus : lzoFiles) {
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(lzoFileStatus));
                    }

                } else if (cmd.equals("ls")) {
                    long total_file_size = 0;
                    long total_repl_size = 0;
                    Path path = (args.length >= 2) ? new Path(args[1]) : fs.getWorkingDirectory();
                    FileStatus[] fileStatusList = fs.listStatus(path);
                    if (fileStatusList == null) {
                        System.out.printf("Error got null when trying to retrieve file statuses\n");
                    }
                    for (FileStatus fileStatus : fileStatusList) {
                        total_file_size += fileStatus.getLen();
                        total_repl_size += fileStatus.getLen() * fileStatus.getReplication();
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(fileStatus));
                    }
                    System.out.printf("Total file bytes: %s\n", Debug.humanReadableBytes(total_file_size));
                    System.out.printf("Total file bytes including replication: %s\n", Debug.humanReadableBytes(total_repl_size));
                    System.out.printf("Total file count: %d\n", fileStatusList.length);
                } else if (cmd.equals("rmin") && args.length >= 2) {
                    List<String> hourDirs = new ArrayList<String>();
                    if (args.length >= 3) {
                        long startHour = Long.parseLong(args[1]);
                        long stopHour = Long.parseLong(args[2]);
                        String inDir = HadoopLogsConfigs.getMapreduceInputPrefix();
                        FileStatus[] stats = hdfsUtils.getFileSystem().listStatus(new Path(inDir));
                        for (FileStatus stat : stats) {
                            String hourKey = pathTailString(stat.getPath());
                            if (!stat.isDir()) {
                                continue;
                            }
                            try {
                                long currHour = Long.parseLong(hourKey);
                                if (currHour >= startHour && currHour <= stopHour) {
                                    hourDirs.add(StaticFileUtils.mergePathString(inDir, hourKey));
                                }
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                        }
                    } else {
                        hourDirs.add(args[1]);
                    }

                    Collections.sort(hourDirs);
                    for (String hourDir : hourDirs) {
                        System.out.printf("Deleting %s\n", hourDir);
                    }
                    System.out.printf("Are you sure you want to delete the above %d directories (Y/N))", hourDirs.size());
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        for (String hourDir : hourDirs) {
                            boolean resp = fs.delete(new Path(hourDir), true);
                            System.out.printf("%s = %s\n", hourDir, resp);
                        }
                    }
                } else if (cmd.equals("rmout") && args.length >= 2) {
                    List<String> hourDirs = new ArrayList<String>();
                    if (args.length >= 3) {
                        long startHour = Long.parseLong(args[1]);
                        long stopHour = Long.parseLong(args[2]);
                        String outDir = StaticFileUtils.mergePathString(HadoopLogsConfigs.getMapreduceOutputPrefix(), LB_LOGS_SPLIT);
                        FileStatus[] stats = hdfsUtils.getFileSystem().listStatus(new Path(outDir));
                        for (FileStatus stat : stats) {
                            String hourKey = pathTailString(stat.getPath());
                            if (!stat.isDir()) {
                                continue;
                            }
                            try {
                                long currHour = Long.parseLong(hourKey);
                                if (currHour >= startHour && currHour <= stopHour) {
                                    hourDirs.add(StaticFileUtils.mergePathString(outDir, hourKey));
                                }
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                        }
                    } else {
                        hourDirs.add(args[1]);
                    }

                    Collections.sort(hourDirs);
                    for (String hourDir : hourDirs) {
                        System.out.printf("Deleting %s\n", hourDir);
                    }
                    System.out.printf("Are you sure you want to delete the above %d directories (Y/N))", hourDirs.size());
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        for (String hourDir : hourDirs) {
                            boolean resp = fs.delete(new Path(hourDir), true);
                            System.out.printf("%s = %s\n", hourDir, resp);
                        }
                    }
                } else if (cmd.equals("dlzip") && args.length >= 2) {
                    Map<String, String> kw = CommonItestStatic.argMapper(args);
                    args = CommonItestStatic.stripKwArgs(args);

                    Integer lid = (kw.containsKey("l")) ? Integer.valueOf(kw.get("l")) : null;
                    Integer aid = (kw.containsKey("a")) ? Integer.valueOf(kw.get("a")) : null;
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    pathComps.add(LB_LOGS_SPLIT);
                    String logSplitDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));

                    List<String> hourKeys = new ArrayList<String>();
                    if (args.length >= 3) {
                        // Add all hours in range of startHour and endHour for zip scan.
                        long startHour = Long.parseLong(args[1]);
                        long endHour = Long.parseLong(args[2]);
                        FileStatus[] stats = hdfsUtils.getFileSystem().listStatus(new Path(logSplitDir));
                        for (FileStatus stat : stats) {
                            String tail = pathTailString(stat.getPath());
                            if (!stat.isDir()) {
                                continue; // If its a plain file don't count this one
                            }
                            try {
                                long currHour = Long.parseLong(tail);
                                if (currHour >= startHour && currHour <= endHour) {
                                    hourKeys.add(tail);
                                }
                            } catch (NumberFormatException ex) {
                                continue; // This is not an hour
                            }
                        }

                    } else {
                        hourKeys.add(args[1]); // Only scan for this one hour
                    }
                    Collections.sort(hourKeys);
                    List<ZipSrcDstFile> transferFiles = new ArrayList<ZipSrcDstFile>();
                    for (String hourKey : hourKeys) {
                        String reducerOutputDir = StaticFileUtils.mergePathString(HadoopLogsConfigs.getMapreduceOutputPrefix(), LB_LOGS_SPLIT, hourKey);
                        List<LogReducerOutputValue> reducerOutputList = hdfsUtils.getZipFileInfoList(reducerOutputDir);
                        List<LogReducerOutputValue> filteredZipFileInfo = hdfsUtils.filterZipFileInfoList(reducerOutputList, aid, lid);

                        for (LogReducerOutputValue val : filteredZipFileInfo) {
                            ZipSrcDstFile transferFile = new ZipSrcDstFile();
                            transferFile.setSrcFile(val.getLogFile());
                            transferFile.setDstFile(zipFilePath(hourKey, val.getAccountId(), val.getLoadbalancerId()));
                            transferFile.setHourKey(hourKey);
                            transferFile.setAccountId(val.getAccountId());
                            transferFile.setLoadbalancerId(val.getLoadbalancerId());
                            transferFiles.add(transferFile);
                        }
                    }
                    Collections.sort(transferFiles, new ZipSrcDstFileComparator());
                    for (ZipSrcDstFile transferFile : transferFiles) {
                        System.out.printf("%s AccountId=%d LoadbalancerId=%d\n", transferFile.toString(), transferFile.getAccountId(), transferFile.getLoadbalancerId());
                    }
                    System.out.printf("Are you sure you want to download the above zip files (Y/N)\n");
                    if (CommonItestStatic.inputStream(stdin, "Y")) {
                        for (ZipSrcDstFile transferFile : transferFiles) {
                            String srcFile = transferFile.getSrcFile();
                            String dstFile = transferFile.getDstFile();
                            System.out.printf("Transfering %s -> %s\n", srcFile, dstFile);
                            InputStream is = hdfsUtils.openHdfsInputFile(srcFile, false);
                            OutputStream os = hdfsUtils.openHdfsOutputFile(dstFile, true, true);
                            StaticFileUtils.copyStreams(is, os, System.out, BUFFER_SIZE);
                            is.close();
                            os.close();
                        }
                    }
                } else if (cmd.equals("lsr")) {
                    long total_file_size = 0;
                    long total_repl_size = 0;
                    String mntPath = (args.length >= 2) ? args[1] : fs.getWorkingDirectory().toUri().getRawPath();
                    double startTime = Debug.getEpochSeconds();
                    List<FileStatus> fileStatusList = hdfsUtils.listFileStatusRecursively(mntPath, false);
                    for (FileStatus fileStatus : fileStatusList) {
                        total_file_size += fileStatus.getLen();
                        total_repl_size += fileStatus.getLen() * fileStatus.getReplication();
                        System.out.printf("%s\n", HdfsCliHelpers.displayFileStatus(fileStatus));
                    }
                    System.out.printf("Total file bytes: %s\n", Debug.humanReadableBytes(total_file_size));
                    System.out.printf("Total file bytes including replication: %s\n", Debug.humanReadableBytes(total_repl_size));
                    System.out.printf("Total file count: %d\n", fileStatusList.size());
                    double endTime = Debug.getEpochSeconds();
                    double delay = endTime - startTime;
                    System.out.printf("Took %f Seconds to scan\n", delay);
                } else if (cmd.equals("exit")) {
                    break;
                } else if (cmd.equals("cd") && args.length >= 2) {
                    Path path = new Path(args[1]);
                    fs.setWorkingDirectory(path);
                } else if (cmd.equals("cdin")) {
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceInputPrefix());
                    if (args.length >= 2) {
                        pathComps.add(args[1]);
                    }
                    String pathStr = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
                    System.out.printf("Changing directory to %s\n", pathStr);
                    fs.setWorkingDirectory(new Path(pathStr));
                } else if (cmd.equals("cdout")) {
                    List<String> pathComps = new ArrayList<String>();
                    pathComps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
                    pathComps.add(LB_LOGS_SPLIT);
                    if (args.length >= 2) {
                        pathComps.add(args[1]);
                    }
                    String pathStr = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
                    System.out.printf("Changing directory to %s\n", pathStr);
                    fs.setWorkingDirectory(new Path(pathStr));
                } else if (cmd.equals("pwd")) {
                    System.out.printf("%s\n", fs.getWorkingDirectory().toUri().toString());
                } else if (cmd.equals("cat") && args.length >= 2) {
                    String pathStr = args[1];
                    Path filePath = new Path(pathStr);
                    FSDataInputStream is = fs.open(filePath);
                    StaticFileUtils.copyStreams(is, System.out, null, PAGESIZE);
                    is.close();
                } else if (cmd.equals("chmod") && args.length >= 3) {
                    String octMal = args[1];
                    Path path = new Path(args[2]);
                    short oct = (short) Integer.parseInt(octMal, 8);
                    fs.setPermission(path, new FsPermission(oct));
                    System.out.printf("Setting permisions on file %s\n", path.toUri().toString());
                } else if (cmd.equals("chown") && args.length >= 4) {
                    String fUser = args[1];
                    String fGroup = args[2];
                    String fPath = args[3];
                    fs.setOwner(new Path(fPath), fUser, fGroup);
                    System.out.printf("Setting owner of %s to %s:%s\n", fPath, fUser, fGroup);
                } else if (cmd.equals("mkdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.mkdirs(new Path(fPath));
                    System.out.printf("mkdir %s = %s\n", fPath, resp);
                } else if (cmd.equals("rm") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), false);
                    System.out.printf("rm %s = %s\n", fPath, resp);
                } else if (cmd.equals("rmdir") && args.length >= 2) {
                    String fPath = args[1];
                    boolean resp = fs.delete(new Path(fPath), true);
                    System.out.printf("rmdir %s = %s\n", fPath, resp);
                } else if (cmd.equals("homedir")) {
                    System.out.printf("%s\n", fs.getHomeDirectory().toUri().toString());

                } else if (cmd.equals("cpld") && args.length >= 3) {
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
                } else if (cmd.equals("cpfl") && args.length >= 3) {
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
                } else if (cmd.equals("cptl") && args.length >= 3) {
                    FSDataInputStream is = fs.open(new Path(args[1]), HDFSBUFFSIZE);
                    OutputStream os = StaticFileUtils.openOutputFile(args[2]);
                    StaticFileUtils.copyStreams(is, os, System.out, HDFSBUFFSIZE);
                    is.close();
                    os.close();
                } else if (cmd.equals("findCp")) {
                    if (args.length >= 2) {
                        String className = args[1];
                        String classPath = Debug.findClassPath(className, jobClassLoader);
                        System.out.printf("%s classpath = %s\n", className, classPath);
                        continue;
                    }
                    String classPath = System.getProperties().getProperty("java.class.path");
                    System.out.printf("classpath = %s\n", classPath);
                } else if (cmd.equals("setJobJar") && args.length >= 2) {
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
                } else if (cmd.equals("showCl") && args.length >= 2) {
                    String className = args[1];
                    if (jobClassLoader == null) {
                        System.out.printf("jobJar not yet set\n");
                    }
                    Class classIn = Class.forName(className, true, jobClassLoader);
                    String classLoaderInfo = Debug.classLoaderInfo(className);
                    System.out.printf("%s\n", classLoaderInfo);
                } else if (cmd.equals("countLines") && args.length >= 3) {
                    String fileName = args[1];
                    int nTicks = Integer.valueOf(args[2]);
                    int buffSize = (args.length > 3) ? Integer.valueOf(args[3]) : PAGESIZE * 4;
                    System.out.printf("Counting the lines from file %s with %d ticks", fileName, nTicks);
                    double startTime = Debug.getEpochSeconds();
                    long nLines = HdfsCliHelpers.countLines(fileName, nTicks, buffSize);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to count %d lines\n", endTime - startTime, nLines);
                } else if (cmd.equals("compressLzo") && args.length >= 3) {
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
                } else if (cmd.equals("indexLzo") && args.length >= 2) {
                    String srcFileName = args[1];
                    Path filePath = new Path(StaticFileUtils.expandUser(srcFileName));
                    System.out.printf("Indexing file %s\n", srcFileName);
                    double startTime = Debug.getEpochSeconds();
                    LzoIndex.createIndex(lfs, filePath);
                    double endTime = Debug.getEpochSeconds();
                    System.out.printf("Took %f seconds to index file %s\n", endTime - startTime, srcFileName);
                } else if (cmd.equals("printReducers") && args.length >= 2) {
                    String sequenceDirectory = args[1];
                    List<LogReducerOutputValue> zipFileInfoList = hdfsUtils.getZipFileInfoList(sequenceDirectory);
                    int totalEntryCount = zipFileInfoList.size();
                    int entryNum = 0;
                    for (LogReducerOutputValue zipFileInfo : zipFileInfoList) {
                        System.out.printf("zipFile[%d]=%s\n", entryNum, zipFileInfo.toString());
                        entryNum++;
                    }
                    System.out.printf("Total entries = %d\n", totalEntryCount);
                } else if (cmd.equals("scanLines") && args.length >= 3) {
                    String fileName = args[1];
                    int nLines = Integer.parseInt(args[2]);
                    int nTicks = Integer.parseInt(args[3]);
                    BufferedReader r = new BufferedReader(new FileReader(StaticFileUtils.expandUser(fileName)), HDFSBUFFSIZE);
                    int badLines = 0;
                    int goodLines = 0;
                    int lineCounter = 0;
                    int totalLines = 0;
                    int totalGoodLines = 0;
                    int totalBadLines = 0;
                    LogMapperOutputValue logValue = new LogMapperOutputValue();
                    double startTime = StaticDateTimeUtils.getEpochSeconds();
                    for (int i = 0; i < nLines; i++) {
                        String line = r.readLine();

                        if (line == null) {
                            break; // End of file
                        }
                        try {
                            LogChopper.getLogLineValues(line, logValue);
                            goodLines++;
                            totalGoodLines++;
                        } catch (Exception ex) {
                            badLines++;
                            totalBadLines++;
                            System.out.printf("BAD=%s\n", line);
                        }
                        lineCounter++;
                        totalLines++;
                        if (i % nTicks == 0) {
                            double stopTime = StaticDateTimeUtils.getEpochSeconds();
                            double lps = (double) lineCounter / (stopTime - startTime);
                            System.out.printf("read %d lines goodlines=%d badlines=%d secs = %f linespersecond=%f\n", lineCounter, goodLines, badLines, stopTime - startTime, lps);
                            startTime = stopTime;
                            lineCounter = 0;
                            goodLines = 0;
                            badLines = 0;
                        }
                    }
                    System.out.printf("Good=%d badLines=%d total = %d\n", totalGoodLines, totalBadLines, totalLines);
                    r.close();
                } else if (cmd.equals("showCrc") && args.length >= 2) {
                    String fileName = StaticFileUtils.expandUser(args[1]);
                    BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileName), BUFFER_SIZE);
                    long crc = StaticFileUtils.computeCrc(is);
                    System.out.printf("crc(%s)=%d\n", fileName, crc);
                    is.close();
                } else if (cmd.equals("du")) {
                    long used = fs.getUsed();
                    System.out.printf("Used bytes: %s\n", Debug.humanReadableBytes(used));
                } else if (cmd.equals("setReplCount") && args.length >= 3) {
                    String fileName = args[1];
                    Path filePath = new Path(fileName);
                    short replCount = Short.parseShort(args[2]);
                    System.out.printf("Setting Replication count for file %s to %d\n", fileName, replCount);
                    fs.setReplication(filePath, replCount);
                } else if (cmd.equals("dumpConfig") && args.length >= 2) {
                    System.out.printf("Dumping configs\n");
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(StaticFileUtils.expandUser(args[1]))), HDFSBUFFSIZE);
                    Configuration dumpConf = new Configuration();
                    for (int i = 2; i < args.length; i++) {
                        dumpConf.addResource(new Path(StaticFileUtils.expandUser(args[i])));
                    }
                    dumpConf.writeXml(bos);
                    bos.close();
                    dumpConf.writeXml(System.out);
                } else if (cmd.equals("lineIndex") && args.length >= 2) {
                    String inFileName = StaticFileUtils.expandUser(args[1]);
                    String outFileName = inFileName + ".idx";
                    InputStream is = StaticFileUtils.openInputFile(inFileName);
                    DataOutputStream os = StaticFileUtils.openDataOutputStreamFile(outFileName);
                    System.out.printf("Indexling file %s -> %s\n", inFileName, outFileName);
                    HdfsCliHelpers.indexFile(is, os, PAGESIZE * 8);
                    is.close();
                    os.close();
                } else if (cmd.equals("rebasePath") && args.length >= 4) {
                    String srcBase = args[1];
                    String srcPath = args[2];
                    String dstPath = args[3];
                    System.out.printf("calling StaticFileUtils.rebasePath(%s,%s,%s)=", srcBase, srcPath, dstPath);
                    System.out.flush();
                    String rebasedPath = StaticFileUtils.rebaseSplitPath(srcBase, srcPath, dstPath);
                    System.out.printf("%s\n", rebasedPath);
                } else if (cmd.equals("joinPath") && args.length >= 1) {
                    List<String> pathComps = new ArrayList<String>();
                    for (int i = 1; i < args.length; i++) {
                        pathComps.add(args[i]);
                    }
                    List<String> joinedPathList = StaticFileUtils.joinPath(pathComps);
                    String joinPathString = StaticFileUtils.splitPathToString(joinedPathList);
                    System.out.printf("joinedPath = %s\n", joinPathString);
                } else {
                    System.out.printf("Unrecognized command\n");
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }
        System.out.printf("Exiting\n");
    }

    public static String chop(String line) {
        return line.replace("\r", "").replace("\n", "");
    }

    public static String zipFilePath(String dateHour, int accountId, int loadbalancerId) {
        List<String> pathComps = new ArrayList<String>();
        pathComps.add(HadoopLogsConfigs.getCacheDir());
        pathComps.add(dateHour);
        pathComps.add(Integer.toString(accountId));
        pathComps.add("access_log_" + Integer.toString(loadbalancerId) + "_" + dateHour + ".zip");
        return StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathComps));
    }

    public static String listHourKeyFiles(HdfsUtils hdfsUtils, String remoteDir, String hourKeyPrefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileStatus[] fileStatusArray = hdfsUtils.listStatuses(remoteDir, false);
        List<FileStatus> fileStatusList = new ArrayList<FileStatus>(Arrays.asList(fileStatusArray));
        Collections.sort(fileStatusList, new FileStatusDateComparator());
        for (FileStatus fileStatus : fileStatusList) {
            String tail = StaticFileUtils.pathTail(fileStatus.getPath().toUri().getRawPath().toString());
            if (hourKeyPrefix != null && !tail.startsWith(hourKeyPrefix)) {
                continue;
            }
            sb.append(tail).append(HdfsCliHelpers.displayFileStatus(fileStatus)).append("\n");
        }
        return sb.toString();
    }


    public static HdfsZipDirScan scanHdfsZipDirs(HdfsUtils hdfsUtils, String hourKey, boolean scanParts) {
        Matcher zipMatch = zipPattern.matcher("");
        HdfsZipDirScan scan = new HdfsZipDirScan();
        scan.setHourKey(hourKey);
        List<String> comps = new ArrayList<String>();
        comps.add(HadoopLogsConfigs.getMapreduceOutputPrefix());
        comps.add(LB_LOGS_SPLIT);
        comps.add(hourKey);
        String partsDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(comps));
        comps.add("zips");
        String zipDir = StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(comps));
        List<LogReducerOutputValue> zipInfoList;
        if (scanParts) {
            try {
                zipInfoList = hdfsUtils.getZipFileInfoList(partsDir);
                scan.setPartionFilesFound(true);
            } catch (SequenceFileReaderException ex) {
                zipInfoList = null;
            }
            if (zipInfoList != null) {
                for (LogReducerOutputValue zipInfo : zipInfoList) {
                    scan.getPartZipsFound().add(StaticFileUtils.pathTail(zipInfo.getLogFile()));
                    scan.incPartZipCount(1);
                }
            }

        }
        FileStatus[] fileStatuses;
        try {
            fileStatuses = hdfsUtils.getFileSystem().listStatus(new Path(zipDir));
            if (fileStatuses != null) {
                scan.setDateDirFound(true);
                scan.setZipDirFound(true);
                for (FileStatus fileStatus : fileStatuses) {
                    String zipFileName = StaticFileUtils.pathTail(HdfsUtils.rawPath(fileStatus));
                    zipMatch.reset(zipFileName);
                    if (zipMatch.find()) {
                        scan.incZipCount(1);
                        scan.getZipsFound().add(zipFileName);
                    }
                }
            }
        } catch (IOException ex) {
            fileStatuses = null;
        }
        return scan;
    }

    public static String pathTailString(Path path) {
        return StaticFileUtils.pathTail(path.toUri().getRawPath());
    }

    public static String pathTailString(FileStatus fileStatus) {
        return pathTailString(fileStatus.getPath());
    }
}
