package org.rackspace.stingray.client.integration;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ActionScriptITest extends StingrayScriptTestBase {

    /**
     * Tests the creation of an Action Script
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testCreateActionScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {

        client.createActionScript(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getActionScript(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));
    }

    /**
     * Tests the updating of an Action Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testUpdateActionScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
        String updatedFileText = "Updated the test script...";

        client.updateActionScript(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getActionScript(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }

    /**
     * Tests the retrieval of a list of Action Scripts
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfActionScripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getActionScripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Action Script
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetActionScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        File retrievedFile = client.getActionScript(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of an Action Script
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteActionScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteActionScript(fileName);
        Assert.assertTrue(wasDeleted);
        client.getActionScript(fileName);
    }
}
