package org.rackspace.capman.tools.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StaticHelpersTest {
    public StaticHelpersTest() {
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
    public void testHexMap() {
        String hex = "0123456789abcdef";
        String[] int2hex = StaticHelpers.getHexMap();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 16; i++) {
            sb.append(int2hex[i]);
        }
        assertEquals(hex, sb.toString());
    }

}
