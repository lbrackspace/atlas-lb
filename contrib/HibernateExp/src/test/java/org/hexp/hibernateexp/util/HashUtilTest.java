package org.hexp.hibernateexp.util;

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

    private void nop(){
    }
}
