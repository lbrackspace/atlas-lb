package org.openstack.atlas.util.staticutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import junit.framework.Assert;
import org.openstack.atlas.util.common.exceptions.FileUtilsException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class StaticFileUtilsTest {

    public StaticFileUtilsTest() {
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
    public void testSplitPath() {
        boolean equals = true;
        boolean notequals = false;
        assertSplitPathMatches("/some/root/level/dir", equals, "", "some", "root", "level", "dir");
        assertSplitPathMatches("some/non/root/level/dir", equals, "some", "non", "root", "level", "dir");
        assertSplitPathMatches("x/some/root/level/dir", notequals, "", "some", "root", "level", "dir");
    }

    @Test
    public void testPathTail() {
        assertEquals("test.txt", StaticFileUtils.pathTail("test.txt"));
        assertEquals(null, StaticFileUtils.pathTail(null));
        assertEquals("test.txt", StaticFileUtils.pathTail("/home/someUser/test.txt"));
        assertEquals("test.txt", StaticFileUtils.pathTail("/tmp/test.txt"));
        assertEquals("test.txt", StaticFileUtils.pathTail("tmp/test.txt"));
        assertEquals("test.txt", StaticFileUtils.pathTail("/home/someUser/test.txt"));
        assertEquals("test.txt", StaticFileUtils.pathTail("home/someUser/test.txt"));
        assertEquals("test.txt", StaticFileUtils.pathTail("////wtf/test.txt"));
    }

    public void testSplitPathToString() {
        assertSplitPathToString("/some/root/level/dir", true, "", "some", "root", "level", "dir");
        assertSplitPathToString("some/non/root/level/dir", true, "some", "non", "root", "level", "dir");

    }

    @Test
    public void testRebasePath() throws FileUtilsException {
        assertTrue(StaticFileUtils.rebaseSplitPath("/users/hdfs/output/l", "/users/hdfs/output/l/logs/test/test.zip", "/nfs/mnt").equals("/nfs/mnt/logs/test/test.zip"));
        assertTrue(StaticFileUtils.rebaseSplitPath("/users/hdfs/output/l", "/users/hdfs/output/l/logs/test/someOtherDirectory/test.zip", "/user/local/mnt").equals("/user/local/mnt/logs/test/someOtherDirectory/test.zip"));
    }

    private void assertSplitPathToString(String path, boolean shouldMatch, String... expComponents) {
        String expPath = StaticFileUtils.splitPathToString(expComponents);
        if (shouldMatch) {
            if (!path.equals(expPath)) {
                fail(String.format("path %s doesn't match %s\n", path, expPath));
            }
        } else {
            if (path.equals(expPath)) {
                fail(String.format("path %s equals %s\n", path, expPath));
            }
        }
    }

    private void assertSplitPathMatches(String path, boolean shouldMatch, String... expComponents) {
        String[] pathSplit = StaticFileUtils.splitPath(path);

        if (shouldMatch) {
            if (pathSplit.length != expComponents.length) {
                fail("Arrays were different lengths");
            }
            for (int i = 0; i < pathSplit.length; i++) {
                if (!pathSplit[i].equals(expComponents[i])) {
                    fail(String.format("Element %d did not match in both arrays", i));
                }
            }
        } else {
            if (pathSplit.length != expComponents.length) {
                return;// Arrays weren't equal
            }
            for (int i = 0; i < pathSplit.length; i++) {
                if (!pathSplit[i].equals(expComponents[i])) {
                    return;
                }
            }
            fail("Arrays were Equals");
        }
    }

    private int nop() {
        return -1;

    }

    @Test
    public void testGetLogFileTime() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String logFileTime = StaticFileUtils.getLogFileTime(absoluteFileName);
        Assert.assertEquals(logFileTime, "2012021005");
    }

    @Test
    public void testGetDateStringFromFileName() throws Exception {
        String absoluteFileName = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        String dateString = StaticFileUtils.getDateStringFromFileName(absoluteFileName);
        Assert.assertEquals(dateString, "2011021513");
    }

    @Test
    public void testGetDateStringFromFileNameWhenInvalidFileName() {
        String absoluteFileName = "/var/log/zxtm/rotated/new-access_log.aggregated";
        boolean expectedException = false;
        try {
            StaticFileUtils.getDateStringFromFileName(absoluteFileName);
        } catch (IllegalArgumentException e) {
            expectedException = true;
        }
        Assert.assertEquals(true, expectedException);
    }

    @Test
    public void testGetDateFromFileName() throws Exception {
        String absoluteFileName = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        Date date = StaticFileUtils.getDateFromFileName(absoluteFileName);
        Assert.assertEquals(date, StaticFileUtils.getDate("2011021513", StaticFileUtils.filedf));
    }

    @Test
    public void testGetNewestFile() throws Exception {
        String absoluteFileName1 = "/var/log/zxtm/rotated/2011021512-access_log.aggregated";
        String absoluteFileName2 = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        String absoluteFileName3 = "/var/log/zxtm/rotated/2011021511-access_log.aggregated";
        List<String> files = new ArrayList<String>();
        files.add(absoluteFileName1);
        files.add(absoluteFileName2);
        files.add(absoluteFileName3);

        String newestFile = StaticFileUtils.getNewestFile(files);
        Assert.assertEquals(newestFile, absoluteFileName2);
    }

    @Test
    public void testGetMonthYearFromFileDate() throws Exception {
        String dateString = "2011021512";

        String monthYear = StaticFileUtils.getMonthYearFromFileDate(dateString);
        Assert.assertEquals(monthYear, "Feb_2011");
    }

    @Test
    public void testCompressBytes() throws IOException {
        double ratio;
        int i;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] rndBytes = Debug.rndBytes(32);
        for (i = 0; i < 1024 * 1024; i++) {
            baos.write(rndBytes);
        }
        byte[] repetativeBytes = baos.toByteArray();
        byte[] compressedBytes = StaticFileUtils.compressBytes(repetativeBytes);
        byte[] decompressedBytes = StaticFileUtils.decompressBytes(compressedBytes);
        ratio = (double) compressedBytes.length / (double) rndBytes.length;
        assertTrue(byteArraysEqual(repetativeBytes, decompressedBytes));
    }

    private static boolean byteArraysEqual(byte[] a, byte[] b) {
        int i;
        int nBytes = a.length;
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        if (b == null) {
            return false;
        }
        if (nBytes != b.length) {
            return false;
        }
        for (i = 0; i < nBytes; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
