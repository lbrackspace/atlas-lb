package org.openstack.atlas.util.ca.primitives;

// Can't make Lists out of primitives so where stuck with this nonsense.
public class ByteLineListEntry {
    private byte[] bytes;

    public ByteLineListEntry(){
    }

    public ByteLineListEntry(byte[] bytes){
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
