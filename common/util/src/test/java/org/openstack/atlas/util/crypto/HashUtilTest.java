package org.openstack.atlas.util.crypto;

import java.security.NoSuchAlgorithmException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HashUtilTest {

    byte[] expectedHash = new byte[]{(byte)0xf0,(byte)0xc6,(byte)0x5c,(byte)0xcc};

    public HashUtilTest() {
    }

    @Before
    public void setUp() {

    }

    @Test
    public void testsha1sum4bytesasHex() throws NoSuchAlgorithmException,  IllegalArgumentException{
        String hex = HashUtil.sha1sumHex("354934".getBytes(),0,4);
        assertEquals("f0c65ccc", hex);
        nop();
    }

    @Test
    public void testsha1sum4bytes() throws NoSuchAlgorithmException, IllegalArgumentException{
        byte[] sha1sum;
        sha1sum = HashUtil.sha1sum("354934".getBytes(),0,4);
    }

    private void nop(){
    }

    private boolean BytesEqual(byte[] x,byte[]y) {
        int i;
        if(x.length != y.length) {
            return false;
        }

        for(i=0;i<x.length;i++) {
            if(x[i] != y[i]){
                return false;
            }
        }
        return true;
    }
}
