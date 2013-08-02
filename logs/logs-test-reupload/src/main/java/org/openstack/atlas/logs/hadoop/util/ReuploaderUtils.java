package org.openstack.atlas.logs.hadoop.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.openstack.atlas.service.domain.pojos.LoadBalancerIdAndName;
import org.openstack.atlas.util.common.VerboseLogger;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class ReuploaderUtils {

    private String cacheDir;
    private Map<Integer, LoadBalancerIdAndName> loadBalancerIdMap;
    private static final VerboseLogger vlog = new VerboseLogger(ReuploaderUtils.class, VerboseLogger.LogLevel.INFO);
    private static final Log LOG = LogFactory.getLog(ReuploaderUtils.class);
    private static final Comparator<CacheZipInfo> defaultZipInfoComparator = new CacheZipInfo.ZipComparator();
    private static final Comparator<CacheZipDirInfo> defaultZipDirInfoComparator = new CacheZipDirInfo.HourAccountComparator();

    public ReuploaderUtils(String cacheDir, Map<Integer, LoadBalancerIdAndName> loadBalancerIdMap) {
        this.cacheDir = cacheDir;
        this.loadBalancerIdMap = loadBalancerIdMap;
    }

    public List<CacheZipDirInfo> getLocalZipDirInfo(long endHour) {
        return getLocalCacheZipDirInfo(cacheDir, endHour);
    }

    public static long getCurrentHourKeyMinusHours(int hours) {
        DateTime dt = StaticDateTimeUtils.nowDateTime(true).plusHours(0 - hours);
        return StaticDateTimeUtils.dateTimeToHourLong(dt);
    }

    public static void deleteIfDirectoryIsEmpty(String path) {
        File[] subFiles = null;
        File file = new File(path);
        try {

            if (!file.isDirectory()) {
                return; // This isn't even a directory lets skip it\n"
            }
            subFiles = file.listFiles();
            if (subFiles == null) {
                LOG.warn(String.format("Could not determine if directory %s was empty. new File(%s).listFiles() returned null", path));
                return;
            }
        } catch (Exception ex) {
            LOG.warn(String.format("Could not determine if directory %s was empty: Exception ex", path, Debug.getExtendedStackTrace(ex)), ex);
            return;
        }

        try {
            if (subFiles.length <= 0) { // If the directories empty then try to delete it
                if (!file.delete()) {
                    throw new IOException();
                }
            }
        } catch (Exception ex) {
            LOG.warn(String.format("Could not delete empty directory %s", path), ex);
            return;
        }

    }

    public void clearDirs(int minusHours) {
        clearDirs(cacheDir, minusHours);
    }

    public static void clearDirs(String cacheDir, int minusHours) {
        long stopHourKey = getCurrentHourKeyMinusHours(minusHours);
        // First pass clear the empty zip directories

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
                deleteIfDirectoryIsEmpty(zipDirPath);
            }
        }

        // Second pass delete the empty hour directories
        for (Long hourKey : hourKeys) {
            String hourKeyDirPath = StaticFileUtils.mergePathString(cacheDir, hourKey.toString());
            deleteIfDirectoryIsEmpty(hourKeyDirPath);
        }
    }

    public static List<CacheZipDirInfo> getLocalCacheZipDirInfo(String cacheDir, long endHour) {
        List<CacheZipDirInfo> localCacheZipDirInfo = new ArrayList<CacheZipDirInfo>();
        List<Long> hourKeys = new ArrayList<Long>();
        for (Long hourKey : listHourDirectories(cacheDir)) {
            if (hourKey > endHour) {
                continue;
            }
            vlog.printf("Scanning %d", hourKey);
            for (Long accountId : listAccountDirectories(cacheDir, hourKey)) {
                CacheZipDirInfo accountDirInfo = new CacheZipDirInfo();
                String accountPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hourKey), Long.toString(accountId));
                accountDirInfo.setDirName(accountPath);
                accountDirInfo.setAccountId((accountId.intValue()));
                accountDirInfo.setHourKey(hourKey);
                accountDirInfo.setZipCount(0);
                localCacheZipDirInfo.add(accountDirInfo);
                accountDirInfo.setZips(getLocalCacheZips(cacheDir, hourKey, accountId.intValue()));
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
                    String zipPath = StaticFileUtils.mergePathString(cacheDir, Long.toString(hourKey), Integer.toString(accountId));
                    zipInfo.setLoadbalancerId(Integer.parseInt(loadBalancerIdStr));
                    zipInfo.setFileSize(zipFile.length());
                    zipInfo.setZipFile(zipPath);
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
