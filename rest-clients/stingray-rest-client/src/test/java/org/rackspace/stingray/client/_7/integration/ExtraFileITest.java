package org.rackspace.stingray.client._7.integration;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client_7.list.Child;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtraFileITest extends VTMScriptTestBase {
    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        fileText = "test_file";
        fileName = TESTNAME;
    }

    /**
     * Tests the creation of an Extra File
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws IOException
     */
    @Test
    public void atestCreateExtraFile() throws IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {

        client.createExtraFile(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getExtraFile(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));
    }

    /**
     * Tests the updating of a Extra File
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws IOException
     */
    @Test
    public void btestUpdateExtraFile() throws IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        String updatedFileText = "Updated the test script...";

        client.updateExtraFile(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getExtraFile(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }

    /**
     * Tests the retrieval of a list of Extra Files
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     *
     */
    @Test
    public void ctestGetListOfExtraFiles() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        List<Child> children = client.getExtraFiles();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Extra File
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetExtraFile() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        File retrievedFile = client.getExtraFile(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of an Extra File
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void etestDeleteExtraFile() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteExtraFile(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getExtraFile(fileName);
    }
}
