package org.openstack.atlas.logs.itest;

public class WastedBytesBlock {

    private byte[] byteArray;


    public WastedBytesBlock(int size) {
        byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = (byte) (i % 127);
        }
    }

    public int size() {
        return byteArray.length;
    }

}
