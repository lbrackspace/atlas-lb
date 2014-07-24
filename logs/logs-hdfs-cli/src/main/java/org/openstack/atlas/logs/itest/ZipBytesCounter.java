package org.openstack.atlas.logs.itest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ZipBytesCounter {

    private int zipCount = 0;
    private long nLines = 0;
    private long zipBytes = 0;
    private long uncompressedBytes = 0;

    public ZipBytesCounter(int zipCount, long zipBytes, long uncompressedBytes, long nLines) {
        this.zipCount = zipCount;
        this.zipBytes = zipBytes;
        this.uncompressedBytes = uncompressedBytes;
        this.nLines = nLines;
    }

    public ZipBytesCounter() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZipBytesCounter other = (ZipBytesCounter) obj;
        if (this.zipCount != other.zipCount) {
            return false;
        }
        if (this.zipBytes != other.zipBytes) {
            return false;
        }
        if (this.uncompressedBytes != other.uncompressedBytes) {
            return false;
        }
        if (this.nLines != other.nLines) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.zipCount;
        hash = 97 * hash + (int) (this.zipBytes ^ (this.zipBytes >>> 32));
        hash = 97 * hash + (int) (this.nLines ^ (this.nLines >>> 32));
        hash = 97 * hash + (int) (this.uncompressedBytes ^ (this.uncompressedBytes >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "{ zipCount=" + zipCount + ", zipBytes= "
                + zipBytes + ", uncompressedBytes=" + uncompressedBytes
                + ", nLines = " + nLines + "}";
    }

    public long getnLines() {
        return nLines;
    }

    public void setnLines(long nLines) {
        this.nLines = nLines;
    }

    public long incnLines(long nLines) {
        this.nLines += nLines;
        return this.nLines;
    }

    public int getZipCount() {
        return zipCount;
    }

    public void setZipCount(int zipCount) {
        this.zipCount = zipCount;
    }

    public int incZipCount(int zipCount) {
        this.zipCount += zipCount;
        return this.zipCount;
    }

    public long getZipBytes() {
        return zipBytes;
    }

    public long incZipBytes(long zipBytes) {
        this.zipBytes += zipBytes;
        return this.zipBytes;
    }

    public void setZipBytes(long zipBytes) {
        this.zipBytes = zipBytes;
    }

    public long getUncompressedBytes() {
        return uncompressedBytes;
    }

    public long incUncompressedBytes(long uncompressedBytes) {
        this.uncompressedBytes += uncompressedBytes;
        return this.uncompressedBytes;
    }

    public void setUncompressedBytes(long uncompressedBytes) {
        this.uncompressedBytes = uncompressedBytes;
    }

    public void clear() {
        zipCount = 0;
        zipBytes = 0;
        uncompressedBytes = 0;
    }

    public static ZipBytesCounter countZips(Map<String, ZipBytesCounter> zipCounters) {
        int nZips = 0;
        long totalZipBytes = 0;
        long totalUncompressedBytes = 0;
        long nLines = 0;
        for (Entry<String, ZipBytesCounter> counter : zipCounters.entrySet()) {
            nZips += counter.getValue().getZipCount();
            totalZipBytes += counter.getValue().getZipBytes();
            totalUncompressedBytes += counter.getValue().getUncompressedBytes();
            nLines += counter.getValue().getnLines();
        }
        return new ZipBytesCounter(nZips, totalZipBytes, totalUncompressedBytes, nLines);
    }

    public static ZipBytesCounter countZips(List<ZipBytesCounter> zipCounters) {
        int nZips = 0;
        long totalZipBytes = 0;
        long totalUncompressedBytes = 0;
        long nLines = 0;
        for (ZipBytesCounter counter : zipCounters) {
            nZips += counter.zipCount;
            totalZipBytes += counter.getZipBytes();
            totalUncompressedBytes += counter.getUncompressedBytes();
            nLines += counter.getnLines();
        }
        return new ZipBytesCounter(nZips, totalZipBytes, totalUncompressedBytes, nLines);
    }
}
