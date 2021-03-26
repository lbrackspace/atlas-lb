package org.openstack.atlas.api.mgmt.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.UpdateNodeListener;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.api.mgmt.async.util.VTMTestBase;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class MgmtUpdateNodeListenerTest extends VTMTestBase {

    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer NODE_ID = 15;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    EsbRequest esbRequest;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private Node nodeToUpdate;
    @Mock
    private RestApiConfiguration config;

    private MgmtUpdateNodeListener updateNodeListener;

    @Before
    public void standUp() throws JMSException {
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
        updateNodeListener = new MgmtUpdateNodeListener();
        updateNodeListener.setLoadBalancerService(loadBalancerService);
        updateNodeListener.setNotificationService(notificationService);
        updateNodeListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        updateNodeListener.setConfiguration(config);
        when(objectMessage.getObject()).thenReturn(esbRequest);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateLoadBalancerWithValidNode() throws Exception {
        when(esbRequest.getLoadBalancer()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateNodeListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).setNodes(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(esbRequest.getLoadBalancer()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        updateNodeListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidNode() throws Exception {
        when(esbRequest.getLoadBalancer()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).setNodes(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateNodeListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).setNodes(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.UPDATE_NODE), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }


}
