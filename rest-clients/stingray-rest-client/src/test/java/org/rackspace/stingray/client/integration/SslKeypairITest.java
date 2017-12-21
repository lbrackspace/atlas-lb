package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.ssl.keypair.Keypair;
import org.rackspace.stingray.client.ssl.keypair.KeypairBasic;
import org.rackspace.stingray.client.ssl.keypair.KeypairProperties;

import java.util.List;

public class SslKeypairITest extends StingrayTestBase {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateSslKeyPair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Keypair createdKeypair = client.createKeypair(TESTNAME, keypair);
        Assert.assertNotNull(createdKeypair);
        Keypair verifyKeypair = client.getKeypair(TESTNAME);
        Assert.assertNotNull(verifyKeypair);
    }

    /**
     * Method that tests the request to retrieve all the names for every SSL Keypair
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfSslKeypairs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getKeypairs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Keypair
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetSslKeypair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Keypair keypair = client.getKeypair(TESTNAME);
        Assert.assertNotNull(keypair);
    }

    /**
     * Method to delete a specific SSL Keypair
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteSslKeyPair() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean result = client.deleteKeypair(TESTNAME);
        Assert.assertTrue(result);
        client.getKeypair(TESTNAME);
    }
}
