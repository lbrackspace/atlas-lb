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
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.AccessListService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

public class DeleteAccessListListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer ACCESS_LIST_ID = 15;
    private Set<AccessList> accessLists;
    private List<Integer> deletionList;

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
    private AccessListService accessListService;
    @Mock
    private RestApiConfiguration config;

    private DeleteAccessListListener deleteAccessListListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        accessLists = new HashSet<AccessList>();
        deletionList = new ArrayList<Integer>();
        AccessList accessList = setupAccessList();
        accessLists.add(accessList);
        deletionList.add(accessList.getId());
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setAccessLists(accessLists);
        deleteAccessListListener = new DeleteAccessListListener();
        deleteAccessListListener.setLoadBalancerService(loadBalancerService);
        deleteAccessListListener.setNotificationService(notificationService);
        deleteAccessListListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        deleteAccessListListener.setLoadBalancerStatusHistoryService(loadBalancerStatusHistoryService);
        deleteAccessListListener.setAccessListService(accessListService);
        deleteAccessListListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    private AccessList setupAccessList() {
        AccessList accessList = mock(AccessList.class);

        when(accessList.getId()).thenReturn(ACCESS_LIST_ID);
        // Could set up more of this class, but not sure if it matters.

        return accessList;
    }

    @Test
    public void testDeleteAccessListItem() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");


        deleteAccessListListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteAccessList(lb, deletionList);
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(ACCESS_LIST_ID), anyString(), anyString(), eq(EventType.DELETE_ACCESS_LIST), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService).update(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testDeleteAccessList() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteAccessListListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteAccessList(lb, deletionList);
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(ACCESS_LIST_ID), anyString(), anyString(), eq(EventType.DELETE_ACCESS_LIST), eq(CategoryType.DELETE), eq(EventSeverity.INFO));
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ACTIVE);
        verify(loadBalancerService).update(lb);
        verify(loadBalancerStatusHistoryService).save(ACCOUNT_ID, LOAD_BALANCER_ID, LoadBalancerStatus.ACTIVE);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        deleteAccessListListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_ACCESS_LIST), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidAccessList() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        doThrow(exception).when(reverseProxyLoadBalancerStmService).deleteAccessList(eq(lb), anyList());

        deleteAccessListListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).deleteAccessList(eq(lb), anyList());
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.DELETE_ACCESS_LIST), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

}
