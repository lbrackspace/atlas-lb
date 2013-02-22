package org.openstack.atlas.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.zip.CRC32;
import org.openstack.atlas.util.exceptions.FileUtilsException;

public class StaticFileUtils {

    private static final int DEFAULT_BUFFSIZE = 1024 * 256;

    private static final Random rnd = new Random();

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
                    double rate = (double) bytesRead / (now - startTime);
                    System.out.printf("rate=%f\n", rate);
                    System.out.flush();
                    startTime = now;
                    String fmt = "%d bytes transfered %s done Bytes left=%s: transfer rate is rate %s per second\n";
                    String bytesLeft = Debug.humanReadableBytes(isSize - totalBytesRead);
                    String byteRate = "";
                    try {
                        byteRate = Debug.humanReadableBytes(rate);
                    } catch (NumberFormatException ex) {
                        byteRate = new StringBuilder().append(rate).toString();
                    }
                    ps.printf(fmt, bytesRead, p, bytesLeft, byteRate);
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

    public static String workingDirectory() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ex) {
            return null;
        }
    }

    public static String[] splitPath(String pathName) {
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

    public static String getRestOfFilename(String fullFilename) {
        if (fullFilename.contains("/")) {
            return fullFilename.substring(fullFilename.lastIndexOf("/") + 1);
        } else {
            return fullFilename;
        }
    }
}
