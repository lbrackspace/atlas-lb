package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
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
        try {
       stmAdapter.deleteLoadBalancer(config, lb);
        } catch(Exception e) {

        }
    }

    //wip...
    @Test
    public void updateAlgorithm() throws RollBackException, InsufficientRequestException, RemoteException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {
        lb.setAlgorithm(LoadBalancerAlgorithm.RANDOM);
        stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
        Assert.assertEquals(LoadBalancerAlgorithm.RANDOM.name().toLowerCase(), stmClient.getPool(loadBalancerName()).getProperties().getLoad_balancing().getAlgorithm());
    }

    @Test
    public void addVip() throws RollBackException, InsufficientRequestException, RemoteException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

        vip1 = new VirtualIp();
        vip1.setId(1000042);
        vip1.setIpAddress("10.69.0.61");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip1);

        Assert.assertEquals(1, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
        lb.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
        stmAdapter.updateVirtualIps(config, lb);
        Assert.assertEquals(2, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
    }

    @Test
    public void removeVip() throws RollBackException, InsufficientRequestException, RemoteException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

        VirtualIp vip = new VirtualIp();
        vip.setId(1000042);
        vip.setIpAddress("10.69.0.61");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip);

        Assert.assertEquals(1, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
        lb.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
        stmAdapter.updateVirtualIps(config, lb);
        Assert.assertEquals(2, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
//        Assert.assertTrue(stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().contains("10.69.0.61"));

//        lb.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
        stmAdapter.deleteVirtualIp(config, lb, vip.getId());
        Assert.assertEquals(1, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
//        Assert.assertFalse(stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().contains("10.69.0.61"));

    }

    @Test
    public void shouldNotDeleteVipsWithBadIDs() throws RollBackException, InsufficientRequestException, RemoteException, StingrayRestClientException, StingrayRestClientObjectNotFoundException {

        VirtualIp vip = new VirtualIp();
        vip.setId(1000042);
        vip.setIpAddress("10.69.0.61");
        LoadBalancerJoinVip loadBalancerJoinVip = new LoadBalancerJoinVip();
        loadBalancerJoinVip.setVirtualIp(vip);

        Assert.assertEquals(1, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
        lb.getLoadBalancerJoinVipSet().add(loadBalancerJoinVip);
        stmAdapter.updateVirtualIps(config, lb);
        Assert.assertEquals(2, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
//        Assert.assertTrue(stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().contains("10.69.0.61"));

//        lb.getLoadBalancerJoinVipSet().remove(loadBalancerJoinVip);
        stmAdapter.deleteVirtualIp(config, lb, 34);
        Assert.assertEquals(2, stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().size());
//        Assert.assertFalse(stmClient.getVirtualServer(loadBalancerName()).getProperties().getBasic().getListen_on_traffic_ips().contains("10.69.0.61"));

    }
}
