package org.openstack.atlas.cloudfiles;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesUtilsTest {

    public static final int BUFFSIZE = 8192;
    public static final String testStr = new String("Testing 1 2 3 4 5 6");

    public CloudFilesUtilsTest() {
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
    public void testRegexFragNumber() {
        int i;
        int n;
        int[] expInts = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        String[] testStrings = new String[]{
            "frag.0000", "frag.0001", "frag.0002", "frag.0003", "frag.0004",
            "frag.0005", "frag.0006", "frag.0007", "frag.0008"};

        Assert.assertEquals(-1, CloudFilesUtils.regexFragNumber("one"));
        n = expInts.length;
        for (i = 0; i < n; i++) {
            int expected = expInts[i];
            String testString = testStrings[i];
            int found = CloudFilesUtils.regexFragNumber(testString);
            Assert.assertEquals(expected, found);
        }
    }
}
