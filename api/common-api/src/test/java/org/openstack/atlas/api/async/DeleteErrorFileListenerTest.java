package org.openstack.atlas.api.async;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
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
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

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
        deleteErrorFileListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        deleteErrorFileListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
    }

    @Test
    public void testDeleteErrorFile() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteErrorFile(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
    }

    @Test
    public void testUpdateInvalidLoadBalancerInput() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(null);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(null);

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService, never()).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(reverseProxyLoadBalancerStmService, never()).deleteErrorFile(lb);
        verify(loadBalancerService, never()).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService, never()).setStatus(lb, LoadBalancerStatus.ERROR);
    }

    @Test
    public void testDeleteInvalidErrorFile() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).deleteErrorFile(lb);

        deleteErrorFileListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteErrorFile(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
    }
}
