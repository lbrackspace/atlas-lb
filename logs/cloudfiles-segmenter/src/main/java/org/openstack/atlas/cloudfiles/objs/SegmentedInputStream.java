package org.openstack.atlas.cloudfiles.objs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class SegmentedInputStream extends InputStream {

    private RandomAccessFile ra;
    long bytesLeft;

    public SegmentedInputStream(File filePath, long offset, long bytesLeft) throws FileNotFoundException, IOException {
        ra = new RandomAccessFile(filePath, "r");
        ra.seek(offset);
        this.bytesLeft = bytesLeft;
    }

    @Override
    public int read() throws IOException {
        if (bytesLeft <= 0) {
            return -1;
        }
        int data = ra.read();
        if (data >= 0) {
            bytesLeft--;
        }
        return data;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesLeft <= 0) {
            return -1;
        }
        int needBytes = len;
        int nBytesRead;
        if (bytesLeft < needBytes) {
            needBytes = (int) bytesLeft;
        }
        nBytesRead = ra.read(b, off, needBytes);
        if (nBytesRead > 0) {
            bytesLeft -= nBytesRead;
        }
        //System.out.printf("read %d bytes\n", nBytesRead);
        return nBytesRead;
    }

    @Override
    public void close() throws IOException {
        ra.close();
    }
}
