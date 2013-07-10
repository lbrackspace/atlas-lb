package org.openstack.atlas.util.staticutils;

import org.openstack.atlas.util.staticutils.StaticStringUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StaticStringUtilsTest {

    public StaticStringUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLpad() {
        assertEquals(StaticStringUtils.lpad("123", "0", 10), "0000000123");
        assertEquals(StaticStringUtils.lpad("123", "*", 5), "**123");
        assertEquals(StaticStringUtils.lpad("123", "*", 1), "123");
    }

    @Test
    public void testLpadLong() {
        assertEquals(StaticStringUtils.lpadLong(123L, "0", 10), "0000000123");
        assertEquals(StaticStringUtils.lpadLong(123L, "*", 5), "**123");
        assertEquals(StaticStringUtils.lpadLong(123L, "*", 1), "123");
    }

    @Test
    public void testTruncateString() {
        assertEquals(StaticStringUtils.truncate("0123456789", 4), "0123");
        assertEquals(StaticStringUtils.truncate("0123456789", 5), "01234");
        assertEquals(StaticStringUtils.truncate("0123456789", 6), "012345");
        assertEquals(StaticStringUtils.truncate("01234", 1024), "0123");
    }

    @Test
    public void testSetToString() {
        String out = "";
        Set<String> testSet = new HashSet<String>();

        out = StaticStringUtils.<String>collectionToString(testSet,", ");

        testSet.add("test1");
        out = StaticStringUtils.<String>collectionToString(testSet,", ");

        testSet.add("test2");
        out = StaticStringUtils.<String>collectionToString(testSet,", ");

        testSet.add("test3");
        out = StaticStringUtils.<String>collectionToString(testSet,", ");
    }

    @Test
    public void testListToString() {
        String out = "";
        List<String> testSet = new ArrayList<String>();

        out = StaticStringUtils.<String>collectionToString(testSet,", ");
        assertTrue(out.equals("[]"));
        assertFalse(out.equals(""));

        testSet.add("test1");

        out = StaticStringUtils.<String>collectionToString(testSet,", ");
        assertTrue(out.equals("[test1]"));
        assertFalse(out.equals(""));

        testSet.add("test2");
        out = StaticStringUtils.<String>collectionToString(testSet,", ");
        assertTrue(out.equals("[test1, test2]"));
        assertFalse(out.equals(""));

        testSet.add("test3");

        out = StaticStringUtils.<String>collectionToString(testSet,", ");
        assertTrue(out.equals("[test1, test2, test3]"));
        assertFalse(out.equals(""));
    }
}
