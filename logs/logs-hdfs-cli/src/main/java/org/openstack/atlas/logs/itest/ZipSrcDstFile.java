package org.openstack.atlas.logs.itest;

import java.util.Comparator;

public class ZipSrcDstFile {

    private String srcFile;
    private String dstFile;
    private String hourKey;
    private int accountId;
    private int loadbalancerId;

    public ZipSrcDstFile() {
    }

    @Override
    public String toString() {
        return srcFile + "->" + dstFile;
    }

    public String getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(String srcFile) {
        this.srcFile = srcFile;
    }

    public String getDstFile() {
        return dstFile;
    }

    public void setDstFile(String dstFile) {
        this.dstFile = dstFile;
    }

    public String getHourKey() {
        return hourKey;
    }

    public void setHourKey(String hourKey) {
        this.hourKey = hourKey;
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

}
