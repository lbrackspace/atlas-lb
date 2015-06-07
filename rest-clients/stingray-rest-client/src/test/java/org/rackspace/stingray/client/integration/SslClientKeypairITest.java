package org.rackspace.stingray.client.integration;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.pojo.list.Child;
import org.rackspace.stingray.pojo.ssl.client.keypair.ClientKeypair;
import org.rackspace.stingray.pojo.ssl.client.keypair.Basic;
import org.rackspace.stingray.pojo.ssl.client.keypair.Properties;

import java.util.List;


public class SslClientKeypairITest extends StingrayTestBase {
    String vsName;
    ClientKeypair clientKeypair;
    Properties clientKeypairProperties;
    Basic clientKeypairBasic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        vsName = TESTNAME;
        clientKeypair = new ClientKeypair();
        clientKeypairProperties = new Properties();
        clientKeypairBasic = new Basic();

        clientKeypairProperties.setBasic(clientKeypairBasic);
        clientKeypair.setProperties(clientKeypairProperties);

    }

    /**
     * Tests the creation of a Client Keypair
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateClientKeypair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        ClientKeypair createdClientKeypair = client.createClientKeypair(vsName, clientKeypair);
        Assert.assertNotNull(createdClientKeypair);
        Assert.assertEquals(createdClientKeypair, client.getClientKeypair(vsName));
    }

    /**
     * Tests the updating of a Client Keypair
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateClientKeypair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
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
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfClientKeypairs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getClientKeypairs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Client Keypair
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetClientKeypair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        ClientKeypair retrievedKeypair = client.getClientKeypair(vsName);
        Assert.assertNotNull(retrievedKeypair);
    }

    /**
     * Tests the deletion of a Client Keypair
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteClientKeypair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteClientKeypair(vsName);
        Assert.assertTrue(wasDeleted);
        client.getClientKeypair(vsName);
    }


}
