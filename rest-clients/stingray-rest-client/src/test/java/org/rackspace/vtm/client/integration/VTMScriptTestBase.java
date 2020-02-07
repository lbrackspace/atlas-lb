package org.rackspace.vtm.client.integration;

import org.junit.AfterClass;
import org.junit.Before;
import org.openstack.atlas.util.crypto.exception.DecryptException;

public class VTMScriptTestBase extends VTMTestBase {

    static String fileName;
    String fileText;

    @Before
    public void standUp() throws DecryptException {
        super.standUp();
        fileText = "test_file";
        fileName = TESTNAME;
    }

    @AfterClass
    public static void tearDown(){
        removeTestFile(fileName);
    }
}
