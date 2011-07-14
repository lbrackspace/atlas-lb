package org.hexp.hibernateexp.util;

import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.hexp.hibernateexp.util.BitUtilTest;
import static org.junit.Assert.*;

public class BitUtilTest {

    public BitUtilTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testBytes2Hex() {
        byte[] bytes = new byte[]{-1, -2, -3, -128, 0, 127};
        String expectedHex = "fffefd80007f";
        assertEquals(expectedHex, BitUtil.bytes2hex(bytes));
    }

    @Test
    public void testByte2Hex() {
        int i;
        String[] expected_hex = {"01", "02", "04", "08",
            "10", "20", "40", "80"};
        byte control_byte[] = {1, 2, 4, 8, 16, 32, 64, -128};
        for(i=0;i<control_byte.length;i++){
            assertEquals(expected_hex[i],BitUtil.byte2hex(control_byte[i]));
        }
    }
}
