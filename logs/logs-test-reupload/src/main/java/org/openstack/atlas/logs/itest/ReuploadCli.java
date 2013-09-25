package org.openstack.atlas.logs.itest;

import org.apache.hadoop.conf.Configuration;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.auth.AuthServiceImpl;
import org.openstack.atlas.auth.AuthUser;
import org.openstack.atlas.config.HadoopLogsConfigs;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.config.LbLogsConfigurationKeys;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.logs.common.util.CacheZipDirInfo;
import org.openstack.atlas.logs.common.util.CacheZipInfo;
import org.openstack.atlas.logs.common.util.ReuploaderThread;
import org.openstack.atlas.logs.common.util.ReuploaderUtils;
import org.openstack.atlas.logs.hadoop.util.HdfsUtils;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.itest.hibernate.HibernateDbConf;
import org.openstack.atlas.util.itest.hibernate.HuApp;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import org.openstack.client.keystone.KeyStoneAdminClient;
import org.openstack.client.keystone.KeyStoneException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class ReuploadCli {

    public static final String DEFAULT_HADOOP_CONF_FILE = "/etc/openstack/atlas/hadoop-logs.conf";
    public static final String DEFAULT_CONF_FILE = "~/conf.json";
    private static final int BUFFSIZE = 1024 * 32;
    private static final Comparator<CacheZipInfo> lidComparator;
    private static final Comparator<CacheZipInfo> aidComparator;
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
    private BufferedReader stdin;

    static {
        lidComparator = new CacheZipInfo.LidComparator();
        aidComparator = new CacheZipInfo.AidComparator();
    }

    public void run(String[] argv) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException, AuthException {
        if (argv.length < 1) {
            System.out.printf("usage is <conf.json> [hadoop-logs.conf]\n");
            System.out.printf("Externally test the reuploader code for CloudFiles\n");
            System.out.printf("the json conf file will be of the form:\n%s\n", HibernateDbConf.exampleJson);
            System.out.printf("if the hadoopConfiguration.xml file param is blank the value\n");
            System.out.printf("will be deduced from the %s file\n", DEFAULT_HADOOP_CONF_FILE);
            System.out.printf("\n");
        }
        List<ReuploaderThread> uploaders = new ArrayList<ReuploaderThread>();
        stdin = StaticFileUtils.inputStreamToBufferedReader(System.in, BUFFSIZE);
        //System.out.printf("Press enter to continue\n");
        //stdin.readLine();
        String jsonDbConfFileName;


        if (argv.length <= 0) {
            System.out.printf("using Default conf.json file %s since no conf file specified on command line\n", DEFAULT_CONF_FILE);
            jsonDbConfFileName = StaticFileUtils.expandUser(DEFAULT_CONF_FILE);
        } else {
            System.out.printf("Using db conf file %s\n", argv[0]);
            jsonDbConfFileName = StaticFileUtils.expandUser(argv[0]);
        }

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
                    System.out.printf("getzinfo [lastHour]   #Scan the cache dir for the zips still in localcache\n");
                    System.out.printf("setComp <size,hour> reverse=false  # Set the comparator to sort by size or by hour\n");
                    System.out.printf("showzinfo  #Display all the zipDirectories found\n");
                    System.out.printf("showzips   #Show all zips\n");
                    System.out.printf("countzinfo  #scan the zinfo block and count the zips by account and lid\n");
                    System.out.printf("countlids #Count the zips grouping by the lids\n");
                    System.out.printf("countaids #Count the zips grouping by the aids\n");
                    System.out.printf("counthours #Count the zips grouping by the hours\n");
                    System.out.printf("showAuth <accountId> #Get information on account via the god AuthClient\n");
                    System.out.printf("rmlid <lid> #remove zips in the zinfolist that are for the specified loadbalancer\n");
                    System.out.printf("rmaid <aid> #remove zips in the zinfolist that are for the specified account\n");
                    System.out.printf("keyauth <accountId> #Get service token and other user info from keystone auth\n");
                    System.out.printf("clearDirs <minusHours>    #Remove any empty directories\n");
                    System.out.printf("delDir <path> #Delete directory if its empty\n");
                    System.out.printf("utc [minusHours] #Get the time stamp in utc for the hour Key that clearDirs would scan\n");
                    System.out.printf("addLock <fileName> #Test the file locker\n");
                    System.out.printf("showLocks #Show the current file locks\n");
                    System.out.printf("clearOldLocks <secs> #Test the lock expiration counter\n");
                    System.out.printf("ru #run the uploader thread\n");
                    System.out.printf("joinThreads #Join reuploader threads\n");

                } else if (cmd.equals("countzinfo")) {
                    countZinfo();
                } else if (cmd.equals("showAuth") && args.length >= 2) {
                    System.out.printf("showing AuthUser info for user %s\n", args[1]);
                    showAuth(args[1]);
                } else if (cmd.equals("auth") && args.length >= 2) {
                    System.out.printf("getting auth tokens for user %s\n", args[1]);
                    auth(args[1]);
                } else if (cmd.equals("ru")) {
                    int nThreads = (args.length >= 2) ? Integer.parseInt(args[1]) : 1;

                    List<ReuploaderThread> newThreads = new ArrayList<ReuploaderThread>();
                    for (int i = 0; i < nThreads; i++) {
                        System.out.printf("Init thread %d of %d\n", i, nThreads);
                        ReuploaderThread uploader = new ReuploaderThread(new ReuploaderUtils(HadoopLogsConfigs.getCacheDir(), lbMap));
                        newThreads.add(uploader);
                    }
                    System.out.printf("Running Threads\n");
                    for (int i = 0; i < nThreads; i++) {
                        System.out.printf("Running thread %d of %d\n", i, nThreads);
                        ReuploaderThread uploader = newThreads.get(i);
                        uploader.start();
                        uploaders.add(uploader);
                    }
                    System.out.printf("All threads running\n");

                } else if (cmd.equals("joinThreads")) {
                    int nThreads = uploaders.size();
                    System.out.printf("Joining %d threads\n", nThreads);
                    for (int i = 0; i < uploaders.size(); i++) {
                        System.out.printf("Joining %d of %d threads\n", i, nThreads);
                        uploaders.get(i).join();
                    }
                    uploaders = new ArrayList<ReuploaderThread>();
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
                    showLocks();
                } else if (cmd.equals("delDir") && args.length >= 2) {
                    delDir(args[1]);
                } else if (cmd.equals("clearDirs") && args.length >= 2) {
                    int minusHours;
                    System.out.printf("Removing empty directories\n");
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
                    setSortComparator(args[1], reverse);
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
                } else if (cmd.equals("getzinfo")) {
                    long endHourKey;
                    if (args.length >= 2) {
                        try {
                            endHourKey = Long.parseLong(args[1]);
                        } catch (NumberFormatException ex) {
                            System.out.printf("lastHour parameter must be of the form YYYYMMDDHH\n");
                            continue;
                        }
                    } else {
                        endHourKey = 9999999999L;
                    }
                    System.out.printf("Searching for zips before HourKey=%d\n", endHourKey);
                    getZipInfo(endHourKey);
                } else if (cmd.equals("rmlid") && args.length >= 2) {
                    int lid;
                    try {
                        lid = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        System.out.printf("Error converting %s to loadbalancer id\n", args[1]);
                        continue;
                    }
                    deleteLidZips(lid);
                } else if (cmd.equals("rmaid") && args.length >= 2) {
                    int aid;
                    try {
                        aid = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        System.out.printf("Error converting %s to loadbalancer id\n", args[1]);
                        continue;
                    }
                    deleteAidZips(aid);
                } else if (cmd.equals("showZips")) {
                    showZips();
                } else if (cmd.equals("showzinfo")) {
                    showZipsDirInfo();
                } else if (cmd.equals("countlids")) {
                    countIds(CountTypes.LOADBALANCER);
                } else if (cmd.equals("countaids")) {
                    countIds(CountTypes.ACCOUNT);
                } else if (cmd.equals("counthours")) {
                    countIds(CountTypes.HOUR);
                } else {
                    System.out.printf("Unknown Command %s\n", cmdLine);
                }
            } catch (Exception ex) {
                System.out.printf("Exception: %s\n", Debug.getExtendedStackTrace(ex));
            }
        }

    }

    public void setSortComparator(String arg, boolean reverse) {
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

    private void delDir(String path) {
        ReuploaderUtils.deleteIfDirectoryIsEmpty(path);
    }

    private void showLocks() {
        System.out.printf("Locks = ");
        System.out.flush();
        System.out.printf("%s\n", ReuploaderUtils.showLocks());
    }

    private void showAuth(String userName) throws AuthException {
        AuthService authService = new AuthServiceImpl(new LbLogsConfiguration());
        AuthUser user = authService.getUser(userName);
        System.out.printf("%s\n", user);
    }

    private void auth(String userName) throws AuthException, KeyStoneException {
        KeyStoneAdminClient keyStoneAdminClient;
        LbLogsConfiguration cfg = new LbLogsConfiguration();
        String adminAuthUrl = cfg.getString(LbLogsConfigurationKeys.auth_management_uri);
        String adminAuthUser = cfg.getString(LbLogsConfigurationKeys.basic_auth_user);
        String adminAuthKey = cfg.getString(LbLogsConfigurationKeys.basic_auth_key);
        keyStoneAdminClient = new KeyStoneAdminClient(adminAuthUrl, adminAuthKey, adminAuthUser);
    }

    private void deleteLidZips(int lid) throws IOException {
        int nDeleted = 0;
        List<CacheZipInfo> doomedZips = new ArrayList<CacheZipInfo>();
        List<CacheZipInfo> sortedZips = new ArrayList<CacheZipInfo>(zipInfoList);
        Collections.sort(sortedZips, lidComparator);
        for (CacheZipInfo zipFile : sortedZips) {
            if (zipFile.getLoadbalancerId() == lid) {
                System.out.printf("%s\n", zipFile.getZipFile());
                doomedZips.add(zipFile);
            }
        }
        System.out.printf("Are you sure you want to delete the above %d zips(Y/N): ", doomedZips.size());
        if (CommonItestStatic.inputStream(stdin, "Y")) {
            System.out.printf("Deleting files\n");
            for (CacheZipInfo doomedZip : doomedZips) {
                if (deleteFile(doomedZip.getZipFile())) {
                    nDeleted++;
                }
            }
        } else {
            System.out.printf("Bailing out\n");
        }
        System.out.printf("Deleted %d files\n", nDeleted);
    }

    private void deleteAidZips(int aid) throws IOException {
        int nDeleted = 0;
        List<CacheZipInfo> doomedZips = new ArrayList<CacheZipInfo>();
        List<CacheZipInfo> sortedZips = new ArrayList<CacheZipInfo>(zipInfoList);
        Collections.sort(sortedZips, aidComparator);
        for (CacheZipInfo zipFile : sortedZips) {
            if (zipFile.getAccountId() == aid) {
                System.out.printf("%s\n", zipFile.getZipFile());
                doomedZips.add(zipFile);
            }
        }
        System.out.printf("Are you sure you want to delete the above %d zips(Y/N): ", doomedZips.size());
        if (CommonItestStatic.inputStream(stdin, "Y")) {
            System.out.printf("Deleting files\n");
            for (CacheZipInfo doomedZip : doomedZips) {
                if (deleteFile(doomedZip.getZipFile())) {
                    nDeleted++;
                }
            }
        } else {
            System.out.printf("Bailing out\n");
        }
        System.out.printf("Deleted %d files\n", nDeleted);
    }

    private static boolean deleteFile(String fileName) {
        File doomedFile = new File(fileName);
        try {
            if (!doomedFile.delete()) {
                System.out.printf("File %s didn't delete perhaps its already deleted\n", fileName);
                return false;
            }
            return true;
        } catch (Exception ex) {
            System.out.printf("Error attempting to delete file %s: %s\n", fileName, Debug.getExtendedStackTrace(ex));
            return false;
        }
    }

    public static void main(String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException, IOException, AuthException {
        ReuploadCli cli = new ReuploadCli();
        cli.run(args);
    }

    public void countIds(CountTypes countType) {
        Map<Long, Integer> counts = new HashMap<Long, Integer>();
        String countTypeStr;
        switch (countType) {
            case ACCOUNT:
                countTypeStr = "AccountId";
                break;
            case LOADBALANCER:
                countTypeStr = "LoadBalancerId";
                break;
            case HOUR:
                countTypeStr = "Hour";
                break;
            default:
                countTypeStr = "Unknown count type Bailing out";
                return;
        }
        System.out.printf("Counting %s:\n", countTypeStr);
        for (CacheZipInfo zipFile : zipInfoList) {
            long key = -1; // Cause the compiler will complain if you don't initialize
            switch (countType) {
                case ACCOUNT:
                    key = zipFile.getAccountId();
                    break;
                case HOUR:
                    key = zipFile.getHourKey();
                    break;
                case LOADBALANCER:
                    key = zipFile.getLoadbalancerId();
                    break;
            }
            if (!counts.containsKey(key)) {
                counts.put(key, 0);
            }
            int count = counts.get(key);
            counts.put(key, count + 1);
        }
        List<Long> keys = new ArrayList<Long>(counts.keySet());
        Collections.sort(keys);
        for (Long key : keys) {
            System.out.printf("    %d = %d\n", key, counts.get(key));
        }
    }

    private void countZinfo() {
        int nCount;
        Map<AccountIdLoadBalancerIdKey, Integer> counts = new HashMap<AccountIdLoadBalancerIdKey, Integer>();
        for (CacheZipInfo zipFile : zipInfoList) {
            AccountIdLoadBalancerIdKey aidLidKey = new AccountIdLoadBalancerIdKey();
            aidLidKey.setAccountId(zipFile.getAccountId());
            aidLidKey.setLoadbalancerId(zipFile.getLoadbalancerId());
            if (!counts.containsKey(aidLidKey)) {
                counts.put(aidLidKey, 0);
            }
            nCount = counts.get(aidLidKey);
            nCount++;
            counts.put(aidLidKey, nCount);
        }
        List<AccountIdLoadBalancerIdKey> keys = new ArrayList<AccountIdLoadBalancerIdKey>();
        for (AccountIdLoadBalancerIdKey key : counts.keySet()) {
            keys.add(key);
        }
        nCount = 0;
        Collections.sort(keys, new AccountIdLoadBalancerIdKeyComparator());
        System.out.printf("(accountId,LoadbalancerId)=count\n");
        for (AccountIdLoadBalancerIdKey aidLidKey : keys) {
            int aid = aidLidKey.getAccountId();
            int lid = aidLidKey.getLoadbalancerId();
            int count = counts.get(aidLidKey);
            System.out.printf("(%d,%d) = %d\n", aid, lid, count);
            nCount++;
        }
        System.out.printf("Total files = %d\n", nCount);
    }
}
