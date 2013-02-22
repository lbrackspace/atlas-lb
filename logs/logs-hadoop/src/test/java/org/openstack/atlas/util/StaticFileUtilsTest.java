package org.openstack.atlas.util;

import org.openstack.atlas.util.exceptions.FileUtilsException;
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
        assertSplitPathMatches("some/non/root/level/dir", equals, "some", "non","root", "level", "dir");
        assertSplitPathMatches("x/some/root/level/dir", notequals, "", "some", "root", "level", "dir");
    }

    public void testSplitPathToString(){
        assertSplitPathToString("/some/root/level/dir", true, "","some","root","level","dir");
        assertSplitPathToString("some/non/root/level/dir", true,"some","non","root","level","dir");

    }

    @Test
    public void testRebasePath() throws FileUtilsException{
        assertTrue(StaticFileUtils.rebaseSplitPath("/users/hdfs/output/l","/users/hdfs/output/l/logs/test/test.zip","/nfs/mnt").equals("/nfs/mnt/logs/test/test.zip"));
        assertTrue(StaticFileUtils.rebaseSplitPath("/users/hdfs/output/l","/users/hdfs/output/l/logs/test/someOtherDirectory/test.zip","/user/local/mnt").equals("/user/local/mnt/logs/test/someOtherDirectory/test.zip"));
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

}