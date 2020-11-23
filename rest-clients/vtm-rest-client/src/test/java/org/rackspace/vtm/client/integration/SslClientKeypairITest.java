package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.ssl.client.keypair.ClientKeypair;
import org.rackspace.vtm.client.ssl.client.keypair.ClientKeypairBasic;
import org.rackspace.vtm.client.ssl.client.keypair.ClientKeypairProperties;

import javax.ws.rs.core.Response;
import java.util.List;


public class SslClientKeypairITest extends VTMTestBase {
    String vsName;
    ClientKeypair clientKeypair;
    ClientKeypairProperties clientKeypairProperties;
    ClientKeypairBasic clientKeypairBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        clientKeypair = new ClientKeypair();
        clientKeypairProperties = new ClientKeypairProperties();
        clientKeypairBasic = new ClientKeypairBasic();

        clientKeypairProperties.setBasic(clientKeypairBasic);
        clientKeypair.setProperties(clientKeypairProperties);

}

    /**
     * Tests the creation of a Client Keypair
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateClientKeypair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        ClientKeypair createdClientKeypair = client.createClientKeypair(vsName, clientKeypair);
        Assert.assertNotNull(createdClientKeypair);
        Assert.assertEquals(createdClientKeypair, client.getClientKeypair(vsName));
    }

    /**
     * Tests the updating of a Client Keypair
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateClientKeypair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        String updateNote = "qwertyuiop";
        clientKeypair.getProperties().getBasic().setNote(updateNote);
        ClientKeypair updatedKeypair = client.updateClientKeypair(vsName, clientKeypair);
        Assert.assertNotNull(updatedKeypair);
        String actualNote = updatedKeypair.getProperties().getBasic().getNote();
        Assert.assertEquals(updateNote, actualNote);


    }

    /**
     * Tests the retrieval of a list of Client Keypairs
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfClientKeypairs() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getClientKeypairs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Client Keypair
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetClientKeypair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        ClientKeypair retrievedKeypair = client.getClientKeypair(vsName);
        Assert.assertNotNull(retrievedKeypair);
    }

    /**
     * Tests the deletion of a Client Keypair
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteClientKeypair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteClientKeypair(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getClientKeypair(vsName);
    }


}
