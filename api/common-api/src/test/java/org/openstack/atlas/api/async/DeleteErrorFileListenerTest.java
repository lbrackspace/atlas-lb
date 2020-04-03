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
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteErrorFileListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private MessageDataContainer messageDataContainer;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private RestApiConfiguration config;

    private DeleteErrorFileListener deleteErrorFileListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        deleteErrorFileListener = new DeleteErrorFileListener();
        deleteErrorFileListener.setLoadBalancerService(loadBalancerService);
        deleteErrorFileListener.setNotificationService(notificationService);
        deleteErrorFileListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        deleteErrorFileListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteErrorFileListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteErrorFile() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).deleteErrorFile(lb, null);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), eq(String.format("Load balancer '%d' not found in database.", lb.getId())));
    }

    @Test
    public void testUpdateInvalidLoadBalancerInput() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(null);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(null);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService, never()).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), eq(String.format("Load balancer '%d' not found in database.", lb.getId())));
        verify(reverseProxyLoadBalancerVTMService, never()).deleteErrorFile(lb, null);
        verify(loadBalancerService, never()).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService, never()).setStatus(lb, LoadBalancerStatus.ERROR);
    }

    @Test
    public void testDeleteInvalidErrorFile() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).deleteErrorFile(lb, null);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).deleteErrorFile(lb, null);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), eq(String.format("Error setting Errorfile for %d_%d", lb.getAccountId(), lb.getId())));
    }
}
