package org.openstack.atlas.util;

import org.openstack.atlas.cloudfiles.CloudFilesDao;
import org.openstack.atlas.cloudfiles.CloudFilesDaoImpl;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import org.openstack.atlas.test.BaseStatsTest;
import org.openstack.atlas.test.TestHelper;
import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.Ignore;

import java.io.File;

@Ignore
public class CloudFilesDaoTest extends BaseStatsTest {
    private static String currentdir = TestHelper.sanitizeCurrentDir(System.getProperty("user.dir"));

    private static String inputFile = "small-upload-me.txt";

    private static String inputString = currentdir + "/src/test/hadoop/input/cloud_files_upload_test/"
            + inputFile;

    private static String outputFile = "small-downloaded.txt";

    private static String outputString = currentdir + "/src/test/hadoop/input/cloud_files_upload_test/"
            + outputFile;

    private FilesClient client;

    private CompositeConfiguration conf;

    private CloudFilesDao dao;
    private File f;

    public void setCloudFilesClient(FilesClient filesClient) {
        this.client = filesClient;
    }

    public void setConf(CompositeConfiguration conf) {
        this.conf = conf;
    }

    public void setMapReduceService(CloudFilesDao cloudFilesDao) {
        this.dao = cloudFilesDao;
    }

    class FakeCloudFilesDaoImpl extends CloudFilesDaoImpl {
        // I do NOT want to expose the login/logout, so this will work, and it
        // will test it.
        public void testLogin() throws Exception {
            super.login();
        }
    }
}
