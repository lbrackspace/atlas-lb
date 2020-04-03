package org.rackspace.stingray.client.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.list.Child;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolProperties;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import javax.ws.rs.core.Response;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VirtualServerITest extends StingrayTestBase {
    String poolName;
    String vsName;
    Integer port;
    Pool pool;
    VirtualServer virtualServer;
    VirtualServerProperties properties;
    VirtualServerBasic basic;

    /**
     * This method is the beginning for every test following.  Initial steps to the testing are completed here.
     */
    @Before
    @Override
    public void standUp() throws DecryptException {
        super.standUp();
        virtualServer = new VirtualServer();
        properties = new VirtualServerProperties();
        basic = new VirtualServerBasic();
        poolName = TESTNAME;
        vsName = TESTNAME;
        port = 8998;
        pool = new Pool();
        pool.setProperties(new PoolProperties());
        basic.setPool(poolName);
        basic.setPort(port);
        properties.setBasic(basic);
        virtualServer.setProperties(properties);
    }

    /**
     * This method tests the create virtual server request, and will verify its creation with a get request.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void atestCreateVirtualServer() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Pool createdPool = client.createPool(poolName, pool);
        Assert.assertNotNull(createdPool);
        VirtualServer vs = client.createVirtualServer(vsName, virtualServer);
        Assert.assertNotNull(vs);
        List<Child> children = client.getVirtualServers();
        Boolean containsVirtualServer = false;
        for (Child child : children) {
            if (child.getName().equals(vsName)) {
                containsVirtualServer = true;
            }
        }
        Assert.assertTrue(containsVirtualServer);
    }

    @Test
    public void btestUpdateVirtualServer() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Integer modPort = 8999;
        virtualServer.getProperties().getBasic().setPort(modPort);
        VirtualServer vs = client.updateVirtualServer(vsName, virtualServer);
        Assert.assertTrue(vs.getProperties().getBasic().getPort().equals(modPort));
    }

    /**
     * This method tests that a list of children holding the name and URI for every virtual server can be retrieved.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void ctestGetVirtualServersList() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        List<Child> children = client.getVirtualServers();
        Assert.assertTrue(children.size() > 0);
    }

    /**
     * This method tests that one virtual server can be retrieved.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test
    public void dtestGetVirtualServer() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        VirtualServer vs = client.getVirtualServer(vsName);
        Assert.assertNotNull(vs);
    }

    /**
     * This method tests that our originally created virtual server is able to be deleted.
     *
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    @Test(expected = StingrayRestClientObjectNotFoundException.class)
    public void etestDeleteVirtualServer() throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        Response result = client.deleteVirtualServer(vsName);
        Assert.assertEquals(204, result.getStatus());
        client.getVirtualServer(vsName);
    }
}