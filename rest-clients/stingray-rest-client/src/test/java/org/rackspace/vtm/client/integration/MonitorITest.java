package org.rackspace.vtm.client.integration;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.list.Child;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.monitor.MonitorBasic;
import org.rackspace.vtm.client.monitor.MonitorProperties;

import javax.ws.rs.core.Response;
import java.util.List;

public class MonitorITest extends VTMTestBase {
    Monitor monitor;
    MonitorProperties monitorProperties;
    MonitorBasic monitorBasic;
    String vsName;

    /**
     * Initializes variables prior to test execution
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        monitor = new Monitor();
        monitorProperties = new MonitorProperties();
        monitorBasic = new MonitorBasic();
        vsName = TESTNAME;

        monitorProperties.setBasic(monitorBasic);
        monitor.setProperties(monitorProperties);
    }

    /**
     * Tests the creation of a Monitor
     * Verifies using get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testCreateMonitor() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Monitor createdMonitor = client.createMonitor(vsName, monitor);
        Assert.assertNotNull(createdMonitor);
        Assert.assertEquals(createdMonitor, client.getMonitor(vsName));

    }

    /**
     * Tests the updating of a Monitor
     * Verifies using a get and a comparison of content contained
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testUpdateMonitor() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        int updateTimeout = 17;
        monitor.getProperties().getBasic().setTimeout(updateTimeout);
        Monitor updatedMonitor = client.updateMonitor(vsName, monitor);
        Assert.assertNotNull(updatedMonitor);
        Assert.assertEquals(updateTimeout, (int) updatedMonitor.getProperties().getBasic().getTimeout());
    }


    /**
     * Tests the retrieval of a list of Monitors
     * Retrieves a list of action scripts and checks its size
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     *
     */
    @Test
    public void testGetListOfMonitors() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        List<Child> children = client.getMonitors();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * Tests the get function for an individual Monitor
     * Retrieves the specific Action Script created earlier
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test
    public void testGetMonitor() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Monitor retrievedMonitor = client.getMonitor(vsName);
        Assert.assertNotNull(retrievedMonitor);
    }

    /**
     * Tests the deletion of a Monitor
     * Checks return of the delete call, and throws an error
     *
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Test(expected = VTMRestClientObjectNotFoundException.class)
    public void testDeleteMonitor() throws VTMRestClientObjectNotFoundException, VTMRestClientException {
        Response wasDeleted = client.deleteMonitor(vsName);
        Assert.assertEquals(204, wasDeleted.getStatus());
        client.getMonitor(vsName);

    }


}
