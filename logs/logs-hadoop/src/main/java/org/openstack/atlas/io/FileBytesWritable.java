package org.openstack.atlas.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.Reporter;

import java.io.*;

public class FileBytesWritable implements WritableComparable<FileBytesWritable> {
    public static int MAXSIZE = 10000000;
    public static int NUMTIMES = 1000;
    public static int BUFSIZE = MAXSIZE / NUMTIMES;
    private static final Log LOG =
            LogFactory.getLog(FileBytesWritable.class);

    private String fileName;
    int order;
    long maxSize;

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    private Reporter reporter;

    // In order to not write extra bytes ths **order** must be set on this object, index to offset of the set of these.

    public void write(DataOutput out) throws IOException {
        out.writeLong(maxSize);
        byte[] buf = new byte[BUFSIZE];
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileName), BUFSIZE);
            if (order > 0) {
                is.skip(order * MAXSIZE);
            }

            int numTimes = 0;

            while (numTimes < NUMTIMES && is.read(buf) >= 0) {
                out.write(buf);
                numTimes++;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    // In order to not write extra bytes ths **order** must be set on this object, index to offset of the set of these.
    // In order to not write extra bytes ths **maxSize** must be set on this object, equal to fileSize.

    public void readFields(DataInput in) throws IOException {
        maxSize = in.readLong();
        byte[] buf = new byte[BUFSIZE];

        int start = order * MAXSIZE;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName, order > 0), BUFSIZE);
        int bufOffset = 0;
        try {
            //This will fail with a EOF Exception or hit this limit.
            for (int i = 0; i < NUMTIMES; i++) {
//                buf = new byte[BUFSIZE];

//                LOG.info("about to read " + MAXSIZE + " bytes");
//                for (bufOffset = 0 ; bufOffset < MAXSIZE ; bufOffset++) {
//                    byte b = in.readByte();
//                    buf[bufOffset] = b;
//                }
//                LOG.info("read a full buffer, "+ bufOffset + " bytes, writing to output");
//                out.write(buf, 0, bufOffset);

                if (start + BUFSIZE < maxSize) {
                    start += BUFSIZE;
                    in.readFully(buf);
                    out.write(buf);
                } else {
                    int rest = (int) maxSize - start;
                    in.readFully(buf);
                    out.write(buf, 0, rest);
                    throw new EOFException();
                }
            }
        } catch (EOFException e) {
            //write the rest of the file
            out.write(buf, 0, bufOffset);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void readBufferAndCount() {

    }


//    public final void readFully(DataInput in, byte b[], int off, int len) throws EOFException{
//        if (len < 0)
//            throw new IndexOutOfBoundsException();
//        int n = 0;
//        while (n < len) {
//            int count = in.readFully(b, off + n, len - n);
//            if (count < 0)
//                throw new EOFException();
//            n += count;
//        }
//    }

    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int compareTo(FileBytesWritable o) {
        return fileName.compareTo(o.getFileName());
    }

}