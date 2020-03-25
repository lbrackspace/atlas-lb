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
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import javax.persistence.RollbackException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ChangeHostListenerTest extends VTMTestBase {
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
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
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

    private ChangeHostListener changeHostListener;

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
        changeHostListener = new ChangeHostListener();
        changeHostListener.setLoadBalancerService(loadBalancerService);
        changeHostListener.setNotificationService(notificationService);
        changeHostListener.setUsageEventCollection(usageEventCollection);
        changeHostListener.setVirtualIpService(virtualIpService);
        changeHostListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        changeHostListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        changeHostListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSameClusterChangeHosts() throws Exception {

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

        when(objectMessage.getObject()).thenReturn(mdc);
        List<LoadBalancer> lbs = new ArrayList<>();
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        lb.setHost(host);
        lbs.add(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        changeHostListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).changeHostForLoadBalancers(lbs, moveHost);
        verify(loadBalancerService, times(1)).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testDifferentClusterChangeHosts() throws Exception {

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

        when(objectMessage.getObject()).thenReturn(mdc);
        List<LoadBalancer> lbs = new ArrayList<>();
        Host host = new Host();
        host.setId(12);
        mCluster = new Cluster();
        mCluster.setId(3);
        host.setCluster(mCluster);
        lb.setHost(host);
        lbs.add(lb);
        doNothing().when(virtualIpService).updateCluster((VirtualIp) any(), any());
        doNothing().when(virtualIpService).updateCluster((VirtualIpv6) any(), any());
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        changeHostListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService, times(1)).changeHostForLoadBalancers(lbs, moveHost);
        verify(virtualIpService, times(1)).updateCluster((VirtualIp) any(), any());
        verify(virtualIpService, times(0)).updateCluster((VirtualIpv6) any(), any());
        verify(loadBalancerService, times(1)).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testChangeHostListenerSetsErrorStatus() throws Exception {

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

        when(objectMessage.getObject()).thenReturn(mdc);
        List<LoadBalancer> lbs = new ArrayList<>();
        Host host = new Host();
        host.setId(12);
        host.setCluster(mCluster);
        lb.setHost(host);
        lbs.add(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
        doThrow(RollbackException.class).when(reverseProxyLoadBalancerVTMService).changeHostForLoadBalancers(any(), any());

        try {
            changeHostListener.doOnMessage(objectMessage);
        } catch (Exception ex) {
            // Excepted
        }

        verify(reverseProxyLoadBalancerVTMService, times(1)).changeHostForLoadBalancers(lbs, moveHost);
        verify(loadBalancerService, times(1)).setStatus(lb, LoadBalancerStatus.ERROR);

    }

    // TODO: more tests...
}