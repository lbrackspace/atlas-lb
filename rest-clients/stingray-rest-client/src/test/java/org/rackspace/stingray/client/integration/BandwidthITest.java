package org.rackspace.stingray.client.integration;


import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.pojo.bandwidth.Bandwidth;
import org.rackspace.stingray.pojo.bandwidth.Basic;
import org.rackspace.stingray.pojo.bandwidth.Properties;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.pojo.list.Child;

import java.util.List;

public class BandwidthITest extends StingrayTestBase {
    Bandwidth bandwidth;
    Properties bandwidthProperties;
    Basic bandwidthBasic;
    String vsName;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        bandwidth = new Bandwidth();
        bandwidthProperties = new Properties();
        bandwidthBasic = new Basic();
        bandwidthProperties.setBasic(bandwidthBasic);
        bandwidth.setProperties(bandwidthProperties);
        vsName = TESTNAME;
    }

    /**
     * Tests the creation of a Bandwidth
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateBandwidth() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Bandwidth createdBandwidth = client.createBandwidth(vsName, bandwidth);
        Assert.assertNotNull(createdBandwidth);
        Assert.assertEquals(createdBandwidth, client.getBandwidth(vsName));


    }

    /**
     * Tests the updating of a Bandwidth
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateBandwidth() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        int testLimit = 1;
        bandwidth.getProperties().getBasic().setMaximum(testLimit);
        Bandwidth updatedBandwidth = client.updateBandwidth(vsName, bandwidth);
        Assert.assertEquals((int) updatedBandwidth.getProperties().getBasic().getMaximum(), (int) testLimit);
    }

    /**
     * Tests the retrieval of a list of Bandwidths
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfBandwidths() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getBandwidths();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Bandwidth
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetBandwidth() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Bandwidth retrievedBandwidth = client.getBandwidth(vsName);
        Assert.assertNotNull(retrievedBandwidth);
    }

    /**
     * Tests the deletion of a Bandwidth
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void deleteBandwidth() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Boolean wasDeleted = client.deleteBandwidth(vsName);
        Assert.assertTrue(wasDeleted);
        client.getBandwidth(vsName);
    }

}
