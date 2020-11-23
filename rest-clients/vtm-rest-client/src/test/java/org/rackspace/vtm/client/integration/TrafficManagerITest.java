package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.tm.TrafficManager;
import org.rackspace.vtm.client.tm.TrafficManagerBasic;
import org.rackspace.vtm.client.tm.TrafficManagerProperties;
import org.rackspace.vtm.client.tm.Trafficip;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrafficManagerITest extends VTMTestBase {
    TrafficManager manager;
    TrafficManagerProperties properties;
    TrafficManagerBasic basic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        basic = new TrafficManagerBasic();
        properties = new TrafficManagerProperties();
        properties.setBasic(basic);
        manager = new TrafficManager();
        manager.setProperties(properties);
    }

    /**
     * Tests the creation of a Traffic Manager
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateTrafficManager() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        TrafficManager createdManager = client.createTrafficManager(TESTNAME, manager);
        String t = manager.toString();
        Assert.assertNotNull(createdManager);
    }

    /**
     * Tests the retrieval of a list of Traffic Managers
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void testGetListOfTrafficManagers() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getTrafficManagers();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Traffic Manager
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetTrafficManager() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        TrafficManager trafficManager = client.getTrafficManager(TESTNAME);
        Assert.assertNotNull(trafficManager);
    }

    /**
     * Tests the deletion of a Traffic Manager
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteTrafficManager() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response result = client.deleteTrafficManager(TESTNAME);
        Assert.assertEquals(204, result.getStatus());
        client.getTrafficManager(TESTNAME);
    }

    /**
     * Tests the creation/updates of a Traffic Manager subnet mappings
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateTrafficManagerSubnetMappings() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        TrafficManager createdManager = client.createTrafficManager(TESTNAME, manager);
        String t = manager.toString();
        Assert.assertNotNull(createdManager);
        List<Trafficip> tips = manager.getProperties().getBasic().getTrafficip();
        Trafficip tip = new Trafficip();
        tip.setName("t1");
        Set<String> nets = new HashSet<>();
        nets.add("192.168.0.0/16");
        tip.setNetworks(nets);
        tips.add(tip);
        manager.getProperties().getBasic().setTrafficip(tips);
        client.updateTrafficManager(TESTNAME, manager);

    }
}
