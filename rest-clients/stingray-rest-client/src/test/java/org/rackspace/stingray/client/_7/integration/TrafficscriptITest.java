package org.rackspace.stingray.client._7.integration;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class TrafficscriptITest extends VTMScriptTestBase {

    /**
     * Tests the creation of a Traffic Script
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testCreateTrafficScript() throws IOException, URISyntaxException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        //the fileName is what it will be created as. ex: /rules/test_script the file in STM is 'test_script'
        client.createTrafficscript(fileName, createTestFile(fileName, fileText));
        File gfile = client.getTraffiscript(fileName);
        Assert.assertNotNull(gfile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(gfile));
    }

    /**
     * Tests the retrieval of a list of Trafficscripts
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void getListOfTrafficscripts() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getTrafficscripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Trafficscript
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetTrafficscript() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        File retrievedFile = client.getTraffiscript(fileName);
        Assert.assertNotNull(retrievedFile);

    }

    /**
     * Tests the updating of a Traffic Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void testUpdateTrafficScript() throws URISyntaxException, IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        //the filename is the same, we want to update the contents...
        String updatedFileText = "Updated the test script...";

        client.updateTrafficScript(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getTraffiscript(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }

    /**
     * Tests the deletion of a Traffic Script
     * Verifies by the expected exception
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException

     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void deleteTrafficScript() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteTrafficscript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getTraffiscript(fileName);

    }
}
