package org.openstack.atlas.cloudfiles.objs;

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
    private int fragNumber;
    private long offset;
    private int size;

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
        md5sum = StaticFileUtils.computeMd5SumForFile(fileName, offset, size, BUFFSIZE);
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
}
