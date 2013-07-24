package org.openstack.atlas.api.async;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CreateNodesListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer NODE_ID = 15;
    private Node node;

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

    private CreateNodesListener createNodesListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        Set<Node> nodes = new HashSet<Node>();
        node = new Node();
        node.setId(NODE_ID);
        node.setIpAddress("192.168.1.1");
        node.setPort(80);
        nodes.add(node);
        lb.setNodes(nodes);
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        createNodesListener = new CreateNodesListener();
        createNodesListener.setLoadBalancerService(loadBalancerService);
        createNodesListener.setNotificationService(notificationService);
        createNodesListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        createNodesListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
    }

    @Test
    public void testCreateNodesWithValidNodes() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        createNodesListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).setNodes(lb);
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService).update(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), eq(EntryHelper.createNodeSummary(node)), eq(EventType.CREATE_NODE), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        createNodesListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.CREATE_NODE), eq(CategoryType.CREATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testCreateNodesWithInvalidNodes() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).setNodes(lb);

        createNodesListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).setNodes(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.CREATE_NODE), eq(CategoryType.CREATE), eq(EventSeverity.CRITICAL));
    }
}
