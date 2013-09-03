package org.openstack.atlas.logs.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openstack.atlas.auth.AuthService;
import org.openstack.atlas.auth.AuthServiceImpl;
import org.openstack.atlas.auth.AuthUser;
import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.cloudfiles.CloudFilesDaoImpl;
import org.openstack.atlas.exception.AuthException;
import org.openstack.atlas.logs.hadoop.util.LogFileNameBuilder;
import org.openstack.atlas.logs.hadoop.util.StaticLogUtils;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.config.LbLogsConfiguration;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

public class ReuploaderUtils {

    private static final int FileLockTTL = 2 * 60 * 60;
    private static final int hoursToStartOn = 4;
    private static final Map<String, DateTime> lockedFiles;
    private String cacheDir;
    private Map<Integer, LoadBalancerIdAndName> loadBalancerIdMap;
    private static final VerboseLogger vlog = new VerboseLogger(ReuploaderUtils.class, VerboseLogger.LogLevel.INFO);
    private static final Log LOG = LogFactory.getLog(ReuploaderUtils.class);
    private static final Comparator<CacheZipInfo> defaultZipInfoComparator = new CacheZipInfo.ZipComparator();
    private static final Comparator<CacheZipDirInfo> defaultZipDirInfoComparator = new CacheZipDirInfo.HourAccountComparator();
    private AuthService authService;
    private CloudFilesDao cloudFilesDao;

    static {
        lockedFiles = new HashMap<String, DateTime>();
    }

    public ReuploaderUtils(String cacheDir, Map<Integer, LoadBalancerIdAndName> loadBalancerIdMap) throws AuthException {
        this.cacheDir = cacheDir;
        this.loadBalancerIdMap = loadBalancerIdMap;
        this.authService = new AuthServiceImpl(new LbLogsConfiguration());
        this.cloudFilesDao = new CloudFilesDaoImpl();
        clearOldLocks(FileLockTTL);
    }

    public static void clearOldLocks(int secs) {
        DateTime expireTime = StaticDateTimeUtils.nowDateTime(true).minusSeconds(secs);
        synchronized (lockedFiles) {
            List<String> fileNames = new ArrayList<String>(lockedFiles.keySet());
            for (String fileName : fileNames) {
                if (expireTime.isAfter(lockedFiles.get(fileName))) {
                    lockedFiles.remove(fileName);
                }
            }
        }
    }

    public static void removeLock(String fileName) {
        synchronized (lockedFiles) {
            lockedFiles.remove(fileName);
        }
    }

    public static boolean addLock(String fileName) {
        DateTime now = StaticDateTimeUtils.nowDateTime(true);
        boolean locked;
        synchronized (lockedFiles) {
            if (lockedFiles.containsKey(fileName)) {
                return false;
            }
            lockedFiles.put(fileName, now);
            return true;
        }
    }

    public static String showLocks() {
        StringBuilder sb = new StringBuilder();
        sb.append("lockedFiles{");
        synchronized (lockedFiles) {
            for (Entry<String, DateTime> lockEntry : lockedFiles.entrySet()) {
                sb.append("{").append(lockEntry.getKey()).
                        append(",").append(lockEntry.getValue()).
                        append("},");
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public void reuploadFiles() {
        List<CacheZipDirInfo> cacheZipDirInfoList = getLocalZipDirInfo(getCurrentHourKeyMinusHours(hoursToStartOn));
        List<CacheZipInfo> zipsList = new ArrayList<CacheZipInfo>();
        for (CacheZipDirInfo cacheZipDirInfo : cacheZipDirInfoList) {
            zipsList.addAll(cacheZipDirInfo.getZips());
        }
        // Sort by date,accountId, and lastly Loadbalancer Id.
        Collections.sort(zipsList, new CacheZipInfo.ZipComparator());
        int currAccountId = -1;
        LoadBalancerIdAndName lb;


        int zipListSize = zipsList.size();
        for (int i = 0; i < zipListSize; i++) {
            CacheZipInfo zipFile = zipsList.get(i);
            // Try to lock the file otherwise continue to the next;

            if (!addLock(zipFile.getZipFile())) {
                LOG.warn(String.format("%s: Skipping file %s as its locked already", Debug.threadName(), zipFile.getZipFile()));
                continue; // 
            }
            if (!loadBalancerIdMap.containsKey(zipFile.getLoadbalancerId())) {
                removeLock(zipFile.getZipFile());
                // this file didn't map so throw it out.
                LOG.warn(String.format("%s:coulden't map file %s to a loadbalancer. :(", Debug.threadName(), zipFile.getZipFile()));
                continue;
            }
            try {
                lb = loadBalancerIdMap.get(zipFile.getLoadbalancerId());
                String containerName = LogFileNameBuilder.getContainerName(Integer.toString(lb.getLoadbalancerId()), lb.getName(), Long.toString(zipFile.getHourKey()));
                String remoteFileName = LogFileNameBuilder.getRemoteFileName(Integer.toString(lb.getLoadbalancerId()), lb.getName(), Long.toString(zipFile.getHourKey()));
                LOG.info(String.format("%s:Sending file %s %d of %d to [%s]:%s %d bytes", Debug.threadName(), zipFile.getZipFile(), i, zipListSize, containerName, remoteFileName, zipFile.getFileSize()));
                if (!new File(zipFile.getZipFile()).canRead()) {
                    LOG.warn(String.format("%s: Coulden't read file %s perhaps its already sent", Debug.threadName(), zipFile.getZipFile()));
                    lockedFiles.remove(zipFile.getZipFile());
                    continue;
                }
                AuthUser user = authService.getUser(Integer.toString(zipFile.getAccountId()));
                LOG.warn(String.format("user info = %s", user.toString()));
                cloudFilesDao.uploadLocalFile(user, containerName, zipFile.getZipFile(), remoteFileName);
                // Delete the file now.
                if (!new File(zipFile.getZipFile()).delete()) {
                    LOG.error(String.format("%s:Error deleting file %s", Debug.threadName(), zipFile.getZipFile()));
                }
                removeLock(zipFile.getZipFile());
            } catch (Exception ex) {
                LOG.error(String.format("%s:Error uploading file %s :(", Debug.threadName(), zipFile.getZipFile()), ex);
                removeLock(zipFile.getZipFile());
            }
        }
    }

    public List<CacheZipDirInfo> getLocalZipDirInfo(long endHour) {
        return getLocalCacheZipDirInfo(cacheDir, endHour);
    }

    public static long getCurrentHourKeyMinusHours(int hours) {
        DateTime dt = StaticDateTimeUtils.nowDateTime(true).plusHours(0 - hours);
        return StaticDateTimeUtils.dateTimeToHourLong(dt);
    }

    public static boolean deleteIfDirectoryIsEmpty(String path) {
        File[] subFiles = null;
        File file = new File(path);
        try {

            if (!file.isDirectory()) {
                return false; // This isn't even a directory lets skip it\n"
            }
            subFiles = file.listFiles();
            if (subFiles == null) {
                LOG.warn(String.format("Could not determine if directory %s was empty. new File(%s).listFiles() returned null", path));
                return false;
            }
        } catch (Exception ex) {
            LOG.warn(String.format("Could not determine if directory %s was empty: Exception ex", path, Debug.getExtendedStackTrace(ex)), ex);
            return false;
        }

        try {
            if (subFiles.length <= 0) { // If the directories empty then try to delete it
                if (!file.delete()) {
                    throw new IOException();
                } else {
                    return true;
                }
            }
        } catch (Exception ex) {
            LOG.warn(String.format("Could not delete empty directory %s", path), ex);

        }
        return false;
    }

    public void clearDirs(int minusHours) {
        clearDirs(cacheDir, minusHours);
    }

    public static void clearDirs(String cacheDir, int minusHours) {
        long stopHourKey = getCurrentHourKeyMinusHours(minusHours);
        // First pass clear the empty zip directories
        int nCleared = 0;
        List<Long> hourKeys = new ArrayList<Long>();
        for (Long hourKey : listHourDirectories(cacheDir)) {
            if (hourKey > stopHourKey) {
                continue; // This directory is too new so skip it.
            }
            hourKeys.add(hourKey);
        }

        // First pass delete the subAccount Directories
        for (Long hourKey : hourKeys) {
            List<Long> accountKeys = listAccountDirectories(cacheDir, hourKey);
            Collections.sort(accountKeys);
            for (Long accountKey : accountKeys) {
                String zipDirPath = StaticFileUtils.mergePathString(cacheDir, hourKey.toString(), accountKey.toString());
                if (deleteIfDirectoryIsEmpty(zipDirPath)) {
                    nCleared++;
                }
            }
        }

        // Second pass delete the empty hour directories
        for (Long hourKey : hourKeys) {
            String hourKeyDirPath = StaticFileUtils.mergePathString(cacheDir, hourKey.toString());
            if (deleteIfDirectoryIsEmpty(hourKeyDirPath)) {
                nCleared++;
            }
        }
        LOG.info(String.format("Cleared %d empty directories", nCleared));
    }

    public static List<CacheZipDirInfo> getLocalCacheZipDirInfo(String cacheDir, long endHour) {
        List<CacheZipDirInfo> localCacheZipDirInfo = new ArrayList<CacheZipDirInfo>();
        List<Long> hourKeys = listHourDirectories(cacheDir);
        for (Long hourKey : hourKeys) {
            long hour = hourKey;
            if (hour > endHour) {
                continue;
            }
            vlog.printf("Scanning %d", hour);
            List<Long> accountIds = listAccountDirectories(cacheDir, hour);
            for (Long accountId : accountIds) {
                int account = accountId.intValue();
                CacheZipDirInfo accountDirInfo = new CacheZipDirInfo();
                String accountPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hour), Long.toString(account));
                accountDirInfo.setDirName(accountPath);
                accountDirInfo.setAccountId(account);
                accountDirInfo.setHourKey(hour);
                accountDirInfo.setZipCount(0);
                localCacheZipDirInfo.add(accountDirInfo);
                accountDirInfo.setZips(getLocalCacheZips(cacheDir, hour, account));
                accountDirInfo.setZipCount(accountDirInfo.getZips().size());
            }
        }
        Collections.sort(localCacheZipDirInfo, defaultZipDirInfoComparator);
        return localCacheZipDirInfo;
    }

    public static List<Long> listHourDirectories(String cacheDir) {
        return listNumericDirectories(cacheDir);
    }

    public static List<Long> listAccountDirectories(String cacheDir, long hourKey) {
        String hourPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hourKey));
        return listNumericDirectories(hourPath);
    }

    public static List<CacheZipInfo> getLocalCacheZips(String cacheDir, long hourKey, int accountId) {
        List<CacheZipInfo> zipInfoList = new ArrayList<CacheZipInfo>();
        String accountPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hourKey), Integer.toString(accountId));
        File[] zipFiles;
        Matcher zipMatcher = StaticLogUtils.zipLogPattern.matcher("");
        try {
            zipFiles = (new File(accountPath)).listFiles();
            if (zipFiles == null) {
                String msg = String.format("Warning unable to read directory %s File.listFiles() returned null", accountPath);
                throw new IOException(msg);
            }
        } catch (Exception ex) {
            String msg = String.format("Unable to read %s: Exception %s", accountPath, Debug.getExtendedStackTrace(ex));
            LOG.warn(msg, ex);
            return zipInfoList;
        }
        for (File zipFile : zipFiles) {
            CacheZipInfo zipInfo = new CacheZipInfo();
            zipInfoList.add(zipInfo);
            try {
                String zipName = zipFile.getName();
                zipMatcher.reset(zipName);
                if (zipMatcher.find()) {
                    String loadBalancerIdStr = zipMatcher.group(2);
                    String zipPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hourKey), Integer.toString(accountId), zipName);
                    zipInfo.setLoadbalancerId(Integer.parseInt(loadBalancerIdStr));
                    zipInfo.setFileSize(zipFile.length());
                    zipInfo.setZipFile(zipPath);
                    zipInfo.setAccountId(accountId);
                    zipInfo.setHourKey(hourKey);
                }
            } catch (Exception ex) {
                String msg = String.format("unable to convert zip file into LocalCacheZipInfo: Exception %s", Debug.getExtendedStackTrace(ex));
                LOG.warn(msg, ex);
                continue;
            }
        }
        Collections.sort(zipInfoList, defaultZipInfoComparator);
        return zipInfoList;
    }

    public static List<Long> listNumericDirectories(String dir) {
        List<Long> numericDirectories = new ArrayList<Long>();
        File[] files;
        try {
            File dirFile = new File(dir);
            files = dirFile.listFiles();
            if (files == null) {
                String msg = String.format("Warning unable to read directory %s File.listFiles() returned null", dir);
                throw new IOException(msg);
            }
        } catch (Exception ex) {
            String msg = String.format("Warning listing directory %s: Exception: %s", Debug.getExtendedStackTrace(ex));
            LOG.error(msg, ex);
            return numericDirectories;
        }
        for (File file : files) {
            try {
                if (!file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                Long hourKey = parseLongOrNull(fileName);
                if (hourKey == null) {
                    continue; // This isn't a numerical file
                }
                numericDirectories.add(hourKey);
            } catch (Exception ex) {
                String msg = String.format("Error determing if %s is a numeric directory: Exception: %s", file.getName(), Debug.getExtendedStackTrace(ex));
                LOG.warn(msg, ex);
            }
        }
        Collections.sort(numericDirectories);
        return numericDirectories;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    private static Long parseLongOrNull(String inputString) {
        Long val;
        try {
            val = Long.parseLong(inputString);
            return val;
        } catch (Exception ex) {
            return null;
        }
    }

    public Map<Integer, LoadBalancerIdAndName> getLoadBalancerIdMap() {
        return loadBalancerIdMap;
    }

    public void setLoadBalancerIdMap(Map<Integer, LoadBalancerIdAndName> loadBalancerIdMap) {
        this.loadBalancerIdMap = loadBalancerIdMap;
    }
}
