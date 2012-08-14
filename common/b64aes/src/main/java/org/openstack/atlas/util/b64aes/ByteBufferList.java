package org.openstack.atlas.util.b64aes;

import java.util.ArrayList;
import java.util.List;

public class ByteBufferList {

    private List<ByteBuffer> byteBufferEntries;

    public ByteBufferList() {
        byteBufferEntries = new ArrayList<ByteBuffer>();
    }

    public byte[] getAllBytes() {
        byte[] out;
        int oi = 0;
        int size = 0;
        for (ByteBuffer buff : byteBufferEntries) {
            size += buff.getUsed();
        }
        out = new byte[size];
        for (ByteBuffer buff : byteBufferEntries) {
            byte[] buffBytes = buff.getBytes();
            int used = buff.getUsed();
            for (int i = 0; i < used; i++, oi++) {
                out[oi] = buffBytes[i];
            }
        }
        return out;
    }

    public List<ByteBuffer> getByteBufferEntries() {
        return byteBufferEntries;
    }
}
