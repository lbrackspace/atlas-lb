package org.openstack.atlas.api.mgmt.async;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.async.util.VTMTestBase;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.NetInterface;
import org.openstack.atlas.service.domain.services.*;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class SetHostsSubnetMappingListenerTest extends VTMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private String LOAD_BALANCER_NAME = "SOME_LB_NAME";
    private LoadBalancerAlgorithm LOAD_BALANCER_ALGORITHM = LoadBalancerAlgorithm.ROUND_ROBIN;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private EsbRequest esbRequest;
    @Mock
    private HostService hostService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ClusterService clusterService;
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private VirtualIpService virtualIpService;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private RestApiConfiguration config;

    private MgmtSetHostsSubnetMappingListener mgmtSetHostsSubnetMappingListener;
    private Hostsubnet hostsubnet;
    private Hostssubnet hostssubnet;
    private ArrayList<Hostsubnet> hostssubnetList;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setAlgorithm(LOAD_BALANCER_ALGORITHM);
        lb.setName(LOAD_BALANCER_NAME);
        lb.setStatus(LoadBalancerStatus.ACTIVE);

        hostsubnet = new Hostsubnet();
        hostssubnet = new Hostssubnet();
        hostssubnetList = new ArrayList<>();
        ArrayList<NetInterface> netInterfaces = new ArrayList<>();
        NetInterface ni1 = new NetInterface();
        ni1.setCidrs(new ArrayList<>());
        netInterfaces.add(ni1);
        hostsubnet.setName("h1");
        hostsubnet.setNetInterfaces(netInterfaces);
        hostssubnetList.add(hostsubnet);
        hostssubnet.setHostsubnets(hostssubnetList);

        mgmtSetHostsSubnetMappingListener = new MgmtSetHostsSubnetMappingListener();
        mgmtSetHostsSubnetMappingListener.setHostService(hostService);
        mgmtSetHostsSubnetMappingListener.setNotificationService(notificationService);
        mgmtSetHostsSubnetMappingListener.setUsageEventCollection(usageEventCollection);
        mgmtSetHostsSubnetMappingListener.setVirtualIpService(virtualIpService);
        mgmtSetHostsSubnetMappingListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        mgmtSetHostsSubnetMappingListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        mgmtSetHostsSubnetMappingListener.setClusterService(clusterService);
        mgmtSetHostsSubnetMappingListener.setConfiguration(config);
    }

    @Test
    public void testSetSubnetMappings() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());

        Host moveHost = new Host();
        Cluster mCluster = new Cluster();
        mCluster.setId(1);
        moveHost.setId(13);
        moveHost.setCluster(mCluster);
        mdc.setMoveHost(moveHost);

        ArrayList lbids = new ArrayList<>();
        lbids.add(lb.getId());
        mdc.setIds(lbids);

        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId(12);
        host1.setCluster(mCluster);
        host1.setTrafficManagerName("t1");
        Host host2 = new Host();
        host2.setId(13);
        host2.setCluster(mCluster);
        host2.setTrafficManagerName("t1");
        hosts.add(host1);
        hosts.add(host2);
        for(Host h : hosts){
            lb.setHost(h);
            lbs.add(lb);
        }

        when(esbRequest.getCluster()).thenReturn(mCluster);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(clusterService.getHosts(ArgumentMatchers.anyInt())).thenReturn(hosts);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostsSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(2)).setSubnetMappings(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void testSetSubnetMappingsAddVips() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());

        Host moveHost = new Host();
        Cluster mCluster = new Cluster();
        mCluster.setId(1);
        moveHost.setId(13);
        moveHost.setCluster(mCluster);
        mdc.setMoveHost(moveHost);

        ArrayList lbids = new ArrayList<>();
        lbids.add(lb.getId());
        mdc.setIds(lbids);

        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId(12);
        host1.setCluster(mCluster);
        host1.setTrafficManagerName("t1");
        Host host2 = new Host();
        host2.setId(13);
        host2.setCluster(mCluster);
        host2.setTrafficManagerName("t1");
        hosts.add(host1);
        hosts.add(host2);
        for(Host h : hosts){
            lb.setHost(h);
            lbs.add(lb);
        }

        when(esbRequest.getCluster()).thenReturn(mCluster);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(clusterService.getHosts(ArgumentMatchers.anyInt())).thenReturn(hosts);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.PUBLIC);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostsSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(2)).setSubnetMappings(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(clusterService, times(1)).addVirtualIpBlocks(any(), eq(VirtualIpType.PUBLIC), eq(mCluster.getId()));
        verify(notificationService, times(0)).saveAlert(any(), any(), any());
    }

    @Test
    public void testSetSubnetMappingsAddSnetVips() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());

        Host moveHost = new Host();
        Cluster mCluster = new Cluster();
        mCluster.setId(1);
        moveHost.setId(13);
        moveHost.setCluster(mCluster);
        mdc.setMoveHost(moveHost);

        ArrayList lbids = new ArrayList<>();
        lbids.add(lb.getId());
        mdc.setIds(lbids);

        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId(12);
        host1.setCluster(mCluster);
        host1.setTrafficManagerName("t1");
        Host host2 = new Host();
        host2.setId(13);
        host2.setCluster(mCluster);
        host2.setTrafficManagerName("t1");
        hosts.add(host1);
        hosts.add(host2);
        for(Host h : hosts){
            lb.setHost(h);
            lbs.add(lb);
        }
        when(esbRequest.getCluster()).thenReturn(mCluster);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(clusterService.getHosts(ArgumentMatchers.anyInt())).thenReturn(hosts);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.SERVICENET);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostsSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(2)).setSubnetMappings(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(clusterService, times(1)).addVirtualIpBlocks(any(), eq(VirtualIpType.SERVICENET), eq(mCluster.getId()));
        verify(notificationService, times(0)).saveAlert(any(), any(), any());
    }

    @Test
    public void testSetSubnetMappingsAddVipsSaveNotifOnFail() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());

        Host moveHost = new Host();
        Cluster mCluster = new Cluster();
        mCluster.setId(1);
        moveHost.setId(13);
        moveHost.setCluster(mCluster);
        mdc.setMoveHost(moveHost);

        ArrayList lbids = new ArrayList<>();
        lbids.add(lb.getId());
        mdc.setIds(lbids);

        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId(12);
        host1.setCluster(mCluster);
        host1.setTrafficManagerName("t1");
        Host host2 = new Host();
        host2.setId(13);
        host2.setCluster(mCluster);
        host2.setTrafficManagerName("t1");
        hosts.add(host1);
        hosts.add(host2);
        for(Host h : hosts){
            lb.setHost(h);
            lbs.add(lb);
        }
        when(esbRequest.getCluster()).thenReturn(mCluster);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(clusterService.getHosts(ArgumentMatchers.anyInt())).thenReturn(hosts);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.PUBLIC);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        doThrow(Exception.class).when(clusterService).addVirtualIpBlocks(any(), any(), any());

        mgmtSetHostsSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(2)).setSubnetMappings(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(clusterService, times(1)).addVirtualIpBlocks(any(), any(), any());
        verify(notificationService, times(1)).saveAlert(any(), any(), any());
    }

    @Test
    public void testSetSubnetMappingsInCaseOfExceptionAndWhenRollbackHappened() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());

        Host moveHost = new Host();
        Cluster mCluster = new Cluster();
        mCluster.setId(1);
        moveHost.setId(13);
        moveHost.setCluster(mCluster);
        mdc.setMoveHost(moveHost);

        ArrayList lbids = new ArrayList<>();
        lbids.add(lb.getId());
        mdc.setIds(lbids);

        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId(12);
        host1.setCluster(mCluster);
        host1.setTrafficManagerName("t1");
        Host host2 = new Host();
        host2.setId(13);
        host2.setCluster(mCluster);
        host2.setTrafficManagerName("t2");
        hosts.add(host1);
        hosts.add(host2);
        for(Host h : hosts){
            lb.setHost(h);
            lbs.add(lb);
        }

        when(esbRequest.getCluster()).thenReturn(mCluster);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(clusterService.getHosts(ArgumentMatchers.anyInt())).thenReturn(hosts);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
        doNothing().doThrow(RollBackException.class).when(reverseProxyLoadBalancerVTMService).setSubnetMappings(ArgumentMatchers.<Host>any(), ArgumentMatchers.<Hostssubnet>any());
        mgmtSetHostsSubnetMappingListener.doOnMessage(objectMessage);
        verify(reverseProxyLoadBalancerVTMService, times(1)).deleteSubnetMappings(ArgumentMatchers.eq(host1), ArgumentMatchers.any());
        verify(notificationService, times(1)).saveAlert(any(), any(), any());
        verify(clusterService, times(0)).addVirtualIpBlocks(any(), eq(VirtualIpType.PUBLIC), eq(mCluster.getId()));
    }
}