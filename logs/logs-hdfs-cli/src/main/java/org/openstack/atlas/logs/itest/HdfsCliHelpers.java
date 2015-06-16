package org.openstack.atlas.logs.itest;

import org.openstack.atlas.util.staticutils.StaticStringUtils;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.hadoop.fs.FileStatus;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;
import org.openstack.atlas.util.debug.Debug;

public class HdfsCliHelpers {

    private static final int BUFFSIZE = 4096 * 8;

    public static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return cal;
    }

    public static void writeStringToFile(String fileName, String strOut) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileOutputStream os = new FileOutputStream(StaticFileUtils.expandUser(fileName));
        byte[] data = strOut.getBytes("utf-8");
        int ep = data.length;
        int currentFilePosition = 0;
        while (currentFilePosition < ep) {
            int nBytesLeft = ep - currentFilePosition;
            int nBytes = (nBytesLeft > BUFFSIZE) ? BUFFSIZE : nBytesLeft;
            os.write(data, currentFilePosition, nBytes);
            currentFilePosition += nBytes;
        }
        os.close();

    }

    public static String readFileToString(String fileName) throws FileNotFoundException, IOException {
        byte[] buff;
        byte[] bytesIn;
        int nbytes;
        FileInputStream is = new FileInputStream(StaticFileUtils.expandUser(fileName));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while (true) {
            buff = new byte[BUFFSIZE];
            nbytes = is.read(buff);
            if (nbytes < 0) {
                break;
            }
            os.write(buff, 0, nbytes);
        }
        bytesIn = os.toByteArray();
        is.close();
        os.close();
        return new String(bytesIn, "utf-8");
    }

    public static String getCalendarString(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS");
        SimpleDateFormat timeFormat = new SimpleDateFormat("z");

        String dateStr = dateFormat.format(cal.getTime());
        String zoneStr = timeFormat.format(cal.getTime());
        String readableString = String.format("%s %s", dateStr, zoneStr);
        return readableString;
    }

    public static Date currDate() {
        return new Date(System.currentTimeMillis());
    }

    public static String dateString(Date date) {
        return getCalendarString(dateToCalendar(date));
    }

    public static String currDateString() {
        return dateString((currDate()));
    }

    public static String displayFileStatus(FileStatus stat) {
        String dir = (stat.isDir()) ? "d" : "-";
        String path = stat.getPath().toUri().getPath();
        String perms = stat.getPermission().toString();
        String owner = StaticStringUtils.lpad(stat.getOwner() + ":" + stat.getGroup(), " ", 60);
        String size = StaticStringUtils.lpadLong(stat.getLen(), " ", 13);
        String blockSize = StaticStringUtils.lpadLong(stat.getBlockSize(), " ", 10);
        String reps = StaticStringUtils.lpadLong(stat.getReplication(), " ", 6);
        String modStr = StaticStringUtils.lpad(dateString(new Date(stat.getModificationTime())), " ", 24);
        String msg = dir + perms + " " + owner + " " + size + blockSize + reps + " " + modStr + " " + path;
        return msg;
    }

    public static String confKV(Configuration conf) {
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<String>();
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<String, String> ent : conf) {
            keys.add(ent.getKey());
            map.put(ent.getKey(), ent.getValue());
        }
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(String.format("%s=%s\n", key, map.get(key)));
        }
        return sb.toString();
    }

    public static long countLines(String fileName, int tic, int buffsize) throws FileNotFoundException, IOException {
        long i = 0;
        String filePath = fileName.replace("~", System.getProperty("user.home"));
        File file = new File(filePath);
        long fSize = file.length();
        long nRead = 0;
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr, buffsize);
        double secs = Debug.getEpochSeconds();
        while (true) {
            if (i % tic == 0) {
                double ratio = (double) nRead / (double) fSize;
                double delta = Debug.getEpochSeconds();
                System.out.printf("line %d and %d bytes of %d left read %f %s\n", i, nRead, fSize, ratio, delta - secs);
                secs = delta;
            }
            String line = br.readLine();
            if (line == null) {
                break;
            }
            nRead += line.length() + 1;
            i++;
        }
        br.close();
        return i;
    }

    public static DataOutputStream openDataOutputStream(String fileName) throws FileNotFoundException {
        return new DataOutputStream(new FileOutputStream(new File(StaticFileUtils.expandUser(fileName))));
    }

    public static DataInputStream openDataInputStream(String fileName) throws FileNotFoundException {
        return new DataInputStream(new FileInputStream(new File(StaticFileUtils.expandUser(fileName))));
    }

    public static void indexFile(InputStream is, DataOutputStream os, int buffsize) throws IOException {
        long offset = 0;
        long nLines = 0;
        os.writeLong(0L);
        while (is.available() > 0) {
            byte[] buff = new byte[buffsize];
            int nbytes = is.read(buff);
            for (int i = 0; i < nbytes; i++) {
                if ((char) buff[i] == '\n') {
                    os.writeLong(offset + (long) i);
                    nLines++;
                    if (nLines % 100000 == 0) {
                        System.out.printf("Indexed %d lines\n", nLines);
                    }
                }
            }
            offset += (long) nbytes;
        }
    }

    public static String printLineNumber(InputStream is, DataInputStream os, long lineNumber) throws IOException {
        os.skip(lineNumber * 8L);
        long offset = os.readLong();
        is.reset();
        is.skip(offset);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (is.available() > 0) {
            char val = (char) is.read();
            if (val == '\n') {
                break;
            }
            bos.write(val);
        }
        return new String(bos.toByteArray(), "utf-8");
    }

    public static Map<String, String> getConfigurationMap(Configuration conf) {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry<String, String> ent : conf) {
            map.put(ent.getKey(), ent.getValue());
        }
        return map;
    }
}
