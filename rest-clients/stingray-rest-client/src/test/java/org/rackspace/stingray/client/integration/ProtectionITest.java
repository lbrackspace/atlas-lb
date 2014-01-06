package org.rackspace.stingray.client.integration;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.protection.Protection;
import org.rackspace.stingray.client.protection.ProtectionBasic;
import org.rackspace.stingray.client.protection.ProtectionProperties;

import java.util.List;

public class ProtectionITest extends StingrayTestBase {;
    String vsName = TESTNAME;
    Protection protection;
    ProtectionProperties protectionProperties;
    ProtectionBasic protectionBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        protection = new Protection();
        protectionProperties = new ProtectionProperties();
        protectionBasic = new ProtectionBasic();

        protectionProperties.setBasic(protectionBasic);
        protection.setProperties(protectionProperties);

    }

    /**
     * Tests the creation of a Protection
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateProtection() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Protection createdProtection = client.createProtection(vsName, protection);
        Assert.assertNotNull(createdProtection);
        Assert.assertEquals(createdProtection, client.getProtection(vsName));
    }

    /**
     * Tests the updating of a Protection
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateProtection() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        String updateNote = "qwertyuiop";
        protection.getProperties().getBasic().setNote(updateNote);
        Protection updatedProtection = client.updateProtection(vsName, protection);
        String actualNote = updatedProtection.getProperties().getBasic().getNote();
        Assert.assertEquals(updateNote, actualNote);
    }


    /**
     * Tests the retrieval of a list of Protections
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfProtections() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getProtections();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Protection
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetProtection() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Protection retrievedProtection = client.getProtection(vsName);
        Assert.assertNotNull(retrievedProtection);
    }

    /**
     * Tests the deletion of a Protection
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteProtection() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteProtection(vsName);
        Assert.assertTrue(wasDeleted);
        client.getProtection(vsName);
    }

}
