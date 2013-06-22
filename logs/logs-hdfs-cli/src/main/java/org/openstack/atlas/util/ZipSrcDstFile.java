package org.openstack.atlas.util;

public class ZipSrcDstFile {

    private String srcFile;
    private String dstFile;

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
}
