
package org.openstack.atlas.util;


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
    public void testTruncateString(){
        assertEquals(StaticStringUtils.truncate("0123456789",4),"0123");
        assertEquals(StaticStringUtils.truncate("0123456789",5),"01234");
        assertEquals(StaticStringUtils.truncate("0123456789",6),"012345");
        assertEquals(StaticStringUtils.truncate("01234",1024),"0123");
    }

}