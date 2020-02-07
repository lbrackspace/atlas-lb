package org.rackspace.stingray.client.integration;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.traffic.ip.TrafficIpProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TrafficIpITest extends StingrayTestBase {
    TrafficIp tip;
    TrafficIp tipTester;
    TrafficIpProperties properties;
    TrafficIpBasic basic;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        basic = new TrafficIpBasic();
        properties = new TrafficIpProperties();
        properties.setBasic(basic);
        tip = new TrafficIp();
        tip.setProperties(properties);

        try {
            tipTester = client.createTrafficIp(TESTNAME, tip);
        } catch (StingrayRestClientException | StingrayRestClientObjectNotFoundException e) {
            Assert.fail("Could not create traffic ip " + e.getMessage());
        }
        Assert.assertNotNull(tipTester);
    }

    /**
     * Tests the creation of a Traffic IP
     * Verifies using get and a comparison of content contained
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateTrafficIp() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        tipTester = client.createTrafficIp(TESTNAME, tip);
        Assert.assertNotNull(tipTester);
    }

    /**
     * Tests the retrieval of a list of Traffic Ips
     * Retrieves a list of action scripts and checks its size
     *
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientException
     *
     */
    @Test
    public void btestGetListOfTrafficIps() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getTrafficIps();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Traffic Ip
     * Retrieves the specific Action Script created earlier
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void ctestGetTrafficIp() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getTrafficIps();
        Assert.assertTrue(children.size() > 0);
        Child child = children.get(0);
        String vsname = child.getName();
        TrafficIp trafficIp = client.getTrafficIp(vsname);
        Assert.assertNotNull(trafficIp);
    }

    /**
     * Tests the deletion of a Traffic Ip
     * Checks return of the delete call, and throws an error
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void dtestDeleteTrafficIp() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response result = client.deleteTrafficIp(TESTNAME);
        Assert.assertEquals(204, result.getStatus());
        client.getTrafficIp(TESTNAME);
    }
}
