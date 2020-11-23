package org.rackspace.vtm.client.integration;


import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class MonitorScriptITest extends VTMScriptTestBase {
    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        fileName = TESTNAME;
        fileText = "test file";

    }

    @AfterClass
    public static void tearDown() {
        removeTestFile(fileName);
    }

    /**
     * Tests the creation of a Monitor Script
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testCreateMonitorScript() throws URISyntaxException, IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        client.createMonitorScript(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getMonitorScript(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));

    }

    /**
     * Tests the updating of a Monitor Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testUpateMonitorScript() throws URISyntaxException, IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String updatedFileText = "Updated the test script...";

        client.updateMonitorScript(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getMonitorScript(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }


    /**
     * Tests the retrieval of a list of Monitor Scripts
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfMonitorScripts() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getMonitorScripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Monitor Script
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetMonitorScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        File retrievedFile = client.getMonitorScript(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of a Monitor Script
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteMonitorScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteMonitorScript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getMonitorScript(fileName);
    }

}
