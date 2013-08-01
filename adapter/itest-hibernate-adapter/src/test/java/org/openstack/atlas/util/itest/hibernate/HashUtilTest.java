package org.openstack.atlas.util.itest.hibernate;

import org.openstack.atlas.util.itest.hibernate.HashUtil;
import java.security.NoSuchAlgorithmException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class HashUtilTest {

    public HashUtilTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testSomeMethod() throws NoSuchAlgorithmException,  IllegalArgumentException{
        String hex = HashUtil.sha1sumHex("test".getBytes(),null,null);
        nop();
    }

    @Test
    public void testBytes2Hex(){
        int[] testInts = new int[]{0xff,0xfe,0xfd,0x81,0x80,0x7f,0x7e,0x01,0x00};
        byte[] testBytes = new byte[testInts.length];
        String expectedHex="fffefd81807f7e0100";
        for(int i=0;i<testInts.length;i++){
            testBytes[i]=(byte)testInts[i];
        }
        assertEquals(expectedHex,HashUtil.bytes2hex(testBytes));
    }

    private void nop(){
    }
}
