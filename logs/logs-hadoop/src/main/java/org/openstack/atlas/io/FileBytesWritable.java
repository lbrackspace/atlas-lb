package org.openstack.atlas.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.Reporter;

import java.io.*;

public class FileBytesWritable implements WritableComparable<FileBytesWritable> {
    public static final int MAXSIZE = 10000000;
    public static final int NUMTIMES = 1000;
    public static final int BUFSIZE = 256*1024;
    private static final Log LOG = LogFactory.getLog(FileBytesWritable.class);

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

    @Override
    public void write(DataOutput d) throws IOException {
        d.writeLong(maxSize);
        byte[] buf = new byte[BUFSIZE];
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(fileName), BUFSIZE);
            if (order > 0) {
                is.skip(order * MAXSIZE);
            }

            int numTimes = 0;

            while (numTimes < NUMTIMES && is.read(buf) >= 0) {
                d.write(buf);
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

    @Override
    public void readFields(DataInput di) throws IOException {
        maxSize = di.readLong();
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
                    di.readFully(buf);
                    out.write(buf);
                } else {
                    int rest = (int) maxSize - start;
                    di.readFully(buf);
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

    @Override
    public int compareTo(FileBytesWritable o) {
        return fileName.compareTo(o.getFileName());
    }

}