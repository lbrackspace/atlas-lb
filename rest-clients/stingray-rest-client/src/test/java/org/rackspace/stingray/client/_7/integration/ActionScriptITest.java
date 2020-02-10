package org.rackspace.stingray.client._7.integration;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client_7.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActionScriptITest extends VTMScriptTestBase {

    /**
     * Tests the creation of an Action Script
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void atestCreateActionScript() throws VTMRestClientException, URISyntaxException, IOException, VTMRestClientObjectNotFoundException {

        client.createActionScript(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getActionScript(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));
    }

    /**
     * Tests the updating of an Action Script
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void btestUpdateActionScript() throws VTMRestClientException, URISyntaxException, IOException, VTMRestClientObjectNotFoundException {
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
     * @throws VTMRestClientException
     *
     */
    @Test
    public void ctestGetListOfActionScripts() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        List<Child> children = client.getActionScripts();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Action Script
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetActionScript() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        File retrievedFile = client.getActionScript(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of an Action Script
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeleteActionScript() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        Response wasDeleted = client.deleteActionScript(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getActionScript(fileName);
    }
}
