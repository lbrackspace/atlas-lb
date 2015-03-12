package org.openstack.atlas.cloudfiles.objs;

import org.joda.time.DateTime;

public class FilesObject {

    @Override
    public String toString() {
        return "FilesObject{" + "bytes=" + bytes +
                ", lastModified=" + lastModified +
                ", hash=" + hash +
                ", name=" + name +
                ", contentType=" +
                contentType + '}';
    }

    public FilesObject(long bytes, DateTime lastModified, String hash, String name, String contentType) {
        this.bytes = bytes;
        this.lastModified = lastModified;
        this.hash = hash;
        this.name = name;
        this.contentType = contentType;
    }

    private long bytes;
    private DateTime lastModified;
    private String hash;
    private String name;
    private String contentType;

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
