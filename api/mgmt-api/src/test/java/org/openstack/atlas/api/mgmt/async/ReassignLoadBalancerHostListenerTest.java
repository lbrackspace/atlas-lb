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
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ReassignLoadBalancerHostListenerTest extends VTMTestBase {
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
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private RestApiConfiguration config;

    private MgmtReassignLoadBalancerHostListener reassignHostListener;

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
        reassignHostListener = new MgmtReassignLoadBalancerHostListener();
        reassignHostListener.setLoadBalancerService(loadBalancerService);
        reassignHostListener.setNotificationService(notificationService);
        reassignHostListener.setUsageEventCollection(usageEventCollection);
        reassignHostListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        reassignHostListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        reassignHostListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testReassignHosts() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_UPDATE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());
        when(objectMessage.getObject()).thenReturn(esbRequest);
        List<LoadBalancer> lbs = new ArrayList<>();
        Host host = new Host();
        host.setId(12);
        lb.setHost(host);
        lbs.add(lb);
        when(esbRequest.getLoadBalancers()).thenReturn(lbs);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        reassignHostListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).changeHostForLoadBalancers(lbs, host);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testReassignHostsCalledError() throws Exception {

        lb.setStatus(LoadBalancerStatus.ERROR);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());
        when(objectMessage.getObject()).thenReturn(esbRequest);
        ArrayList<LoadBalancer> lbs = new ArrayList<>();
        Host host = new Host();
        host.setId(12);
        lb.setHost(host);
        lbs.add(lb);
        // Trigger exception
        when(esbRequest.getLoadBalancers()).thenReturn(null);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        try {
            reassignHostListener.doOnMessage(objectMessage);
        } catch (Exception ex) {
            // Excepted
        }

        verify(reverseProxyLoadBalancerVTMService, times(0)).changeHostForLoadBalancers(null, host);
        verify(loadBalancerService).setStatus(null, LoadBalancerStatus.ERROR);

    }

    // TODO: more tests...
}