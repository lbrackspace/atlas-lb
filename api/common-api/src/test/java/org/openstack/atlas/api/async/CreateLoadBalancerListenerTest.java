package org.openstack.atlas.api.async;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CreateLoadBalancerListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private String LOAD_BALANCER_NAME = "SOME NAME";
    private Integer HEALTH_MONITOR_ID = 15;
    private HealthMonitor healthMonitor;
    private Integer ACCESS_LIST_ID = 20;
    private AccessList accessList;
    private Integer CONNECTION_LIMIT_ID = 25;
    private ConnectionLimit connectionLimit;
    private Integer VIRTUAL_IP4_ID = 30;
    private LoadBalancerJoinVip virtualIp4;
    private Integer VIRTUAL_IP6_ID = 35;
    private LoadBalancerJoinVip6 virtualIp6;
    private Integer NODE_ID = 40;
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
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private RestApiConfiguration config;

    private CreateLoadBalancerListener createLoadBalancerListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();

        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setName(LOAD_BALANCER_NAME);
        lb.setAlgorithm(LoadBalancerAlgorithm.ROUND_ROBIN);
        lb.setPort(80);
        lb.setProtocol(LoadBalancerProtocol.HTTP);

        lb.setHealthMonitor(setupHealthMonitor());
        lb.setAccessLists(setupAccessList());
        lb.setNodes(setupNodes());
        lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
        lb.setLoadBalancerJoinVipSet(setupVip4());
        lb.setLoadBalancerJoinVip6Set(setupVip6());
        lb.setConnectionLimit(setupConnectionLimit());
        lb.setConnectionLogging(true);

        createLoadBalancerListener = new CreateLoadBalancerListener();
        createLoadBalancerListener.setLoadBalancerService(loadBalancerService);
        createLoadBalancerListener.setNotificationService(notificationService);
        createLoadBalancerListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        createLoadBalancerListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        createLoadBalancerListener.setUsageEventCollection(usageEventCollection);
        createLoadBalancerListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    // I started doing this with mocks, but it turns out it's easier to just use a real object
    private HealthMonitor setupHealthMonitor() {
        healthMonitor = mock(HealthMonitor.class);
        when(healthMonitor.getId()).thenReturn(HEALTH_MONITOR_ID);
        when(healthMonitor.getType()).thenReturn(HealthMonitorType.CONNECT);
        when(healthMonitor.getDelay()).thenReturn(10);
        when(healthMonitor.getTimeout()).thenReturn(20);
        when(healthMonitor.getAttemptsBeforeDeactivation()).thenReturn(25);
        when(healthMonitor.getPath()).thenReturn("SOME_PATH");
        when(healthMonitor.getStatusRegex()).thenReturn("SOME_STATUS_REGEX");
        when(healthMonitor.getBodyRegex()).thenReturn("SOME_BODY_REGEX");

        return healthMonitor;
    }

    private Set<AccessList> setupAccessList() {
        Set<AccessList> accessLists = new HashSet<AccessList>();
        accessList = mock(AccessList.class);

        when(accessList.getId()).thenReturn(ACCESS_LIST_ID);
        // Could set up more of this class, but not sure if it matters.

        accessLists.add(accessList);
        return accessLists;
    }

    private Set<Node> setupNodes() {
        Set<Node> nodes = new HashSet<Node>();
        node = new Node();
        node.setId(NODE_ID);

        nodes.add(node);
        return nodes;
    }

    private Set<LoadBalancerJoinVip> setupVip4() {
        Set<LoadBalancerJoinVip> joinVips = new HashSet<LoadBalancerJoinVip>();
        VirtualIp virtualIp = new VirtualIp();
        virtualIp.setIpVersion(IpVersion.IPV4);
        virtualIp.setId(VIRTUAL_IP4_ID);
        virtualIp4 = new LoadBalancerJoinVip();
        virtualIp4.setId(new LoadBalancerJoinVip.Id(LOAD_BALANCER_ID, VIRTUAL_IP4_ID));
        virtualIp4.setLoadBalancer(lb);
        virtualIp4.setPort(80);
        virtualIp4.setVirtualIp(virtualIp);
        joinVips.add(virtualIp4);
        return joinVips;
    }

    private Set<LoadBalancerJoinVip6> setupVip6() {
        Set<LoadBalancerJoinVip6> joinVips = new HashSet<LoadBalancerJoinVip6>();
        Cluster cluster = new Cluster();
        cluster.setClusterIpv6Cidr("0:0:0:0:0:0");
        VirtualIpv6 virtualIp = new VirtualIpv6();
        virtualIp.setId(VIRTUAL_IP6_ID);
        virtualIp.setCluster(cluster);
        virtualIp6 = new LoadBalancerJoinVip6();
        virtualIp6.setId(new LoadBalancerJoinVip6.Id(LOAD_BALANCER_ID, VIRTUAL_IP6_ID));
        virtualIp6.setLoadBalancer(lb);
        virtualIp6.setPort(80);
        virtualIp6.setVirtualIp(virtualIp);
        joinVips.add(virtualIp6);
        return joinVips;
    }

    private ConnectionLimit setupConnectionLimit() {
        connectionLimit = new ConnectionLimit();
        connectionLimit.setId(CONNECTION_LIMIT_ID);

        return connectionLimit;
    }

    private Boolean checkNodeHelper(Set<Node> nodes, NodeStatus status) {
        Boolean good = true;
        for (Node node : nodes) {
            if (node.getCondition() == NodeCondition.DISABLED || node.getCondition() == NodeCondition.DRAINING) {
                if (node.getStatus() != NodeStatus.OFFLINE) good = false;
            } else {
                if (node.getStatus() != status) good = false;
            }
        }
        return good;
    }

    @Test
    public void testCreateValidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(loadBalancerService.update(lb)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        createLoadBalancerListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).createLoadBalancer(lb);
        verify(usageEventCollection).processZeroUsageEvent(eq(lb), eq(UsageEvent.CREATE_LOADBALANCER), Matchers.any(Calendar.class));
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ACTIVE);
        Assert.assertTrue(checkNodeHelper(lb.getNodes(), NodeStatus.ONLINE)); // This is how I decided to test NodesHelper
        verify(loadBalancerService).update(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ACTIVE);

        //Atom Entries
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.CREATE_LOADBALANCER), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
        verify(notificationService).saveNodeEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(NODE_ID), anyString(), anyString(), eq(EventType.CREATE_NODE), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(VIRTUAL_IP4_ID), anyString(), anyString(), eq(EventType.CREATE_VIRTUAL_IP), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(VIRTUAL_IP6_ID), anyString(), anyString(), eq(EventType.CREATE_VIRTUAL_IP), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
        verify(notificationService).saveHealthMonitorEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(HEALTH_MONITOR_ID), anyString(), anyString(), eq(EventType.UPDATE_HEALTH_MONITOR), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(notificationService).saveSessionPersistenceEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_SESSION_PERSISTENCE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_CONNECTION_LOGGING), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(notificationService).saveConnectionLimitEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(CONNECTION_LIMIT_ID), anyString(), anyString(), eq(EventType.UPDATE_CONNECTION_THROTTLE), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(ACCESS_LIST_ID), anyString(), anyString(), eq(EventType.UPDATE_ACCESS_LIST), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));

        //TODO: Verify usage now that its been updated...
//        verify(usageEventHelper).processUsageEvent(eq(lb), eq(UsageEvent.CREATE_LOADBALANCER), eq(0l), eq(0l), eq(0), eq(0l), eq(0l), eq(0), Matchers.any(Calendar.class));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception { //This is named oddly for this specific test, but left it alone for consistency
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        createLoadBalancerListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.CREATE_LOADBALANCER), eq(CategoryType.CREATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testCreateInvalidLoadBalancer() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).createLoadBalancer(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        createLoadBalancerListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).createLoadBalancer(lb);
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ERROR);
        Assert.assertTrue(checkNodeHelper(lb.getNodes(), NodeStatus.OFFLINE)); // This is how I decided to test NodesHelper
        verify(loadBalancerService).update(lb);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ERROR);
        //TODO: Verify usage now that its been updated...
//        verify(usageEventHelper).processUsageEvent(eq(lb), eq(UsageEvent.CREATE_LOADBALANCER), eq(0l), eq(0l), eq(0), eq(0l), eq(0l), eq(0), Matchers.any(Calendar.class));
        verify(usageEventCollection).processZeroUsageEvent(eq(lb), eq(UsageEvent.CREATE_LOADBALANCER), Matchers.any(Calendar.class));
    }
}
