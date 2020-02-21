package org.openstack.atlas.api.async;

import org.junit.Assert;
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
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteNodesListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private List<Integer> doomedNodeIds = new ArrayList<Integer>();
    private List<Node> doomedNodes = new ArrayList<Node>();

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
    private NodeService nodeService;
    @Mock
    private RestApiConfiguration config;

    private DeleteNodesListener deleteNodesListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();

        Node node = new Node();
        node.setId(10);
        doomedNodeIds.add(10);
        doomedNodes.add(node);
        node = new Node();
        node.setId(15);
        doomedNodeIds.add(15);
        doomedNodes.add(node);

        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        deleteNodesListener = new DeleteNodesListener();
        deleteNodesListener.setLoadBalancerService(loadBalancerService);
        deleteNodesListener.setNotificationService(notificationService);
        deleteNodesListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        deleteNodesListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteNodesListener.setNodeService(nodeService);
        deleteNodesListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteNodes() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(doomedNodeIds);
        when(nodeService.getNodesByIds(doomedNodeIds)).thenReturn(doomedNodes);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteNodesListener.doOnMessage(objectMessage);

        verify(loadBalancerService).getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(loadBalancerService).get(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(messageDataContainer).getIds();
        verify(nodeService).getNodesByIds(doomedNodeIds);
        verify(reverseProxyLoadBalancerVTMService).removeNodes(lb, doomedNodes);
        verify(nodeService).delNodes(lb, doomedNodes);
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService).update(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ACTIVE);
        verify(notificationService, times(2)).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_NODE), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        deleteNodesListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_NODE), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidNodes() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(doomedNodeIds);
        when(nodeService.getNodesByIds(doomedNodeIds)).thenReturn(doomedNodes);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).removeNodes(lb, doomedNodes);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteNodesListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).removeNodes(lb, doomedNodes);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService, times(2)).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_NODE), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }
}
