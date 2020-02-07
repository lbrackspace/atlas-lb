package org.rackspace.stingray.client._7.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.tm.TrafficManager;
import org.rackspace.stingray.client.tm.TrafficManagerBasic;
import org.rackspace.stingray.client.tm.TrafficManagerProperties;

import javax.ws.rs.core.Response;
import java.util.List;

public class TrafficManagerITest extends StingrayTestBase {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testCreateTrafficManager() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        TrafficManager createdManager = client.createTrafficManager(TESTNAME, manager);
        String t = manager.toString();
        Assert.assertNotNull(createdManager);
    }

    /**
     * Tests the retrieval of a list of Traffic Managers
     * Retrieves a list of action scripts and checks its size
     *
     * @throws StingrayRestClientException
     *
     */
    @Test
    public void testGetListOfTrafficManagers() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getTrafficManagers();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Traffic Manager
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void testGetTrafficManager() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        TrafficManager trafficManager = client.getTrafficManager(TESTNAME);
        Assert.assertNotNull(trafficManager);
    }

    /**
     * Tests the deletion of a Traffic Manager
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void testDeleteTrafficManager() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response result = client.deleteTrafficManager(TESTNAME);
        Assert.assertEquals(204, result.getStatus());
        client.getTrafficManager(TESTNAME);
    }
}
