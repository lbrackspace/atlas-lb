package org.openstack.atlas.config;

public class CloudFilesZipInfo implements Comparable<CloudFilesZipInfo> {

    protected int accountId;
    protected int loadbalancerId;
    protected int nLines;
    protected long uncompressedSize;
    protected long crc;
    private String hdfsFile;
    private String cacheFile;
    private String localCacheDir;
    private String accountCacheDir;

    public CloudFilesZipInfo() {
    }

    @Override
    public int compareTo(CloudFilesZipInfo o) {
        int oAccountId = o.getAccountId();
        int oLoadbalancerId = o.getLoadbalancerId();
        if (accountId < oAccountId) {
            return -1;
        }
        if (accountId > oAccountId) {
            return 1;
        }
        if (loadbalancerId < oLoadbalancerId) {
            return -1;
        }
        if (loadbalancerId > oLoadbalancerId) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "CloudFilesZipInfo{accountId=" + accountId
                + ", loadbalancerId=" + loadbalancerId
                + ", nLines=" + nLines
                + ", uncompressedSize=" + uncompressedSize
                + ", crc=" + crc
                + ", hdfsFile=" + hdfsFile
                + ", cacheFile=" + cacheFile + '}';
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getLoadbalancerId() {
        return loadbalancerId;
    }

    public void setLoadbalancerId(int loadbalancerId) {
        this.loadbalancerId = loadbalancerId;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public long getUncompressedSize() {
        return uncompressedSize;
    }

    public void setUncompressedSize(long uncompressedSize) {
        this.uncompressedSize = uncompressedSize;
    }

    public int getnLines() {
        return nLines;
    }

    public void setnLines(int nLines) {
        this.nLines = nLines;
    }

    public String getHdfsFile() {
        return hdfsFile;
    }

    public void setHdfsFile(String hdfsFile) {
        this.hdfsFile = hdfsFile;
    }

    public String getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(String cacheFile) {
        this.cacheFile = cacheFile;
    }

    public String getLocalCacheDir() {
        return localCacheDir;
    }

    public void setLocalCacheDir(String localCacheDir) {
        this.localCacheDir = localCacheDir;
    }

    public String getAccountCacheDir() {
        return accountCacheDir;
    }

    public void setAccountCacheDir(String accountCacheDir) {
        this.accountCacheDir = accountCacheDir;
    }
}
