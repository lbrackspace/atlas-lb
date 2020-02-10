package org.rackspace.stingray.client._7.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client_7.list.Child;
import org.rackspace.stingray.client_7.protection.Protection;
import org.rackspace.stingray.client_7.protection.ProtectionBasic;
import org.rackspace.stingray.client_7.protection.ProtectionProperties;

import javax.ws.rs.core.Response;
import java.util.List;

public class ProtectionITest extends VTMTestBase {;
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateProtection() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Protection createdProtection = client.createProtection(vsName, protection);
        Assert.assertNotNull(createdProtection);
        Assert.assertEquals(createdProtection, client.getProtection(vsName));
    }

    /**
     * Tests the updating of a Protection
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateProtection() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
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
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfProtections() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getProtections();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Protection
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetProtection() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Protection retrievedProtection = client.getProtection(vsName);
        Assert.assertNotNull(retrievedProtection);
    }

    /**
     * Tests the deletion of a Protection
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteProtection() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteProtection(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getProtection(vsName);
    }

}
