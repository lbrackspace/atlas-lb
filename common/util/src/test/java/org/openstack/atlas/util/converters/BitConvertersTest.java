package org.openstack.atlas.util.converters;

import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class BitConvertersTest {

    public BitConvertersTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testInt2NibbleMap() {
        int i;
        byte[] expectedHex = new byte[]{'0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'A', 'B',
            'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd',
            'e', 'f'};

        int[] controlInts = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            10, 11, 12, 13, 14, 15};
        for (i = 0; i < controlInts.length; i++) {
            assertEquals(BitConverters.nibble2Int(expectedHex[i]), controlInts[i]);
        }
    }

    @Test
    public void testubyte2int() {
        int i;
        byte[] controlBytes = {(byte) 0xff, (byte) 0x80, (byte) 0x7f};
        int[] expectedInts = {255, 128, 127};
        for (i = 0; i < controlBytes.length; i++) {
            assertEquals(expectedInts[i], BitConverters.ubyte2int(controlBytes[i]));
        }

        //exuastive test
        for (i = 0; i < 256; i++) {
            assertEquals(i, BitConverters.ubyte2int(BitConverters.int2ubyte(i)));
        }
    }

    @Test
    public void testint2ubyte() {
        int i;
        int[] controlInts = {0, 2, 4, 8, 16, 32, 64, 127, 128, 129, 255};
        byte[] expectedBytes = {(byte) 0x00, (byte) 0x02, (byte) 0x04, (byte) 0x08,
            (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x7f, (byte) 0x80,
            (byte) 0x81, (byte) 0xff};

        for (i = 0; i < controlInts.length; i++) {
            assertEquals(expectedBytes[i], BitConverters.int2ubyte(controlInts[i]));
        }
    }

    @Test
    public void testuint2bytes() {
        int control;
        byte[] observed;
        byte[] expected;
        control = 2145845231;
        expected = new byte[]{(byte) 0x7f, (byte) 0xe6, (byte) 0xff, (byte) 0xef};
        observed = BitConverters.uint2bytes(control);
        assertTrue(bytesEqual(expected, observed));
    }

    @Test
    public void testBytes2Hex() {
        byte[] bytes = new byte[]{-1, -2, -3, -128, 0, 127};
        String expectedHex = "fffefd80007f";
        assertEquals(expectedHex, BitConverters.bytes2hex(bytes));

    }

    @Test
    public void testByte2Hex() {
        int i;
        String[] expected_hex = {"01", "02", "04", "08",
            "10", "20", "40", "80"};
        byte control_byte[] = {1, 2, 4, 8, 16, 32, 64, -128};
        for (i = 0; i < control_byte.length; i++) {
            assertEquals(expected_hex[i], BitConverters.byte2hex(control_byte[i]));
        }
    }

    private boolean bytesEqual(byte[] a, byte[] b) {
        int i;
        if (a.length != b.length) {
            return false;
        }

        for (i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
