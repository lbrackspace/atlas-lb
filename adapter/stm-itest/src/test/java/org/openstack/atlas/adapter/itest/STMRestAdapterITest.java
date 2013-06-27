package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import java.rmi.RemoteException;

public class STMRestAdapterITest extends STMTestBase {

    @Before
    public void setUp() {
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        //clean up...
    }

    @Ignore
    @Test
    public void updateVirtualServer() throws RollBackException, InsufficientRequestException, RemoteException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        stmAdapter.updateLoadBalancer(config, lb);
        Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN.name().toLowerCase(), stmClient.getPool(loadBalancerName()).getProperties().getLoad_balancing().getAlgorithm());
    }
}
