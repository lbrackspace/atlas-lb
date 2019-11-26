package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteVirtualIpsListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private List<Integer> vipIdsToDelete = new ArrayList<Integer>();

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
    @Mock
    private VirtualIpService virtualIpService;
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private RestApiConfiguration config;

    private DeleteVirtualIpsListener deleteVirtualIpsListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        vipIdsToDelete.add(10);
        deleteVirtualIpsListener = new DeleteVirtualIpsListener();
        deleteVirtualIpsListener.setLoadBalancerService(loadBalancerService);
        deleteVirtualIpsListener.setNotificationService(notificationService);
        deleteVirtualIpsListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        deleteVirtualIpsListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteVirtualIpsListener.setVirtualIpService(virtualIpService);
        deleteVirtualIpsListener.setUsageEventCollection(usageEventCollection);
        deleteVirtualIpsListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteVirtualIps() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(vipIdsToDelete);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteVirtualIpsListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteVirtualIps(lb, vipIdsToDelete, null);
        verify(virtualIpService).removeVipsFromLoadBalancer(lb, vipIdsToDelete);
        //TODO: Verify usage now that its been updated...
//        verify(usageEventHelper).processUsageEvent(eq(lb), eq(UsageEvent.DELETE_VIRTUAL_IP), Matchers.any(Calendar.class));
        verify(usageEventCollection).collectUsageAndProcessUsageRecords(eq(lb), eq(UsageEvent.DELETE_VIRTUAL_IP), Matchers.any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_VIRTUAL_IP), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(vipIdsToDelete);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        deleteVirtualIpsListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), eq(String.format("Load balancer '%d' not found in database.", lb.getId())));
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_VIRTUAL_IP), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidZeusVirtualIps() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(vipIdsToDelete);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerStmService).deleteVirtualIps(lb, vipIdsToDelete, null);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteVirtualIpsListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteVirtualIps(lb, vipIdsToDelete, null);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), eq(String.format("Error deleting virtual ips in Zeus for loadbalancer '%d'.", lb.getId())));
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_VIRTUAL_IP), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidDatabaseVirtualIps() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getIds()).thenReturn(vipIdsToDelete);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(virtualIpService).removeVipsFromLoadBalancer(lb, vipIdsToDelete);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteVirtualIpsListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteVirtualIps(lb, vipIdsToDelete, null);
        verify(virtualIpService).removeVipsFromLoadBalancer(lb, vipIdsToDelete);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.DATABASE_FAILURE.name()), eq(String.format("Error deleting virtual ips in database for loadbalancer '%d'.", lb.getId())));
        verify(notificationService).saveVirtualIpEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_VIRTUAL_IP), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ERROR);
    }
}
