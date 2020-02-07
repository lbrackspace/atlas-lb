package org.rackspace.stingray.client.integration;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class TrafficscriptITest extends StingrayScriptTestBase {

    /**
     * Tests the creation of a Traffic Script
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testCreateTrafficScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void getListOfTrafficscripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getTrafficscripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Trafficscript
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetTrafficscript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        File retrievedFile = client.getTraffiscript(fileName);
        Assert.assertNotNull(retrievedFile);

    }

    /**
     * Tests the updating of a Traffic Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testUpdateTrafficScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
        //the filename is the same, we want to update the contents...
        String updatedFileText = "Updated the test script...";

        client.updateTrafficScript(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getTraffiscript(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }

    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void deleteTrafficScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deleteTrafficscript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getTraffiscript(fileName);

    }
}
