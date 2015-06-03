package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.SslTerminationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteLoadBalancerListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private List<SnmpUsage> usages;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private SslTerminationService sslTerminationService;
    @Mock
    private RestApiConfiguration config;

    private DeleteLoadBalancerListener deleteLoadBalancerListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        usages = new ArrayList<SnmpUsage>();

        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);

        deleteLoadBalancerListener = new DeleteLoadBalancerListener();
        deleteLoadBalancerListener.setLoadBalancerService(loadBalancerService);
        deleteLoadBalancerListener.setNotificationService(notificationService);
        deleteLoadBalancerListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        deleteLoadBalancerListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteLoadBalancerListener.setUsageEventCollection(usageEventCollection);
        deleteLoadBalancerListener.setSslTerminationService(sslTerminationService);
        deleteLoadBalancerListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteValidLoadBalancerWithoutSSL() throws Exception {
        lb.setSslTermination(null);
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.pseudoDelete(lb)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteLoadBalancerListener.doOnMessage(objectMessage);

        verify(usageEventCollection).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).deleteLoadBalancer(lb);
        verify(loadBalancerService).pseudoDelete(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.DELETED);
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_LOADBALANCER), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
    }

    @Test
    public void testDeleteValidLoadBalancerWithSSL() throws Exception {
        lb.setSslTermination(new SslTermination());
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.pseudoDelete(lb)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteLoadBalancerListener.doOnMessage(objectMessage);

        verify(usageEventCollection).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).deleteLoadBalancer(lb);
        verify(sslTerminationService).deleteSslTermination(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(loadBalancerService).pseudoDelete(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.DELETED);
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_LOADBALANCER), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception { //This is named oddly for this specific test, but left it alone for consistency
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        deleteLoadBalancerListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_LOADBALANCER), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidLoadBalancer() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).deleteLoadBalancer(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteLoadBalancerListener.doOnMessage(objectMessage);

        verify(usageEventCollection).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).deleteLoadBalancer(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_LOADBALANCER), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }
}
