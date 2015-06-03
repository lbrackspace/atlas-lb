package org.openstack.atlas.api.async;

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
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class AddVirtualIpListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private List<Integer> newVipIds = new ArrayList<Integer>();
    private Integer VIP_ID = 15;

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
    private UsageEventCollection usageEventCollection;
    @Mock
    private RestApiConfiguration config;

    private AddVirtualIpListener addVirtualIpListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        newVipIds.add(VIP_ID);
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        addVirtualIpListener = new AddVirtualIpListener();
        addVirtualIpListener.setLoadBalancerService(loadBalancerService);
        addVirtualIpListener.setNotificationService(notificationService);
        addVirtualIpListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        addVirtualIpListener.setUsageEventCollection(usageEventCollection);
        addVirtualIpListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddVirtualIp() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getNewVipIds()).thenReturn(newVipIds);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        addVirtualIpListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).addVirtualIps(LOAD_BALANCER_ID, ACCOUNT_ID, lb);
        //TODO: Verify usage now that its been updated...
//        verify(usageEventHelper).processUsageEvent(eq(lb), eq(UsageEvent.CREATE_VIRTUAL_IP), Matchers.any(Calendar.class));
        verify(usageEventCollection).collectUsageAndProcessUsageRecords(eq(lb), eq(UsageEvent.CREATE_VIRTUAL_IP), Matchers.any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(VIP_ID), anyString(), anyString(), eq(EventType.CREATE_VIRTUAL_IP), eq(CategoryType.CREATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getNewVipIds()).thenReturn(newVipIds);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        addVirtualIpListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(VIP_ID), anyString(), anyString(), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.CREATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testAddInvalidVirtualIp() throws Exception {
        Exception exception = new Exception();
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getNewVipIds()).thenReturn(newVipIds);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).addVirtualIps(LOAD_BALANCER_ID, ACCOUNT_ID, lb);

        addVirtualIpListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).addVirtualIps(LOAD_BALANCER_ID, ACCOUNT_ID, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(VIP_ID), anyString(), anyString(), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.CREATE), eq(EventSeverity.CRITICAL));
    }
}
