package org.openstack.atlas.cloudfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.openstack.atlas.util.converters.BitConverters;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesSegment {

    private static final int BUFFSIZE = 1024 * 16;
    private static final String MD5 = "MD5";
    private String fileName;
    private String md5sum;
    private long offset;
    private long size;

    CloudFilesSegment() {
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
        long nBytesLeft = size;
        int nBytes;
        int bytesRead;
        RandomAccessFile ra = new RandomAccessFile(new File(StaticFileUtils.expandUser(fileName)), "r");
        ra.seek(offset);
        byte[] buff = new byte[BUFFSIZE];
        MessageDigest md = MessageDigest.getInstance(MD5);
        while (nBytesLeft > 0) {
            nBytes = (nBytesLeft < BUFFSIZE) ? (int) nBytesLeft : BUFFSIZE;
            bytesRead = ra.read(buff, 0, nBytes);
            md.update(buff, 0, bytesRead);
            nBytesLeft -= bytesRead;
        }
        md5sum = BitConverters.bytes2hex(md.digest());
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "CloudFilesSegment{fileName=" + fileName + ", md5sum=" + md5sum + ", offset=" + offset + ", size=" + size + '}';
    }
}
