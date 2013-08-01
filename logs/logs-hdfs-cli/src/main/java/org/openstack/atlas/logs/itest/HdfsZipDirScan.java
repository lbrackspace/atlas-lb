package org.openstack.atlas.logs.itest;

import java.util.HashSet;
import java.util.Set;

public class HdfsZipDirScan {

    private String hourKey;
    private boolean dateDirFound = false;
    private boolean zipDirFound = false;
    private boolean partionFilesFound = false;
    private long zipByteCount = 0L;
    private int zipCount = 0;
    private int partZipCount = 0;
    private Set<String> partZipsFound;
    private Set<String> zipsFound;

    public String displayString() {
        String fmt = "%s zipCount=%d zipsInPartitionCount=%d dirFound=%s outputPartitionsFound = %s zipsDirFound=%s zipBytes=%d";
        return String.format(fmt, hourKey, zipCount, partZipCount, dateDirFound, partionFilesFound, zipDirFound, zipByteCount);
    }

    public HdfsZipDirScan() {
    }

    public boolean isDateDirFound() {
        return dateDirFound;
    }

    public void setDateDirFound(boolean dateDirFound) {
        this.dateDirFound = dateDirFound;
    }

    public boolean isZipDirFound() {
        return zipDirFound;
    }

    public void setZipDirFound(boolean zipDirFound) {
        this.zipDirFound = zipDirFound;
    }

    public boolean isPartionFilesFound() {
        return partionFilesFound;
    }

    public void setPartionFilesFound(boolean partionFilesFound) {
        this.partionFilesFound = partionFilesFound;
    }

    public int getZipCount() {
        return zipCount;
    }

    public void setZipCount(int zipCount) {
        this.zipCount = zipCount;
    }

    public int getPartZipCount() {
        return partZipCount;
    }

    public void setPartZipCount(int partZipCount) {
        this.partZipCount = partZipCount;
    }

    public String getHourKey() {
        return hourKey;
    }

    public void setHourKey(String hourKey) {
        this.hourKey = hourKey;
    }

    public Set<String> getZipsFound() {
        if (zipsFound == null) {
            zipsFound = new HashSet<String>();
        }
        return zipsFound;
    }

    public void setZipsFound(Set<String> zipsFound) {
        this.zipsFound = zipsFound;
    }

    public Set<String> getPartZipsFound() {
        if (partZipsFound == null) {
            partZipsFound = new HashSet<String>();
        }
        return partZipsFound;
    }

    public void setPartZipsFound(Set<String> partZipsFound) {
        this.partZipsFound = partZipsFound;
    }

    public long getZipByteCount() {
        return zipByteCount;
    }

    public void setZipByteCount(long zipByteCount) {
        this.zipByteCount = zipByteCount;
    }

    public int incZipCount(int n) {
        zipCount += n;
        return zipCount;
    }

    public int incPartZipCount(int n) {
        partZipCount += n;
        return partZipCount;
    }

    public long incZipByteCount(long size) {
        zipByteCount += size;
        return zipByteCount;
    }
}
