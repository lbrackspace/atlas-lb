package org.openstack.atlas.api.mgmt.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.async.util.VTMTestBase;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.*;
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

public class SetHostSubnetMappingListenerTest extends VTMTestBase {
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

    private MgmtSetHostSubnetMappingListener mgmtSetHostSubnetMappingListener;
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

        mgmtSetHostSubnetMappingListener = new MgmtSetHostSubnetMappingListener();
        mgmtSetHostSubnetMappingListener.setHostService(hostService);
        mgmtSetHostSubnetMappingListener.setNotificationService(notificationService);
        mgmtSetHostSubnetMappingListener.setUsageEventCollection(usageEventCollection);
        mgmtSetHostSubnetMappingListener.setVirtualIpService(virtualIpService);
        mgmtSetHostSubnetMappingListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        mgmtSetHostSubnetMappingListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        mgmtSetHostSubnetMappingListener.setClusterService(clusterService);
        mgmtSetHostSubnetMappingListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
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
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        host.setTrafficManagerName("t1");
        lb.setHost(host);
        lbs.add(lb);
        when(esbRequest.getHost()).thenReturn(host);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(hostService.getById(12)).thenReturn(host);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(host, hostssubnet);
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
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        host.setTrafficManagerName("t1");
        lb.setHost(host);
        lbs.add(lb);
        when(esbRequest.getHost()).thenReturn(host);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(hostService.getById(12)).thenReturn(host);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.PUBLIC);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(host, hostssubnet);
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
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        host.setTrafficManagerName("t1");
        lb.setHost(host);
        lbs.add(lb);
        when(esbRequest.getHost()).thenReturn(host);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(hostService.getById(12)).thenReturn(host);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.SERVICENET);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        mgmtSetHostSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(host, hostssubnet);
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
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        host.setTrafficManagerName("t1");
        lb.setHost(host);
        lbs.add(lb);
        when(esbRequest.getHost()).thenReturn(host);
        when(esbRequest.getHostssubnet()).thenReturn(hostssubnet);
        when(hostService.getById(12)).thenReturn(host);
        when(esbRequest.getAddVips()).thenReturn(true);
        when(esbRequest.getVirtualIpType()).thenReturn(VirtualIpType.PUBLIC);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        doThrow(Exception.class).when(clusterService).addVirtualIpBlocks(any(), any(), any());

        mgmtSetHostSubnetMappingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).setSubnetMappings(host, hostssubnet);
        verify(clusterService, times(1)).addVirtualIpBlocks(any(), any(), any());
        verify(notificationService, times(1)).saveAlert(any(), any(), any());
    }


}