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

public class SslCacrlITest extends StingrayScriptTestBase {

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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @Test
    public void testCreateCacrl() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void getListOfCacrls() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getCacrls();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Cacrl
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetCacrl() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        File retrievedFile = client.getCacrl(fileName);
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
    public void testUpdateCacrl() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void deleteCacrl() throws StingrayRestClientException, URISyntaxException, IOException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteCacrl(fileName);
        Assert.assertTrue(wasDeleted);
        client.getCacrl(fileName);

    }
}
