package org.rackspace.capman.tools.ca.primitives;

import org.rackspace.capman.tools.ca.primitives.ByteLineReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.rackspace.capman.tools.ca.StringUtils.asciiBytes;

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
        assertTrue(bytesMatchString("\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\n", line));
        line = br.readLine(false);
        assertTrue(bytesMatchString("abc\n", line));
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

    @Test
    public void testAppendLF(){
        byte[] line1 = asciiBytes("ABC");
        byte[] line2 = asciiBytes("12345");
        byte[] line1exp = asciiBytes("ABC\n");
        byte[] line2exp = asciiBytes("12345\n");
        byte[] line1result = ByteLineReader.appendLF(line1);
        byte[] line2result = ByteLineReader.appendLF(line2);
        assertTrue(ByteLineReader.cmpBytes(line1exp,line1result));
        assertTrue(ByteLineReader.cmpBytes(line2exp,line2result));
        assertFalse(ByteLineReader.cmpBytes(line1result,line2result));
    }

    @Test
    public void testChop(){
        byte[] line1 = asciiBytes("ABCDEF\n");
        byte[] line2 = asciiBytes("ABCDEF");
        byte[] line3 = asciiBytes("test \n");
        byte[] line4 = asciiBytes("test ");
        assertTrue(bytesMatchString("ABCDEF", ByteLineReader.chopLine(line1)));
        assertTrue(bytesMatchString("ABCDEF", ByteLineReader.chopLine(line2)));
        assertTrue(bytesMatchString("test ", ByteLineReader.chopLine(line3)));
        assertTrue(bytesMatchString("test ", ByteLineReader.chopLine(line4)));
    }

    @Test
    public void testTrime(){
        byte[] line1 = asciiBytes(" trim this  ");
        byte[] line2 = asciiBytes("             ");
        byte[] line3 = asciiBytes(" \u001eRecord Seperator\u001f  ");
        assertTrue(bytesMatchString("trim this",ByteLineReader.trim(line1)));
        assertTrue(bytesMatchString("",ByteLineReader.trim(line2)));
        assertTrue(bytesMatchString("Record Seperator",ByteLineReader.trim(line3)));

        assertTrue(bytesMatchString("test",ByteLineReader.trim(asciiBytes("     test"))));
        assertTrue(bytesMatchString("test",ByteLineReader.trim(asciiBytes("   test  "))));
        assertTrue(bytesMatchString("test",ByteLineReader.trim(asciiBytes("test     "))));
        assertTrue(bytesMatchString("",ByteLineReader.trim(asciiBytes("     "))));
        assertTrue(bytesMatchString("",ByteLineReader.trim(asciiBytes(""))));
        assertFalse(bytesMatchString("FAIL",ByteLineReader.trim(asciiBytes("PFFT"))));
    }

    public boolean bytesMatchString(String expectedStr, byte[] b) {
        return ByteLineReader.cmpBytes(asciiBytes(expectedStr), b);
    }
}
