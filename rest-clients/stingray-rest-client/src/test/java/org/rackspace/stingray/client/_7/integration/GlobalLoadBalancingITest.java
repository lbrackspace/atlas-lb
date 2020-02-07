package org.rackspace.stingray.client._7.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.glb.GlobalLoadBalancing;
import org.rackspace.stingray.client.glb.GlobalLoadBalancingBasic;
import org.rackspace.stingray.client.glb.GlobalLoadBalancingProperties;
import org.rackspace.stingray.client.list.Child;

import javax.ws.rs.core.Response;
import java.util.List;


public class GlobalLoadBalancingITest extends StingrayTestBase {
    GlobalLoadBalancing glb;
    GlobalLoadBalancingProperties glbProperties;
    GlobalLoadBalancingBasic glbBasic;
    String vsName;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        glbBasic = new GlobalLoadBalancingBasic();
        glbProperties = new GlobalLoadBalancingProperties();
        glb = new GlobalLoadBalancing();
        glbProperties.setBasic(glbBasic);
        glb.setProperties(glbProperties);
        vsName = TESTNAME;
    }

    /**
     * Tests the creation of a Glb
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateGlb() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        GlobalLoadBalancing createdGlb = client.createGlb(vsName, glb);
        Assert.assertNotNull(createdGlb);
        Assert.assertEquals(createdGlb, client.getGlb(vsName));
    }

    /**
     * Tests the updating of a Glb
     * Verifies using a get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateGlb() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        int testInt = 1;
        glb.getProperties().getBasic().setGeoEffect(testInt);
        GlobalLoadBalancing updatedGlb = client.updateGlb(vsName, glb);
        Assert.assertNotNull(updatedGlb);
        Assert.assertEquals((int) testInt, (int) updatedGlb.getProperties().getBasic().getGeoEffect());
    }


    /**
     * Tests the retrieval of a list of Glbs
     * Retrieves a list of action scripts and checks its size
     *
     * @throws StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfGlbs() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getGlbs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Glb
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetGlb() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        GlobalLoadBalancing retrievedGlb = client.getGlb(vsName);
        Assert.assertNotNull(retrievedGlb);
    }

    /**
     * Tests the deletion of a Glb
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteGlb() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response wasDeleted = client.deleteGlb(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getGlb(vsName);

    }

}
