package org.rackspace.vtm.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.traffic.ip.TrafficIpBasic;
import org.rackspace.vtm.client.traffic.ip.TrafficIpProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TrafficIpITest extends VTMTestBase {
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
        } catch (VTMRestClientException | VTMRestClientObjectNotFoundException e) {
            Assert.fail("Could not create traffic ip " + e.getMessage());
        }
        Assert.assertNotNull(tipTester);
    }

    /**
     * Tests the creation of a Traffic IP
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateTrafficIp() throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        tipTester = client.createTrafficIp(TESTNAME, tip);
        Assert.assertNotNull(tipTester);
    }

    /**
     * Tests the retrieval of a list of Traffic Ips
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException
     *
     */
    @Test
    public void btestGetListOfTrafficIps() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getTrafficIps();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Traffic Ip
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void ctestGetTrafficIp() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
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
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void dtestDeleteTrafficIp() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response result = client.deleteTrafficIp(TESTNAME);
        Assert.assertEquals(204, result.getStatus());
        client.getTrafficIp(TESTNAME);
    }
}
