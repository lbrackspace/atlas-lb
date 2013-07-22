package org.rackspace.capman.tools.ca.gui.utils;

import java.util.ArrayList;
import java.util.List;

public class BytesList {
    private List<byte[]> bytesList;

    public BytesList(){
        bytesList = new ArrayList<byte[]>();
    }

    public void clear(){
        bytesList = new ArrayList<byte[]>();
    }

    public long length(){
        long nCount=0;
        for(byte[] byteArray:bytesList){
            nCount += byteArray.length;
        }
        return nCount;
    }

    public void addFilledBytes(int nbytes){
        byte[] append = new byte[nbytes];
        int i;
        for(i=0;i<nbytes;i++){
            append[i] = (i < 128) ? (byte) i : (byte) (i - 256);
        }
        bytesList.add(append);
    }

    public List<byte[]> getBytesList() {
        return bytesList;
    }
}
