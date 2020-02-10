package org.rackspace.stingray.client._7.integration;

import org.apache.commons.io.FileUtils;
import org.junit.*;
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
public class SslCacrlITest extends VTMScriptTestBase {

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
     * Tests the creation of a Traffic Script
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void atestCreateCacrl() throws URISyntaxException, IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        //the fileName is what it will be created as. ex: /rules/test_script the file in STM is 'test_script'
        client.createCacrl(fileName, createTestFile(fileName, fileText));
        File gfile = client.getCacrl(fileName);
        Assert.assertNotNull(gfile);
        Assert.assertEquals(fileText, FileUtils.readFileToString(gfile));
    }

    /**
     * Tests the retrieval of a list of Cacrls
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void bgetListOfCacrls() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getCacrls();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Cacrl
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void ctestGetCacrl() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        File retrievedFile = client.getCacrl(fileName);
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
    public void dtestUpdateCacrl() throws URISyntaxException, IOException, VTMRestClientObjectNotFoundException, VTMRestClientException {
        //the filename is the same, we want to update the contents...
        String updatedFileText = "Updated the test script...";

        client.updateCacrl(fileName, createTestFile(fileName, updatedFileText));

        File updatedFile = client.getCacrl(fileName);
        Assert.assertNotNull(updatedFile);
        Assert.assertEquals(updatedFileText, FileUtils.readFileToString(updatedFile));
    }

    /**
     * Tests the deletion of a Cacrl
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void edeleteCacrl() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteCacrl(fileName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getCacrl(fileName);

    }
}
