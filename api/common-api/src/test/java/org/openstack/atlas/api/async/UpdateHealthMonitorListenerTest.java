package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.VTMTestBase;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.HealthMonitor;
import org.openstack.atlas.service.domain.entities.HealthMonitorType;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class UpdateHealthMonitorListenerTest extends VTMTestBase {

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
    private HealthMonitor healthMonitor;
    @Mock
    private RestApiConfiguration config;

    private UpdateHealthMonitorListener updateHealthMonitorListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        setupHealthMonitor();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setHealthMonitor(healthMonitor);
        updateHealthMonitorListener = new UpdateHealthMonitorListener();
        updateHealthMonitorListener.setLoadBalancerService(loadBalancerService);
        updateHealthMonitorListener.setNotificationService(notificationService);
        updateHealthMonitorListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        updateHealthMonitorListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    private void setupHealthMonitor() {
        when(healthMonitor.getId()).thenReturn(15);
        when(healthMonitor.getType()).thenReturn(HealthMonitorType.CONNECT);
        when(healthMonitor.getDelay()).thenReturn(10);
        when(healthMonitor.getTimeout()).thenReturn(20);
        when(healthMonitor.getAttemptsBeforeDeactivation()).thenReturn(25);
        when(healthMonitor.getPath()).thenReturn("SOME_PATH");
        when(healthMonitor.getStatusRegex()).thenReturn("SOME_STATUS_REGEX");
        when(healthMonitor.getBodyRegex()).thenReturn("SOME_BODY_REGEX");
    }

    @Test
    public void testUpdateLoadBalancerWithValidMonitor() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateHealthMonitorListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateHealthMonitor(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveHealthMonitorEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, healthMonitor.getId(), EntryHelper.UPDATE_MONITOR_TITLE, EntryHelper.createHealthMonitorSummary(lb), EventType.UPDATE_HEALTH_MONITOR, CategoryType.UPDATE, EventSeverity.INFO);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        updateHealthMonitorListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveHealthMonitorEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.UPDATE_HEALTH_MONITOR), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidMonitor() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).updateHealthMonitor(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateHealthMonitorListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateHealthMonitor(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveHealthMonitorEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.UPDATE_HEALTH_MONITOR), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

}
