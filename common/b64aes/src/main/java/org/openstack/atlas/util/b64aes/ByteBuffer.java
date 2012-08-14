
package org.openstack.atlas.util.b64aes;

public class ByteBuffer {
    private byte[] bytes;
    private int used;

    public ByteBuffer(byte[] bytes,int used){
        this.bytes = bytes;
        this.used = used;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }
}
