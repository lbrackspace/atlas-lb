package org.rackspace.stingray.client._7.integration;


import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client_7.bandwidth.Bandwidth;
import org.rackspace.stingray.client_7.bandwidth.BandwidthBasic;
import org.rackspace.stingray.client_7.bandwidth.BandwidthProperties;
import org.rackspace.stingray.client_7.list.Child;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BandwidthITest extends VTMTestBase {
    Bandwidth bandwidth;
    BandwidthProperties bandwidthProperties;
    BandwidthBasic bandwidthBasic;
    String vsName;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        bandwidth = new Bandwidth();
        bandwidthProperties = new BandwidthProperties();
        bandwidthBasic = new BandwidthBasic();
        bandwidthProperties.setBasic(bandwidthBasic);
        bandwidth.setProperties(bandwidthProperties);
        vsName = TESTNAME;
    }

    /**
     * Tests the creation of a Bandwidth
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateBandwidth() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        Bandwidth createdBandwidth = client.createBandwidth(vsName, bandwidth);
        Assert.assertNotNull(createdBandwidth);
        Assert.assertEquals(createdBandwidth, client.getBandwidth(vsName));


    }

    /**
     * Tests the updating of a Bandwidth
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void btestUpdateBandwidth() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        int testLimit = 1;
        bandwidth.getProperties().getBasic().setMaximum(testLimit);
        Bandwidth updatedBandwidth = client.updateBandwidth(vsName, bandwidth);
        Assert.assertEquals((int) updatedBandwidth.getProperties().getBasic().getMaximum(), (int) testLimit);
    }

    /**
     * Tests the retrieval of a list of Bandwidths
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void ctestGetListOfBandwidths() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getBandwidths();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Bandwidth
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetBandwidth() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Bandwidth retrievedBandwidth = client.getBandwidth(vsName);
        Assert.assertNotNull(retrievedBandwidth);
    }

    /**
     * Tests the deletion of a Bandwidth
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void edeleteBandwidth() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteBandwidth(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getBandwidth(vsName);
    }

}
