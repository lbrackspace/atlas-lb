package org.rackspace.stingray.client.integration;

import org.junit.*;
import org.apache.commons.io.FileUtils;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExtraFileITest extends StingrayScriptTestBase {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void atestCreateExtraFile() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {

        client.createExtraFile(fileName, createTestFile(fileName, fileText));
        File createdFile = client.getExtraFile(fileName);
        Assert.assertNotNull(createdFile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(createdFile));
    }

    /**
     * Tests the updating of a Extra File
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void btestUpdateExtraFile() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void ctestGetListOfExtraFiles() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getExtraFiles();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Extra File
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetExtraFile() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        File retrievedFile = client.getExtraFile(fileName);
        Assert.assertNotNull(retrievedFile);
    }

    /**
     * Tests the deletion of an Extra File
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void etestDeleteExtraFile() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteExtraFile(fileName);
        Assert.assertTrue(wasDeleted);
        client.getExtraFile(fileName);
    }
}
