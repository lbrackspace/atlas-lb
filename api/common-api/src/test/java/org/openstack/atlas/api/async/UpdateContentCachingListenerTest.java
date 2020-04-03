package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
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

public class UpdateContentCachingListenerTest extends STMTestBase {
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
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private RestApiConfiguration config;

    private UpdateContentCachingListener updateContentCachingListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        updateContentCachingListener = new UpdateContentCachingListener();
        updateContentCachingListener.setLoadBalancerService(loadBalancerService);
        updateContentCachingListener.setNotificationService(notificationService);
        updateContentCachingListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        updateContentCachingListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateLoadBalancerWithCachingTrue() throws Exception {
        lb.setContentCaching(true);
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateContentCachingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateLoadBalancer(lb, lb);
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_CONTENT_CACHING), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateLoadBalancerWithCachingFalse() throws Exception {
        lb.setContentCaching(false);
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateContentCachingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateLoadBalancer(lb, lb);
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_CONTENT_CACHING), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        updateContentCachingListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_CONTENT_CACHING), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidCache() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).updateLoadBalancer(lb, lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateContentCachingListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_CONTENT_CACHING), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }
}
