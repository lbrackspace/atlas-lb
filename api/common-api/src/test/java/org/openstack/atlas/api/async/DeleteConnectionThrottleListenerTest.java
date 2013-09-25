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
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.ConnectionThrottleService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteConnectionThrottleListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer CONNECTION_LIMIT_ID = 20;

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
    private ConnectionThrottleService connectionThrottleService;
    @Mock
    private RestApiConfiguration config;

    private DeleteConnectionThrottleListener deleteConnectionThrottleListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        ConnectionLimit connectionLimit = new ConnectionLimit();
        connectionLimit.setId(CONNECTION_LIMIT_ID);
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setConnectionLimit(connectionLimit);
        deleteConnectionThrottleListener = new DeleteConnectionThrottleListener();
        deleteConnectionThrottleListener.setLoadBalancerService(loadBalancerService);
        deleteConnectionThrottleListener.setNotificationService(notificationService);
        deleteConnectionThrottleListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        deleteConnectionThrottleListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteConnectionThrottleListener.setConnectionThrottleService(connectionThrottleService);
        deleteConnectionThrottleListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteThrottle() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteConnectionThrottleListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteConnectionThrottle(lb);
        verify(connectionThrottleService).delete(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveConnectionLimitEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(CONNECTION_LIMIT_ID), anyString(), anyString(), eq(EventType.DELETE_CONNECTION_THROTTLE), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        deleteConnectionThrottleListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveConnectionLimitEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_CONNECTION_THROTTLE), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidThrottle() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).deleteConnectionThrottle(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteConnectionThrottleListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteConnectionThrottle(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveConnectionLimitEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(CONNECTION_LIMIT_ID), anyString(), anyString(), eq(EventType.DELETE_CONNECTION_THROTTLE), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }
}
