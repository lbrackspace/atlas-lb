package org.openstack.atlas.logs.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CacheZipDirInfo {

    private long hourKey = 0;
    private int accountId = 0;
    private String dirName = null;
    private int zipCount = 0;
    private List<CacheZipInfo> zips = new ArrayList<CacheZipInfo>();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CacheZipDirInfo other = (CacheZipDirInfo) obj;
        if (this.hourKey != other.hourKey) {
            return false;
        }
        if (this.accountId != other.accountId) {
            return false;
        }
        if ((this.dirName == null) ? (other.dirName != null) : !this.dirName.equals(other.dirName)) {
            return false;
        }
        if (this.zipCount != other.zipCount) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.hourKey ^ (this.hourKey >>> 32));
        hash = 37 * hash + this.accountId;
        hash = 37 * hash + (this.dirName != null ? this.dirName.hashCode() : 0);
        hash = 37 * hash + this.zipCount;
        return hash;
    }

    @Override
    public String toString() {
        return "{"
                + "hourKey=" + hourKey
                + ", accountId=" + accountId
                + ", dirName=" + dirName
                + ", zipCount=" + zipCount
                + "}";
    }

    public long getHourKey() {
        return hourKey;
    }

    public void setHourKey(long hourKey) {
        this.hourKey = hourKey;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public int getZipCount() {
        return zipCount;
    }

    public void setZipCount(int zipCount) {
        this.zipCount = zipCount;
    }

    public List<CacheZipInfo> getZips() {
        if (zips == null) {
            zips = new ArrayList<CacheZipInfo>();
        }
        return zips;
    }

    public void setZips(List<CacheZipInfo> zips) {
        this.zips = zips;
    }

    public static class CountComparator implements Comparator<CacheZipDirInfo> {

        @Override
        public int compare(CacheZipDirInfo o1, CacheZipDirInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null && o2 != null) {
                return 1;
            }
            if (o1 != null && o2 == null) {
                return -1;
            }
            int o1ZipCount = o1.getZipCount();
            int o2ZipCount = o2.getZipCount();
            if (o1ZipCount < o2ZipCount) {
                return -1;
            }
            if (o1ZipCount > o2ZipCount) {
                return 1;
            }
            return 0;
        }
    }

    public static class HourAccountComparator implements Comparator<CacheZipDirInfo> {

        @Override
        public int compare(CacheZipDirInfo o1, CacheZipDirInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null && o2 != null) {
                return 1;
            }
            if (o1 != null && o2 == null) {
                return -1;
            }
            long o1hour = o1.getHourKey();
            long o2hour = o2.getHourKey();
            if (o1hour < o2hour) {
                return -1;
            }
            if (o1hour > o2hour) {
                return 1;
            }

            int o1accountId = o1.getAccountId();
            int o2accountId = o2.getAccountId();

            if (o1accountId < o2accountId) {
                return -1;
            }
            if (o1accountId > o2accountId) {
                return 1;
            }
            return 0;
        }
    }
}
