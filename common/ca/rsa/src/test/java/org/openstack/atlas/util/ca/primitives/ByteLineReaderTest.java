package org.openstack.atlas.util.ca.primitives;

import org.rackexp.ca.primitives.ByteLineReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rackexp.ca.primitives.ByteLineReader;
import static org.junit.Assert.*;
import static org.openstack.atlas.util.ca.StringUtils.asciiBytes;

public class ByteLineReaderTest {

    private byte[] lines;

    public ByteLineReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        lines = asciiBytes("\r\nabc\nabc\r\nabc\r\n\nx");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBytesAvailable() {
        ByteLineReader br = new ByteLineReader(lines);
        assertEquals(18, br.bytesAvailable());
        br.readLine();
        assertEquals(16, br.bytesAvailable());
        br.readLine();
        assertEquals(12, br.bytesAvailable());
        br.readLine();
        assertEquals(7, br.bytesAvailable());
        br.readLine();
        assertEquals(2, br.bytesAvailable());
        br.readLine();
        assertEquals(1, br.bytesAvailable());
        br.readLine();
        assertEquals(0, br.bytesAvailable());
    }

    @Test
    public void testReadLineNoChop() {
        ByteLineReader br = new ByteLineReader(lines);
        byte[] line;
        line = br.readLine(false);
        assertTrue(bytesMatchString("\r\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\r\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\r\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("x", line));
    }

    @Test
    public void testReadLineWithChop() {
        ByteLineReader br = new ByteLineReader(lines);
        byte[] line;
        line = br.readLine(true);
        assertTrue(bytesMatchString("", line));
        line = br.readLine(true);
        assertTrue(bytesMatchString("abc", line));
        line = br.readLine(true);
        assertTrue(bytesMatchString("abc", line));
        line = br.readLine(true);
        assertTrue(bytesMatchString("abc", line));
        line = br.readLine(true);
        assertTrue(bytesMatchString("", line));
        line = br.readLine(true);
        assertTrue(bytesMatchString("x", line));
    }

    @Test
    public void testCopyBytes() {
        System.out.println("copyBytes");
        byte[] a = asciiBytes("Test");
        byte[] exp = asciiBytes("Test");
        byte[] r = ByteLineReader.copyBytes(a);
        assertArrayEquals(exp, a);
    }

    @Test
    public void testCmpBytes() {
        System.out.println("cmpBytes");
        byte[] a = asciiBytes("ABCDEFG");
        byte[] b = asciiBytes("ABCDEFG");
        assertTrue(ByteLineReader.cmpBytes(a, b));
        a = asciiBytes("NotEqual");
        assertFalse(ByteLineReader.cmpBytes(a, b));
    }

    public boolean bytesMatchString(String expectedStr, byte[] b) {
        return ByteLineReader.cmpBytes(asciiBytes(expectedStr), b);
    }
}
