package org.openstack.atlas.api.async;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class UpdateSessionPersistenceListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;

    private UpdateSessionPersistenceListener updateSessionPersistenceListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        updateSessionPersistenceListener = new UpdateSessionPersistenceListener();
        updateSessionPersistenceListener.setLoadBalancerService(loadBalancerService);
        updateSessionPersistenceListener.setNotificationService(notificationService);
        updateSessionPersistenceListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
    }

    @Test
    public void testUpdateLoadBalancerWithValidSessionPersistence() throws Exception {
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        updateSessionPersistenceListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveSessionPersistenceEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(EntryHelper.UPDATE_PERSISTENCE_TITLE), anyString(), eq(EventType.UPDATE_SESSION_PERSISTENCE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateLoadBalancerWithValidNoSessionPersistence() throws Exception {
        lb.setSessionPersistence(SessionPersistence.NONE);
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        updateSessionPersistenceListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService, never()).saveSessionPersistenceEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(EntryHelper.UPDATE_PERSISTENCE_TITLE), anyString(), eq(EventType.UPDATE_SESSION_PERSISTENCE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        updateSessionPersistenceListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveSessionPersistenceEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_SESSION_PERSISTENCE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidNode() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);

        updateSessionPersistenceListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveSessionPersistenceEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_SESSION_PERSISTENCE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

}
