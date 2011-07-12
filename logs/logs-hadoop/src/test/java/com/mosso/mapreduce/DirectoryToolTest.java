package com.mosso.mapreduce;

import org.openstack.atlas.util.FileSystemUtils;
import org.openstack.atlas.test.BaseHadoopTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DirectoryToolTest extends BaseHadoopTest {

    private FakeDirectoryTool tool;

    public DirectoryToolTest() throws Exception {
        super();
        /*CompositeConfiguration conf = new CompositeConfiguration();
        conf.addConfiguration(new PropertiesConfiguration("application.properties"));
        conf.addProperty(Constants.JOBJAR_PATH, "jobjar");
*/
        org.openstack.atlas.cfg.Configuration conf = new org.openstack.atlas.config.LbLogsConfiguration();
        tool = new FakeDirectoryTool();
        tool.setConf(conf);
        tool.setFileSystemUtils(new FileSystemUtils());
    }

    @Test
    public void testFakeMapperFakeReducer() throws Exception {
        FakeMapper m = new FakeMapper();
        FakeReducer r = new FakeReducer();
        m.close();
        m.configure(null);
        m.map(null, null, null, null);
        r.close();
        r.configure(null);
        r.reduce(null, null, null, null);
    }

    @Test
    public void testSetupHadoopRuns() throws Exception {
        tool.setupHadoopRun("inputDir");
        tool.setupHadoopRun("inputDir", null);
        tool.setupHadoopRun("inputDir", "jobjar");
        tool.setSpecialConfigurations(null, null);
    }
}
