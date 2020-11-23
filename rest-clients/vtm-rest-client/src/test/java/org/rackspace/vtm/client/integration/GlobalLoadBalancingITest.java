package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.glb.GlobalLoadBalancing;
import org.rackspace.vtm.client.glb.GlobalLoadBalancingBasic;
import org.rackspace.vtm.client.glb.GlobalLoadBalancingProperties;
import org.rackspace.vtm.client.list.Child;

import javax.ws.rs.core.Response;
import java.util.List;


public class GlobalLoadBalancingITest extends VTMTestBase {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateGlb() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        GlobalLoadBalancing createdGlb = client.createGlb(vsName, glb);
        Assert.assertNotNull(createdGlb);
        Assert.assertEquals(createdGlb, client.getGlb(vsName));
    }

    /**
     * Tests the updating of a Glb
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateGlb() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
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
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfGlbs() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getGlbs();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Glb
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetGlb() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        GlobalLoadBalancing retrievedGlb = client.getGlb(vsName);
        Assert.assertNotNull(retrievedGlb);
    }

    /**
     * Tests the deletion of a Glb
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteGlb() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteGlb(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getGlb(vsName);

    }

}
