package org.openstack.atlas.logs.itest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.logs.hadoop.util.CacheZipDirInfo;
import org.openstack.atlas.logs.hadoop.util.CacheZipInfo;
import org.openstack.atlas.logs.hadoop.util.ReuploaderUtils;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class ReuploadCli {

    public static final String DEFAULT_HADOOP_CONF_FILE = "/etc/openstack/atlas/hadoop-logs.conf";
    private static final int BUFFSIZE = 1024 * 32;
    private HdfsUtils hdfsUtils;
    private Configuration conf;
    private Map<Integer, LoadBalancerIdAndName> lbMap;
    private HuApp huApp;
    private HibernateDbConf hConf;
    private ReuploaderUtils ru;
    private List<CacheZipDirInfo> zipDirInfoList = new ArrayList<CacheZipDirInfo>();
    private List<CacheZipInfo> zipInfoList = new ArrayList<CacheZipInfo>();
    private Comparator<CacheZipInfo> ziComp;
    private Comparator<CacheZipDirInfo> zidComp;

    public void run(String[] argv) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        if (argv.length < 1) {
            System.out.printf("usage is <conf.json> [hadoop-logs.conf]\n");
            System.out.printf("Externally test the reuploader code for CloudFiles\n");
            System.out.printf("the json conf file will be of the form:\n%s\n", HibernateDbConf.exampleJson);
            System.out.printf("if the hadoopConfiguration.xml file param is blank the value\n");
            System.out.printf("will be deduced from the %s file\n", DEFAULT_HADOOP_CONF_FILE);
            return;
        }

        BufferedReader stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);
        //System.out.printf("Press enter to continue\n");
        //stdin.readLine();

        if (argv.length >= 2) {
            System.out.printf("Useing confFile %s\n", argv[1]);
            HadoopLogsConfigs.resetConfigs(argv[1]);
        } else {
            System.out.printf("useing confFile %s\n", LbLogsConfiguration.defaultConfigurationLocation);
        }

        hdfsUtils = HadoopLogsConfigs.getHdfsUtils();
        String user = HadoopLogsConfigs.getHdfsUserName();
        conf = HadoopLogsConfigs.getHadoopConfiguration();
        HadoopLogsConfigs.markJobsJarAsAlreadyCopied();
        System.setProperty(CommonItestStatic.HDUNAME, user);

        System.out.printf("ReuploadTestStatic.main Spinning up\n");
        System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
        String jsonDbConfFileName = StaticFileUtils.expandUser(argv[0]);
        huApp = new HuApp();
        hConf = HibernateDbConf.newHibernateConf(jsonDbConfFileName);
        System.out.printf("Useing db config %s\n", hConf.toString());
        huApp.setDbMap(hConf);
        System.out.printf("Reading LoadBalancers from databases\n");
        lbMap = CommonItestStatic.getLbIdMap(huApp);
        System.out.printf("HadoopLogsConfig=%s\n", HadoopLogsConfigs.staticToString());

        ru = new ReuploaderUtils(HadoopLogsConfigs.getCacheDir(), lbMap);

        ziComp = new CacheZipInfo.ZipComparator();
        zidComp = new CacheZipDirInfo.HourAccountComparator();

        while (true) {
            try {
                System.out.printf("reuploadClient> ");
                String cmdLine = stdin.readLine();
                if (cmdLine == null) {
                    break;// Eof
                }
                String[] args = CommonItestStatic.stripBlankArgs(cmdLine);
                Map<String, String> kwArgs = CommonItestStatic.argMapper(args);
                args = CommonItestStatic.stripKwArgs(args);
                if (args.length < 1) {
                    System.out.printf("usage is help\n");
                    continue;
                }
                String cmd = args[0];
                if (cmd.equals("help")) {
                    System.out.printf("gc         #Run garbage collector\n");
                    System.out.printf("mem        #Display memory usage\n");
                    System.out.printf("exit       #exit program\n");
                    System.out.printf("clzinfo    #reset the zipInfo from memory from memory\n");
                    System.out.printf("getzinfo <lastHour>   #Scan the cache dir for the zips still in localcache\n");
                    System.out.printf("setComp <size,hour> reverse=false  # Set the comparator to sort by size or by hour\n");
                    System.out.printf("showzinfo  #Display all the zipDirectories found\n");
                    System.out.printf("showzips   #Show all zips\n");
                    System.out.printf("clearDirs <minusHours>    #Remove any empty directories\n");
                    System.out.printf("delDir <path> #Delete directory if its empty\n");
                    System.out.printf("utc [minusHours] #Get the time stamp in utc for the hour Key that clearDirs would scan\n");
                    System.out.printf("addLock <fileName> #Test the file locker\n");
                    System.out.printf("showLocks #Show the current file locks\n");
                    System.out.printf("clearOldLocks <secs> #Test the lock expiration counter\n");
                } else if (cmd.equals("addLock") && args.length >= 2) {
                    String fileName = args[1];
                    System.out.printf("Locking file %s = ", fileName);
                    System.out.flush();
                    System.out.printf("%s\n", ReuploaderUtils.addLock(fileName));

                } else if (cmd.equals("clearOldLocks") && args.length >= 2) {
                    System.out.printf("Clearing locks older then %s\n", args[1]);
                    int secs = Integer.parseInt(args[1]);
                    ReuploaderUtils.clearOldLocks(secs);
                } else if (cmd.equals("showLocks")) {
                    System.out.printf("Locks = ");
                    System.out.flush();
                    System.out.printf("%s\n", ReuploaderUtils.showLocks());
                } else if (cmd.equals("delDir") && args.length >= 2) {
                    delDir(args[1]);
                } else if (cmd.equals("clearDirs") && args.length >= 2) {
                    int minusHours;
                    try {
                        minusHours = Integer.parseInt(args[1]);
                        clearDirs(minusHours);
                    } catch (NumberFormatException ex) {
                        System.out.printf("Error could not convert %s to integer\n", args[1]);
                    }

                } else if (cmd.equals("setComp") && args.length >= 2) {
                    boolean reverse = false;
                    if (kwArgs.containsKey("reverse") && kwArgs.get("reverse").equals("true")) {
                        reverse = true;
                    }
                    setComparator(args[1], reverse);
                } else if (cmd.equals("utc")) {
                    int minusHours = 0;
                    if (args.length >= 2) {
                        try {
                            minusHours = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex) {
                            System.out.printf("minusHours %s is not an integer defaulting to 0\n", args[0]);
                        }
                    }
                    utcStamp(minusHours);
                } else if (cmd.equals("gc")) {
                    gc();
                } else if (cmd.equals("mem")) {
                    mem();
                } else if (cmd.equals("clzinfo")) {
                    clearZipInfo();
                } else if (cmd.equals("getzinfo") && args.length >= 2) {
                    long endHourKey;
                    try {
                        endHourKey = Long.parseLong(args[1]);
                        getZipInfo(endHourKey);
                    } catch (NumberFormatException ex) {
                        System.out.printf("lastHour parameter must be of the form YYYYMMDDHH\n");
                    }
                } else if (cmd.equals("showZips")) {
                    showZips();
                } else if (cmd.equals("showzinfo")) {
                    showZipsDirInfo();
                } else {
                    System.out.printf("Unknown Command %s\n", cmdLine);
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }

    }

    public void setComparator(String arg, boolean reverse) {
        String compOption = arg;
        if (compOption.equals("size")) {
            System.out.printf("Setting comparator for size\n");
            ziComp = new CacheZipInfo.ByteCountComparator();
            zidComp = new CacheZipDirInfo.CountComparator();
        } else if (compOption.equals("hour")) {
            System.out.printf("Setting comarator for hour\n");
            ziComp = new CacheZipInfo.ZipComparator();
            zidComp = new CacheZipDirInfo.HourAccountComparator();
        } else {
            System.out.printf("Comparator operator must be either \"size\" or \"hour\"\n");
            return;
        }
        System.out.printf("Sorting zinfo and zips\n");
        Collections.sort(zipInfoList, ziComp);
        Collections.sort(zipDirInfoList, zidComp);
        if (reverse) {
            System.out.printf("reversing order\n");
            Collections.reverse(zipInfoList);
        }
    }

    public void showZipsDirInfo() {
        System.out.printf("ZipDirInfo:\n");
        for (CacheZipDirInfo zdi : zipDirInfoList) {
            System.out.printf("%s %d\n", zdi.getDirName(), zdi.getZipCount());
        }
    }

    public void showZips() {
        System.out.printf("Zips:\n");
        for (CacheZipInfo zi : zipInfoList) {
            System.out.printf("%s %d\n", zi.toString(), zi.getFileSize());
        }
    }

    public void clearZipInfo() {
        System.out.printf("clearing zinfo\n");
        zipDirInfoList = new ArrayList<CacheZipDirInfo>();
        zipInfoList = new ArrayList<CacheZipInfo>();
    }

    public void getZipInfo(long endHour) {
        clearZipInfo();
        zipDirInfoList = ru.getLocalZipDirInfo(endHour);
        int totalZips = 0;
        int totalDirs = 0;
        long totalBytes = 0;

        for (CacheZipDirInfo dirInfo : zipDirInfoList) {
            totalDirs++;
            for (CacheZipInfo zipFileInfo : dirInfo.getZips()) {
                zipInfoList.add(zipFileInfo);
                totalZips++;
                totalBytes += zipFileInfo.getFileSize();
            }
        }
        System.out.printf("Scan complete\n");
        System.out.printf("TotalDirs = %d\n", totalDirs);
        System.out.printf("TotalZips = %s\n", totalZips);
        System.out.printf("TotalBytes=%d\n", totalBytes);
    }

    public void mem() {
        System.out.printf("Memory\n================================\n%s\n", Debug.showMem());
    }

    public void gc() {
        System.out.printf("Invoking garbage collector\n");
        Debug.gc();
    }

    private void utcStamp(int minusHours) {
        long hourKey = ReuploaderUtils.getCurrentHourKeyMinusHours(minusHours);
        DateTime dt = StaticDateTimeUtils.nowDateTime(true).plusHours(0 - minusHours);
        DateTime lt = StaticDateTimeUtils.toDateTime(dt.toDate(), false);
        System.out.printf("    hourKey = %d\n", hourKey);
        System.out.printf("    sqlDate = %s\n", StaticDateTimeUtils.sqlDateTimeFormat.print(dt));
        System.out.printf("    apacheDate = %s\n", StaticDateTimeUtils.apacheDateTimeFormat.print(dt));
        System.out.printf("    iso8601 = %s\n", StaticDateTimeUtils.isoFormat.print(dt));
        System.out.printf("    localTime = %s\n", StaticDateTimeUtils.isoFormat.print(lt));
        System.out.printf("\n");
    }

    private void clearDirs(int minusHours) {
        ru.clearDirs(minusHours);
    }

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException {
        ReuploadCli cli = new ReuploadCli();
        cli.run(args);
    }

    private void delDir(String path) {
        ReuploaderUtils.deleteIfDirectoryIsEmpty(path);
    }
}
