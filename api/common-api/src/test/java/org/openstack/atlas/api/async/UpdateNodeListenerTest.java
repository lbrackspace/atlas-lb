package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class UpdateNodeListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer NODE_ID = 15;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;
    @Mock
    private Node nodeToUpdate;

    private UpdateNodeListener updateNodeListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        Set<Node> nodes = new HashSet<Node>();
        when(nodeToUpdate.getId()).thenReturn(NODE_ID);
        when(nodeToUpdate.isToBeUpdated()).thenReturn(true);
        nodes.add(nodeToUpdate);
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setNodes(nodes);
        updateNodeListener = new UpdateNodeListener();
        updateNodeListener.setLoadBalancerService(loadBalancerService);
        updateNodeListener.setNotificationService(notificationService);
        updateNodeListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
    }

    @After
    public void tearDown() {
        stmClient.destroy();
    }

    @Test
    public void testUpdateLoadBalancerWithValidNode() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        updateNodeListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).setNodes(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        updateNodeListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidNode() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).setNodes(lb);

        updateNodeListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).setNodes(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

}
