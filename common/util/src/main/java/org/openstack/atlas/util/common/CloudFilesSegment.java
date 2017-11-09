package org.openstack.atlas.util.common;

import org.openstack.atlas.util.common.SegmentedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesSegment {

    private static final int BUFFSIZE = 64 * 1024;
    private static final String MD5 = "MD5";
    private String fileName;
    private String md5sum;
    private int fragNumber;
    private long offset;
    private int size;

    public CloudFilesSegment() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void computeMd5sum() throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        md5sum = computeMd5SumForFile(fileName, offset, size, BUFFSIZE);
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "CloudFilesSegment{fileName=" + fileName + "." + fragNumber + ", md5sum=" + md5sum + ", offset=" + offset + ", size=" + size + '}';
    }

    public int getFragNumber() {
        return fragNumber;
    }

    public void setFragNumber(int fragNumber) {
        this.fragNumber = fragNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static String computeMd5SumForFile(String filePath, long offset, long length, int buffsize) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        long nBytesLeft = length;
        int nBytes;
        int bytesRead;
        InputStream is = null;
        String md5hex;
        try {
            is = new SegmentedInputStream(new File(StaticFileUtils.expandUser(filePath)), offset, length);
            byte[] buff = new byte[buffsize];
            MessageDigest md = MessageDigest.getInstance(MD5);
            while (nBytesLeft > 0) {
                nBytes = (nBytesLeft < buffsize) ? (int) nBytesLeft : buffsize;
                bytesRead = is.read(buff, 0, nBytes);
                if (bytesRead <= 0) {
                    break;
                }
                md.update(buff, 0, bytesRead);
                nBytesLeft -= bytesRead;
            }
            md5hex = BitConverters.bytes2hex(md.digest());
        } catch (IOException ex) {
            throw ex;
        } catch (NoSuchAlgorithmException ex) {
            throw ex;
        } finally {
            StaticFileUtils.close(is);
        }
        return md5hex;
    }

    public static int neededSegments(long totalSize, int segSize) {
        long needed = (totalSize - 1) / segSize + 1;
        return (int) needed;
    }
}
