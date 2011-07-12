package org.openstack.atlas.test;

import junit.framework.TestCase;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapred.HadoopTestCase;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MiniMRCluster;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BaseHadoopTest extends TestCase {

    public static final int LOCAL_MR = 1;
    public static final int CLUSTER_MR = 2;
    public static final int LOCAL_FS = 4;
    public static final int DFS_FS = 8;


    private static final int DATANODES = 1;

    private static final int FSMODE = HadoopTestCase.LOCAL_FS;

    private static final int MRMODE = HadoopTestCase.LOCAL_MR;

    private static final int TASKTRACKERS = 1;

    private boolean localMR;
    private boolean localFS;

    private int taskTrackers;
    private int dataNodes;


    protected JobConf c;

    public BaseHadoopTest() throws IOException {
        this(MRMODE, FSMODE, TASKTRACKERS, DATANODES);
    }

    public BaseHadoopTest(int mrMode, int fsMode, int taskTrackers, int dataNodes) throws IOException {
        if (mrMode != LOCAL_MR && mrMode != CLUSTER_MR) {
            throw new IllegalArgumentException(
                    "Invalid MapRed mode, must be LOCAL_MR or CLUSTER_MR");
        }
        if (fsMode != LOCAL_FS && fsMode != DFS_FS) {
            throw new IllegalArgumentException(
                    "Invalid FileSystem mode, must be LOCAL_FS or DFS_FS");
        }
        if (taskTrackers < 1) {
            throw new IllegalArgumentException(
                    "Invalid taskTrackers value, must be greater than 0");
        }
        if (dataNodes < 1) {
            throw new IllegalArgumentException(
                    "Invalid dataNodes value, must be greater than 0");
        }
        localMR = (mrMode == LOCAL_MR);
        localFS = (fsMode == LOCAL_FS);

        this.taskTrackers = taskTrackers;
        this.dataNodes = dataNodes;
        System.setProperty("hadoop.log.dir", "/tmp/hadoop-" + System.getProperty("user.name") + "/logs");
    }

    @Test
    public void testCreateLogLine() throws Exception {
        String fqdn = "1";
        String time = "2";
        Assert.assertEquals(TestHelper.generateLogLine(fqdn, time), TestHelper.generateLogLine(fqdn, time));
        createJobConf();
    }

    private MiniDFSCluster dfsCluster = null;
    protected MiniMRCluster mrCluster = null;
    private FileSystem fileSystem = null;

    /**
     * Creates Hadoop instance based on constructor configuration before
     * a test case is run.
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        if (localFS) {
            fileSystem = FileSystem.getLocal(new JobConf());
        } else {
            dfsCluster = new MiniDFSCluster(new JobConf(), dataNodes, true, null);
            fileSystem = dfsCluster.getFileSystem();
        }
        if (localMR) {
        } else {
            //noinspection deprecation
            String[] racks = new String[taskTrackers];
            for (int i = 0; i < taskTrackers; i++) {
                racks[i] = "/rack";
            }
            mrCluster = new MiniMRCluster(0, 0, taskTrackers, fileSystem.getName(), 1, racks, null, null, null);
        }
    }

    /**
     * Destroys Hadoop instance based on constructor configuration after
     * a test case is run.
     *
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        try {
            if (mrCluster != null) {
                mrCluster.shutdown();
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        try {
            if (dfsCluster != null) {
                dfsCluster.shutdown();
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        super.tearDown();
    }

    /**
     * Returns the Filesystem in use.
     * <p/>
     * TestCases should use this Filesystem as it
     * is properly configured with the workingDir for relative PATHs.
     *
     * @return the filesystem used by Hadoop.
     */
    protected FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Returns a job configuration preconfigured to run against the Hadoop
     * managed by the testcase.
     *
     * @return configuration that works on the testcase Hadoop instance
     */
    protected JobConf createJobConf() {
        return (localMR) ? new JobConf() : mrCluster.createJobConf();
    }
}
