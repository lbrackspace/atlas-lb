package org.openstack.atlas.util.staticutils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.debug.Debug;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.openstack.atlas.util.common.exceptions.FileUtilsException;

public class StaticFileUtils {

    private static final Log LOG = LogFactory.getLog(StaticFileUtils.class);
    public static DateFormat filedf = new SimpleDateFormat("yyyyMMddHH");//2011021513
    public static DateFormat jobdf = new SimpleDateFormat("yyyyMMdd-HHmmss"); //20110215-130916
    private static final int DEFAULT_BUFFSIZE = 1024 * 256;
    private static final int PAGESIZE = 4096;
    private static final Random rnd = new Random();

    public static synchronized String generateRandomBase() {
        return "-" + rnd.nextLong() + ".tmp";
    }

    public static String expandUser(String pathIn) {
        return pathIn.replace("~", System.getProperty("user.home"));
    }

    public static DataOutputStream openDataOutputStreamFile(String fileName) throws FileNotFoundException {
        return openDataOutputStreamFile(fileName, DEFAULT_BUFFSIZE);
    }

    public static DataInputStream openDataInputStreamFile(String fileName) throws FileNotFoundException {
        return openDataInputStreamFile(fileName, DEFAULT_BUFFSIZE);
    }

    public static DataOutputStream openDataOutputStreamFile(String fileName, int buffsize) throws FileNotFoundException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(expandUser(fileName))), buffsize));
    }

    public static DataInputStream openDataInputStreamFile(String fileName, int buffsize) throws FileNotFoundException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(new File(expandUser(fileName))), buffsize));
    }

    public static byte[] readFile(File file) throws FileNotFoundException, IOException {
        byte[] bytesOut;
        byte[] buff;
        int nbytes;
        FileInputStream is = new FileInputStream(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (true) {
            buff = new byte[DEFAULT_BUFFSIZE];
            nbytes = is.read(buff);
            if (nbytes < 0) {
                break;
            }
            os.write(buff, 0, nbytes);
        }
        bytesOut = os.toByteArray();
        is.close();
        os.close();
        return bytesOut;
    }

    public static byte[] readFile(String fileName) throws FileNotFoundException, IOException {
        return readFile(new File(expandUser(fileName)));
    }

    public static void copyStreams(InputStream is, OutputStream os, PrintStream ps, long isSize, int buffsize) throws IOException {
        byte[] data;
        long totalBytesRead = 0;
        long bytesRead = 0;
        double startTime = Debug.getEpochSeconds();
        String percentStr = "0%";
        while (true) {
            data = new byte[buffsize];
            int nread = is.read(data);
            if (nread == -1) {
                break;// EOF
            }
            totalBytesRead += nread;
            bytesRead += nread;
            os.write(data, 0, nread);
            if (ps != null) {
                double percentVal = 100.0 * ((double) totalBytesRead / (double) isSize);
                String p = String.format("%.0f%%", percentVal);
                if (!p.equals(percentStr)) {
                    double now = Debug.getEpochSeconds();
                    double timeDelta = now - startTime;
                    double rate = (double) bytesRead / (timeDelta);
                    System.out.printf("rate=%f\n", rate);
                    System.out.flush();
                    startTime = now;
                    String fmt = "%.4f(secs) %d bytes transfered %s done Bytes left=%s: transfer rate is rate %s per second\n";
                    String bytesLeft = Debug.humanReadableBytes(isSize - totalBytesRead);
                    String byteRate = "";
                    try {
                        byteRate = Debug.humanReadableBytes(rate);
                    } catch (NumberFormatException ex) {
                        byteRate = new StringBuilder().append(rate).toString();
                    }
                    ps.printf(fmt, timeDelta, bytesRead, p, bytesLeft, byteRate);
                    bytesRead = 0;
                    ps.flush();
                    percentStr = p;
                }
            }
        }
    }

    public static void copyStreams(InputStream is, OutputStream os, PrintStream ps, int buffsize) throws IOException {
        byte[] data;
        long totalBytesRead = 0;
        long totalBytes = is.available();
        long nwritten = 0;
        String percentStr = "0%";
        double startTime = Debug.getEpochSeconds();
        while (true) {
            data = new byte[buffsize];
            int nread = is.read(data);
            if (nread == -1) {
                break;//EOF
            }
            totalBytesRead += nread;
            os.write(data, 0, nread);
            nwritten += nread;
            if (ps != null) {
                double percentVal = 100.0 * ((double) totalBytesRead / (double) totalBytes);
                String p = String.format("%.0f%%", percentVal);
                if (!p.equals(percentStr)) {
                    String fmt = "%s bytes transfered: wrote %d bytes %d bytes left. totalBytes read %s rate is %s per seconds\n";
                    double now = Debug.getEpochSeconds();
                    double rate = (double) nwritten / (now - startTime);
                    startTime = now;
                    ps.printf(fmt, p, nwritten, is.available(), Debug.humanReadableBytes(totalBytesRead), Debug.humanReadableBytes(rate));
                    nwritten = 0;
                    ps.flush();
                    percentStr = p;
                }
            }
        }
    }

    public static OutputStream openOutputFile(String fileName) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(new File(expandUser(fileName))), DEFAULT_BUFFSIZE);
    }

    public static OutputStream openOutputFile(String fileName, int buffsize) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(new File(expandUser(fileName))), buffsize);
    }

    public static InputStream openInputFile(String fileName) throws FileNotFoundException {
        return openInputFile(fileName, DEFAULT_BUFFSIZE);
    }

    public static InputStream openInputFile(String fileName, int buffsize) throws FileNotFoundException {
        String expandedUserFilePath = expandUser(fileName);
        File file = new File(expandedUserFilePath);
        return new BufferedInputStream(new FileInputStream(new File(expandUser(fileName))), buffsize);
    }

    public static String getWorkingDirectory() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ex) {
            return null;
        }
    }

    public static List<String> joinPath(List<String> paths) {
        List<String> joined = new ArrayList<String>();
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            String[] pathComps = splitPath(path);
            for (int j = 0; j < pathComps.length; j++) {
                String comp = pathComps[j];
                if (comp.length() == 0 && !(j == 0 && i == 0)) {
                    continue; // Skip blanks for all but the joined Root element
                }
                joined.add(comp);
            }
        }
        return joined;
    }

    public static List<String> splitPathList(String pathName) {
        List<String> pathList = new ArrayList<String>();
        File file = new File(pathName);
        while (true) {
            if (file == null) {
                break;
            }
            String name = file.getName();
            pathList.add(name);
            file = file.getParentFile();
        }
        return pathList;
    }

    public static String[] splitPath(String pathName) {
        List<String> pathList = splitPathList(pathName);
        Collections.reverse(pathList);
        return pathList.toArray(new String[pathList.size()]);
    }

    public static String joinPath(String firstPart, String secondPart) {
        return splitPathToString(joinPath(splitPath(firstPart), splitPath(secondPart)));
    }

    public static String[] joinPath(String[] firstPart, String[] secondPart) {
        int firstLen = firstPart.length;
        int secondLen = secondPart.length;
        int i;
        String[] newPath = new String[firstLen + secondLen];
        for (i = 0; i < firstLen; i++) {
            newPath[i] = firstPart[i];
        }
        for (i = 0; i < secondLen; i++) {
            newPath[i + firstLen] = secondPart[i];
        }
        return newPath;
    }

    public static String splitPathToString(List<String> pathComps) {
        StringBuilder sb = new StringBuilder();
        if (pathComps.isEmpty()) {
            return null;
        }
        if (pathComps.size() == 1) {
            return pathComps.get(0);
        }

        for (int i = 0; i < pathComps.size() - 1; i++) {
            sb.append(pathComps.get(i)).append(File.separator);
        }
        sb.append(pathComps.get(pathComps.size() - 1));
        return sb.toString();
    }

    public static String splitPathToString(String[] splitPath) {
        StringBuilder sb = new StringBuilder();
        if (splitPath.length == 0) {
            return null;
        }
        if (splitPath.length == 1) {
            return splitPath[0];
        }

        for (int i = 0; i < splitPath.length - 1; i++) {
            sb.append(splitPath[i]).append(File.separator);
        }
        sb.append(splitPath[splitPath.length - 1]);
        return sb.toString();
    }

    public static String rebaseSplitPath(String srcBase, String srcPath, String dstBase) throws FileUtilsException {
        String[] srcBaseArr = splitPath(srcBase);
        String[] srcPathArr = splitPath(srcPath);
        String[] dstBaseArr = splitPath(dstBase);
        String[] rebasedPath = rebaseSplitPath(srcBaseArr, srcPathArr, dstBaseArr);
        String rebasePathString = splitPathToString(rebasedPath);
        return rebasePathString;
    }

    public static String[] rebaseSplitPath(String[] srcBase, String[] srcPath, String[] dstBase) throws FileUtilsException {
        int srcBaseLen = srcBase.length;
        int srcPathLen = srcPath.length;
        int dstBaseLen = dstBase.length;
        int i;
        int j;
        if (srcPathLen < srcBaseLen) {
            throw new FileUtilsException("srcPath is smaller then srcBase");
        }
        for (i = 0; i < srcBaseLen; i++) {
            if (!srcBase[i].equals(srcPath[i])) {
                throw new FileUtilsException("srcPath does not include srcBase");
            }
        }
        int deltaLen = srcPathLen - srcBaseLen;
        int rebasedLength = dstBaseLen + deltaLen;
        String[] rebasedPath = new String[rebasedLength];
        System.arraycopy(dstBase, 0, rebasedPath, 0, dstBaseLen);
        System.arraycopy(srcPath, srcBaseLen, rebasedPath, dstBaseLen, srcPathLen - srcBaseLen);
        return rebasedPath;
    }

    public static String[] stripBeginingPath(String[] splitPath, int nTimes) {
        if (splitPath.length <= nTimes) {
            return new String[0];
        }

        int newLength = splitPath.length - nTimes;
        String[] newPath = new String[newLength];
        for (int i = 0; i < newLength; i++) {
            newPath[i] = splitPath[nTimes + i];
        }
        return newPath;
    }

    public static String pathTail(String path) {
        if (path == null) {
            return null;
        }
        String[] pathComps = splitPath(path);
        if (pathComps == null || pathComps.length <= 0) {
            return null;
        }
        return pathComps[pathComps.length - 1];
    }

    public static String[] stripEndPath(String[] splitPath, int nTimes) {
        if (splitPath.length <= nTimes) {
            return new String[0];
        }
        int newLen = splitPath.length - nTimes;
        String[] newPath = new String[newLen];
        System.arraycopy(splitPath, 0, newPath, 0, newLen);
        return newPath;
    }

    public static String listDir(String path) {
        if (path == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(path).append(":{");
        File[] files = new File(path).listFiles();
        if (files.length <= 0) {
            sb.append("}");
            return sb.toString();
        }
        int i;
        for (i = 0; i < files.length - 1; i++) {
            sb.append(files[i].toString()).append(",");
        }
        sb.append(files[i]).append("}");
        return sb.toString();
    }

    // Be careful your not reading a huge file
    public static byte[] readInputStream(InputStream fis, int buffSize) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreams(fis, bos, null, -1, buffSize);
        return bos.toByteArray();
    }

    public static byte[] readInputStream(InputStream fis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreams(fis, bos, null, -1, DEFAULT_BUFFSIZE);
        return bos.toByteArray();
    }

    public static long computeCrc(InputStream fis) throws IOException {
        int nBytes;
        byte[] data = new byte[DEFAULT_BUFFSIZE];
        CRC32 crc = new CRC32();
        while (true) {
            nBytes = fis.read(data, 0, DEFAULT_BUFFSIZE);
            if (nBytes < 0) {
                break;
            }
            crc.update(data, 0, nBytes);
        }
        return crc.getValue();
    }

    public static void mkParentDir(String filePath) {
        String expandPath = expandUser(filePath);
        File fp = new File(expandPath);
        fp.getParentFile().mkdirs();
    }

    public static Properties loadProperties(String propertiesPath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(propertiesPath));
        return properties;
    }

    public static String sanitizeDir(String dir) {
        String sanitized = dir;
        if (sanitized.contains("-*")) {
            sanitized = sanitized.replace("-*", "");
        }
        return sanitized;
    }

    public static String stripDirectoryFromFileName(String fullFilePath) {
        String[] pathComponents = splitPath(fullFilePath);
        if (pathComponents == null || pathComponents.length == 0) {
            return "";
        }
        return pathComponents[pathComponents.length - 1];
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

    /**
     * /var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip => 2012021005
     */
    public static String getLogFileTime(String absoluteFileName) {
        String accountDirectory = absoluteFileName.substring(0, absoluteFileName.lastIndexOf("/"));
        String logFileTimeDirectory = accountDirectory.substring(0, accountDirectory.lastIndexOf("/"));
        String logFileTime = logFileTimeDirectory.substring(logFileTimeDirectory.lastIndexOf("/") + 1, logFileTimeDirectory.length());
        return logFileTime;
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
            for (File a : file.listFiles()) {
                delete(a.getAbsolutePath(), purgeTime);
            }
        }
        if (file.lastModified() < purgeTime && !file.getAbsolutePath().equals(location)) {
            if (!file.delete()) {
                LOG.debug("Unable to delete file: " + file);
            } else {
                LOG.info("Deleted an old log file for cleanup. FileName: " + file.getAbsolutePath() + " Last Modified: " + new Date(file.lastModified()));
            }
        }

    }

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

    public static BufferedReader inputStreamToBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is), DEFAULT_BUFFSIZE);
    }

    public static Random getRnd() {
        return rnd;
    }

    public static boolean isSymLink(String filePath) throws IOException {
        File file = new File(expandUser(filePath));
        return org.apache.commons.io.FileUtils.isSymlink(file);
    }

    public static void close(Closeable is) {
        try {
            is.close();
        } catch (Exception ex) {
            // Not logging since the stream is likely already closed
        }
    }

    public static BufferedReader inputStreamToBufferedReader(InputStream is, int buffSize) {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr, buffSize);
        return br;
    }

    public static String mergePathString(String... pathArray) {
        List<String> pathList = new ArrayList<String>();
        pathList.addAll(Arrays.asList(pathArray));
        return StaticFileUtils.splitPathToString(StaticFileUtils.joinPath(pathList));
    }

    public static byte[] compressBytes(byte[] bytesIn) throws IOException {
        byte[] bytesOut;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream gzOs = new GZIPOutputStream(os,PAGESIZE);
        gzOs.write(bytesIn);
        gzOs.close();
        bytesOut = os.toByteArray();
        return bytesOut;
    }

    public static byte[] decompressBytes(byte[] bytesIn) throws IOException {
        byte[] bytesOut;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayInputStream is = new ByteArrayInputStream(bytesIn);
        GZIPInputStream gzIs = new GZIPInputStream(is,PAGESIZE);
        StaticFileUtils.copyStreams(gzIs, os, null, PAGESIZE);
        bytesOut = os.toByteArray();
        return bytesOut;
    }
}
