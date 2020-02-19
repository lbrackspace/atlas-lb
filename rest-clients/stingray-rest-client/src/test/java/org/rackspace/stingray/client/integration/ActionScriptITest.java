package org.rackspace.stingray.client.integration;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActionScriptITest extends StingrayScriptTestBase {

    /**
     * Tests the creation of an Action Script
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void atestCreateActionScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {

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
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void btestUpdateActionScript() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws StingrayRestClientException
     *
     */
    @Test
    public void ctestGetListOfActionScripts() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
    public void dtestGetActionScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
    public void etestDeleteActionScript() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deleteActionScript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getActionScript(fileName);
    }
}
