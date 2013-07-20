package org.openstack.atlas.api.async;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
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

public class UpdateErrorFileListenerTest extends STMTestBase {

    private final String USERNAME = "SOME_USER_NAME";
    private final String ERROR_FILE_CONTENT = "SOME ERROR FILE CONTENT";
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private final Integer CLUSTER_ID = 15;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private MessageDataContainer messageDataContainer;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;
    @Mock
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;

    private UpdateErrorFileListener updateErrorFileListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        updateErrorFileListener = new UpdateErrorFileListener();
        updateErrorFileListener.setLoadBalancerService(loadBalancerService);
        updateErrorFileListener.setNotificationService(notificationService);
        updateErrorFileListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        updateErrorFileListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
    }

    @Test
    public void testUpdateLoadBalancerWithValidErrorFile() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getClusterId()).thenReturn(null);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getErrorFileContents()).thenReturn(ERROR_FILE_CONTENT);

        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        Assert.assertNull(lb.getUserName());

        updateErrorFileListener.doOnMessage(objectMessage);

        Assert.assertEquals(USERNAME, lb.getUserName());
        verify(reverseProxyLoadBalancerStmService).setErrorFile(lb, ERROR_FILE_CONTENT);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidLBErrorFile() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getClusterId()).thenReturn(null);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getErrorFileContents()).thenReturn(ERROR_FILE_CONTENT);

        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        Assert.assertNull(lb.getUserName());

        updateErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), anyString(), anyString());
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ERROR);
    }

    @Test
    public void testUpdateLoadBalancerWithFailureToSetErrorFile() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getClusterId()).thenReturn(null);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getErrorFileContents()).thenReturn(ERROR_FILE_CONTENT);

        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).setErrorFile(lb, ERROR_FILE_CONTENT);

        Assert.assertNull(lb.getUserName());

        updateErrorFileListener.doOnMessage(objectMessage);

        Assert.assertEquals(USERNAME, lb.getUserName());
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateDefaultErrorFileWithValidCluster() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(null);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(null);
        when(messageDataContainer.getClusterId()).thenReturn(CLUSTER_ID);
        when(messageDataContainer.getUserName()).thenReturn(null);
        when(messageDataContainer.getErrorFileContents()).thenReturn(ERROR_FILE_CONTENT);

        updateErrorFileListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).uploadDefaultErrorFile(CLUSTER_ID, ERROR_FILE_CONTENT);
    }

    @Test
    public void testUpdateDefaultErrorFileWithInvalidCluster() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(null);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(null);
        when(messageDataContainer.getClusterId()).thenReturn(CLUSTER_ID);
        when(messageDataContainer.getUserName()).thenReturn(null);
        when(messageDataContainer.getErrorFileContents()).thenReturn(ERROR_FILE_CONTENT);

        doThrow(exception).when(reverseProxyLoadBalancerStmService).uploadDefaultErrorFile(CLUSTER_ID, ERROR_FILE_CONTENT);

        updateErrorFileListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(anyInt(), anyInt(), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(reverseProxyLoadBalancerStmService).uploadDefaultErrorFile(CLUSTER_ID, ERROR_FILE_CONTENT);
    }

}
