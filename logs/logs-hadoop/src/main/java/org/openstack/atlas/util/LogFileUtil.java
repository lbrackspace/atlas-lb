package org.openstack.atlas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogFileUtil {

    private static final Log LOG = LogFactory.getLog(LogFileUtil.class);

    public static DateFormat filedf = new SimpleDateFormat("yyyyMMddHH");//2011021513
    public static DateFormat jobdf = new SimpleDateFormat("yyyyMMdd-HHmmss"); //20110215-130916

    public static Date getDateFromFileName(String fileName) {
        String dateString = getDateStringFromFileName(fileName);
        return getDate(dateString, filedf);
    }

    public static String getDateStringFromFileName(String fileName) {
        // /var/log/zxtm/rotated/2011021513-access_log.aggregated
        int start = fileName.lastIndexOf("/");
        fileName = fileName.substring(start + 1, fileName.length());
        String arr[] = fileName.split("-");
        String dateString = arr[0];
        if (!dateString.matches("\\d{10}")) {
            throw new IllegalArgumentException("File names must be in the following format: 'yyyyMMddHH-access_log.aggregated' eg. 2011021513-access_log.aggregated");
        }
        return dateString;
    }

    public static String getNewestFile(List<String> fileNames) {
        Collections.sort(fileNames, new Comparator<String>() {
            public int compare(String s1, String s2) {
                try {
                    Date d1 = getDateFromFileName(s1);
                    Date d2 = getDateFromFileName(s2);
                    return d2.compareTo(d1);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        return fileNames.get(0);
    }

    public static String getTotalTimeTaken(String dateStart) {
        Date startDate = getDate(dateStart, jobdf);
        Date now = Calendar.getInstance().getTime();
        long diff = now.getTime() - startDate.getTime();
        String timeTaken = Long.toString((diff / 1000));
        return timeTaken;
    }

    public static Date getDate(String dateString, DateFormat format) {
        Date startDate = new Date();
        try {
            startDate = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    public static String getMonthYearFromFileDate(String dateString) {
        String monthYear = "";
        try {
            Date date = filedf.parse(dateString);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int year = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH);

            String month = "invalid";
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] months = dfs.getShortMonths();
            if (m >= 0 && m <= 11) {
                month = months[m];
            }
            monthYear = month + "_" + year;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return monthYear;
    }

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 1
     */
    public static String getAccountId(String absoluteFileName) {
        String accountDirectory = absoluteFileName.substring(0, absoluteFileName.lastIndexOf("/"));
        String accountId = accountDirectory.substring(accountDirectory.lastIndexOf("/") + 1, accountDirectory.length());
        return accountId;
    }

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 10
     */
    public static String getLoadBalancerId(String absoluteFileName) {
        return absoluteFileName.split("_")[2];
    }

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 2012021005
     */
    public static String getLogFileTime(String absoluteFileName) {
        String accountDirectory = absoluteFileName.substring(0, absoluteFileName.lastIndexOf("/"));
        String logFileTimeDirectory = accountDirectory.substring(0, accountDirectory.lastIndexOf("/"));
        String logFileTime = logFileTimeDirectory.substring(logFileTimeDirectory.lastIndexOf("/") + 1, logFileTimeDirectory.length());
        return logFileTime;
    }

    /**
     * /var/log/zxtm/hadoop/cache ==> (/var/log/zxtm/hadoop/cache/2012021005/1, /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip)
     */
    public static Map<String, List> getLocalCachedFiles(String cacheLocation) {
        Map<String, List> map = new HashMap<String, List>();

        File folder = new File(cacheLocation);
        File[] runtimes = folder.listFiles();

        for (File runtime : runtimes) {
            if (runtime.isDirectory()) {
                File[] accounts = runtime.listFiles();
                for (File account : accounts) {
                    if (account.isDirectory()) {
                        File[] zippedFiles = account.listFiles();
                        List<String> filesLog = new ArrayList<String>();
                        for (File logFile : zippedFiles) {
                            if (logFile.getName().endsWith(".zip")) {
                                filesLog.add(logFile.getAbsolutePath());

                            }
                        }
                        map.put(account.getAbsolutePath(), filesLog);
                    }
                }
            }
        }
        return map;
    }

    public static void deleteLocalFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.isDirectory() && file.listFiles().length == 0) {
                file.delete();
            } else if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory() && file.listFiles().length > 0) {
                LOG.debug(file.listFiles().length + " existing files inside " + fileName + ". Hence unable to clean up dir.");
            }
        } catch (Exception e) {
            LOG.error("Error deleting file after uploading to CloudFiles: " + fileName, e);
        }
    }

    public static void deleteFilesOlderThanNDays(String location, int nDays) {
        long purgeTime = System.currentTimeMillis() - (nDays * 24L * 60L * 60L * 1000L);
        delete(location, purgeTime);
    }

    public static void delete(String location, long purgeTime) {
        File file = new File(location);
        if (file.isDirectory()) {
            for (File a : file.listFiles())
                delete(a.getAbsolutePath(), purgeTime);
        }
        if(file.lastModified() < purgeTime && !file.getAbsolutePath().equals(location)) {
            if(!file.delete()) {
                LOG.debug("Unable to delete file: " + file);
            } else {
                LOG.info("Deleted an old log file for cleanup. FileName: " + file.getAbsolutePath() + " Last Modified: " + new Date(file.lastModified()));
            }
        }

    }
}
