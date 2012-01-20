package org.hexp.hibernateexp.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
public class FileUtils {
    public static byte[] readFileToBytes(String fileName) throws FileNotFoundException, IOException {
        byte[] data;
        String fmt;
        String msg;
        FileInputStream fis;
        InputStreamReader isr;
        File file;
        file = new File(fileName);
        long flen = file.length();
        if (flen > Integer.MAX_VALUE) {
            fmt = "can not read more then %d bytes\n";
            msg = String.format(fmt, Integer.MAX_VALUE);
            throw new IOException(msg);
        }
        fis = new FileInputStream(file);
        data = new byte[(int) flen];
        fis.read(data, 0, (int) flen);
        fis.close();
        return data;
    }

    public static void writeBytesToFile(String fileName, byte[] data) throws IOException {
        File file;
        FileOutputStream fs;
        DataOutputStream ds;
        file = new File(fileName);
        fs = new FileOutputStream(file);
        ds = new DataOutputStream(fs);
        ds.write(data);
        ds.flush();
        ds.close();
    }
}
