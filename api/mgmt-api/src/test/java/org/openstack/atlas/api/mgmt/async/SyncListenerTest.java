package org.openstack.atlas.api.mgmt.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.async.util.STMTestBase;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SyncListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private String LOAD_BALANCER_NAME = "SOME_LB_NAME";
    private LoadBalancerAlgorithm LOAD_BALANCER_ALGORITHM = LoadBalancerAlgorithm.ROUND_ROBIN;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerStmService;
    @Mock
    private RestApiConfiguration config;

    private SyncListener syncListener;

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
        syncListener = new SyncListener();
        syncListener.setLoadBalancerService(loadBalancerService);
        syncListener.setNotificationService(notificationService);
        syncListener.setUsageEventCollection(usageEventCollection);
        syncListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        syncListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerStmService);
        syncListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSyncLBVerifyRestAdapterCalledPendingDelete() throws Exception {

        lb.setStatus(LoadBalancerStatus.PENDING_DELETE);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());
        when(objectMessage.getObject()).thenReturn(mdc);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        syncListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteLoadBalancer(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.DELETED);
    }

    @Test
    public void testSyncLBVerifyRestAdapterCalledError() throws Exception {

        lb.setStatus(LoadBalancerStatus.ERROR);

        MessageDataContainer mdc = new MessageDataContainer();
        mdc.setLoadBalancerId(lb.getId());
        mdc.setAccountId(lb.getAccountId());
        mdc.setLoadBalancerStatus(lb.getStatus());
        when(objectMessage.getObject()).thenReturn(mdc);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        syncListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    // TODO: more tests...
}