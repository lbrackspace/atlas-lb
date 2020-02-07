package org.rackspace.stingray.client.integration;


import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class MonitorScriptITest extends StingrayScriptTestBase {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testCreateMonitorScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
        client.createMonitorScript(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getMonitorScript(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));

    }

    /**
     * Tests the updating of a Monitor Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testUpateMonitorScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfMonitorScripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getMonitorScripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Monitor Script
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetMonitorScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        File retrievedFile = client.getMonitorScript(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of a Monitor Script
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteMonitorScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deleteMonitorScript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getMonitorScript(fileName);
    }

}
