package org.openstack.atlas.logs.common.util;

import java.util.Comparator;

public class CacheZipInfo {

    private int loadbalancerId = 0;
    private int accountId = 0;
    private long hourKey = 0;
    private long fileSize = 0;
    private String zipFile = null;

    public CacheZipInfo() {
        loadbalancerId = 0;
        accountId = 0;
        hourKey = 0;
        fileSize = 0;
        zipFile = null;
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public long getHourKey() {
        return hourKey;
    }

    public void setHourKey(long hourKey) {
        this.hourKey = hourKey;
    }

    public String getZipFile() {
        return zipFile;
    }

    public void setZipFile(String zipFile) {
        this.zipFile = zipFile;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public static class ZipComparator implements Comparator<CacheZipInfo> {

        @Override
        public int compare(CacheZipInfo o1, CacheZipInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            long o1Hour = o1.getHourKey();
            long o2Hour = o2.getHourKey();
            if (o1Hour < o2Hour) {
                return -1;
            }
            if (o1Hour > o2Hour) {
                return 1;
            }
            int o1AccountId = o1.getAccountId();
            int o2AccountId = o2.getAccountId();

            if (o1AccountId < o2AccountId) {
                return -1;
            }
            if (o1AccountId > o2AccountId) {
                return 1;
            }

            int o1LoadbalancerId = o1.getLoadbalancerId();
            int o2LoadbalancerId = o2.getLoadbalancerId();

            if (o1LoadbalancerId < o2LoadbalancerId) {
                return -1;
            }
            if (o1LoadbalancerId > o2LoadbalancerId) {
                return 1;
            }
            long o1FileSize = o1.getFileSize();
            long o2FileSize = o2.getFileSize();
            if (o1FileSize < o2FileSize) {
                return -1;
            }
            if (o1FileSize > o2FileSize) {
                return 1;
            }
            return 0;
        }
    }

    public static class LidComparator implements Comparator<CacheZipInfo> {

        @Override
        public int compare(CacheZipInfo o1, CacheZipInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            int ol1 = o1.getLoadbalancerId();
            int ol2 = o2.getLoadbalancerId();
            if (ol1 < ol2) {
                return -1;
            }
            if (ol2 > ol2) {
                return 1;
            }
            return 0;
        }
    }

    public static class AidComparator implements Comparator<CacheZipInfo> {

        @Override
        public int compare(CacheZipInfo o1, CacheZipInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            
            int oa1 = o1.getAccountId();
            int oa2 = o2.getAccountId();
            if (oa1 < oa2) {
                return -1;
            }
            if (oa2 > oa2) {
                return 1;
            }
            return 0;
        }
    }

    public static class ByteCountComparator implements Comparator<CacheZipInfo> {

        @Override
        public int compare(CacheZipInfo o1, CacheZipInfo o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            long o1size = o1.getFileSize();
            long o2size = o2.getFileSize();
            if (o1size < o2size) {
                return -1;
            }
            if (o1size > o2size) {
                return 1;
            }
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CacheZipInfo other = (CacheZipInfo) obj;
        if (this.loadbalancerId != other.loadbalancerId) {
            return false;
        }
        if (this.accountId != other.accountId) {
            return false;
        }
        if (this.hourKey != other.hourKey) {
            return false;
        }
        if (this.fileSize != other.fileSize) {
            return false;
        }
        if ((this.zipFile == null) ? (other.zipFile != null) : !this.zipFile.equals(other.zipFile)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.loadbalancerId;
        hash = 41 * hash + this.accountId;
        hash = 41 * hash + (int) (this.hourKey ^ (this.hourKey >>> 32));
        hash = 41 * hash + (int) (this.fileSize ^ (this.fileSize >>> 32));
        hash = 41 * hash + (this.zipFile != null ? this.zipFile.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "{"
                + "loadbalancerId=" + loadbalancerId
                + ", accountId=" + accountId
                + ", hourKey=" + hourKey
                + ", zipFile=" + zipFile
                + "}";
    }
}
