package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class Base64CoderTest {

    public Base64CoderTest() {
    }

    @Before
    public void setUp(){
    }
 
    @Test
    public void testEncodeString() {
        String b64 = Base64Coder.encodeString("TestingInput");
        assertEquals("VGVzdGluZ0lucHV0",b64);
    }

    @Test
    public void testDecodeString() {
        String str = Base64Coder.decodeString("VGVzdGluZ091dHB1dA==");
        assertEquals("TestingOutput",str);
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnNonbase64InputDuringDecode() {
        String str = Base64Coder.decodeString("~`!@");
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnBadPaddingDecode() {
        String str = Base64Coder.decodeString("VGVzdGluZ0lucHV06");
    }

}