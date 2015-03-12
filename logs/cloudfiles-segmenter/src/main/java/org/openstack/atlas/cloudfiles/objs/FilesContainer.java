package org.openstack.atlas.cloudfiles.objs;

public class FilesContainer {

    @Override
    public String toString() {
        return "FilesContainer{" + "name=" + name + ", count=" + count + ", bytes=" + bytes + '}';
    }
    private String name;
    private int count;
    private long bytes;

    public FilesContainer() {
    }

    public FilesContainer(String name, int count, long bytes) {
        this.name = name;
        this.count = count;
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
