package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.ssl.keypair.KeypairBasic;
import org.rackspace.vtm.client.ssl.keypair.KeypairProperties;

import javax.ws.rs.core.Response;
import java.util.List;

public class SslKeypairITest extends VTMTestBase {
    Keypair keypair;
    KeypairProperties properties;
    KeypairBasic basic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        basic = new KeypairBasic();
        properties = new KeypairProperties();
        properties.setBasic(basic);
        keypair = new Keypair();
        keypair.setProperties(properties);
    }

    /**
     * Method to test the creation of SSL Keypair
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateSslKeyPair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Keypair createdKeypair = client.createKeypair(TESTNAME, keypair);
        Assert.assertNotNull(createdKeypair);
        Keypair verifyKeypair = client.getKeypair(TESTNAME);
        Assert.assertNotNull(verifyKeypair);
    }

    /**
     * Method that tests the request to retrieve all the names for every SSL Keypair
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfSslKeypairs() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getKeypairs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Keypair
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetSslKeypair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Keypair keypair = client.getKeypair(TESTNAME);
        Assert.assertNotNull(keypair);
    }

    /**
     * Method to delete a specific SSL Keypair
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteSslKeyPair() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response result = client.deleteKeypair(TESTNAME);
        Assert.assertEquals(204, result.getStatus());
        client.getKeypair(TESTNAME);
    }
}
