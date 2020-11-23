package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openstack.atlas.api.async.util.VTMTestBase;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.AccessListService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.ObjectMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class UpdateAccessListListenerTest extends VTMTestBase {

    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer ACCESS_LIST_ID = 15;
    private Set<AccessList> accessLists;
    private AccessList accessList;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private AccessListService accessListService;
    @Mock
    private RestApiConfiguration config;
    @Mock
    private LoadBalancerEventRepository loadBalancerEventRepository;
    @Captor
    private ArgumentCaptor<Integer> valueCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;


    private UpdateAccessListListener updateAccessListListener;


    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        accessLists = new HashSet<AccessList>();
        AccessList accessList = setupAccessList();
        accessLists.add(accessList);
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setAccessLists(accessLists);
        updateAccessListListener = new UpdateAccessListListener();
        updateAccessListListener.setLoadBalancerService(loadBalancerService);
        updateAccessListListener.setNotificationService(notificationService);
        updateAccessListListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        updateAccessListListener.setAccessListService(accessListService);
        updateAccessListListener.setConfiguration(config);

    }

    @After
    public void tearDown() {
    }

    private AccessList setupAccessList() {
        accessList = mock(AccessList.class);

        when(accessList.getId()).thenReturn(ACCESS_LIST_ID);
        when(accessList.getIpAddress()).thenReturn("206.160.163.21");
        when(accessList.getType()).thenReturn(AccessListType.ALLOW);

        // Could set up more of this class, but not sure if it matters.

        return accessList;
    }

    @Test
    public void testUpdateLoadBalancerWithValidAccessList() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(accessListService.diffRequestAccessListWithDomainAccessList(lb, lb)).thenReturn(accessLists);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateAccessListListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateAccessList(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveAccessListEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, ACCESS_LIST_ID, EntryHelper.UPDATE_ACCESS_LIST_TITLE, EntryHelper.createAccessListSummary(accessList), EventType.UPDATE_ACCESS_LIST, CategoryType.UPDATE, EventSeverity.INFO);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        doThrow(EntityNotFoundException.class).when(loadBalancerService).getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID);

        updateAccessListListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.UPDATE_ACCESS_LIST), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidAccessList() throws Exception {
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).updateAccessList(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateAccessListListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerVTMService).updateAccessList(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyInt(), anyString(), anyString(), eq(EventType.UPDATE_ACCESS_LIST), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateAccessListListenerEvent() throws Exception {


        AccessList al = new AccessList();
        al.setType(AccessListType.ALLOW);
        al.setId(20);
        al.setIpAddress("206.160.163.21");
        AccessList al2 = new AccessList();
        al2.setType(AccessListType.ALLOW);
        al2.setId(21);
        al.setIpAddress("206.160.163.22");
        HashSet<AccessList> newAccessLists = new HashSet<AccessList>();

        newAccessLists.add(al);
        newAccessLists.add(al2);

        lb.setAccessLists(newAccessLists);

        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");


        updateAccessListListener.doOnMessage(objectMessage);

        verify(notificationService, times(2)).saveAccessListEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), valueCaptor.capture(), anyString(), anyString(), eq(EventType.UPDATE_ACCESS_LIST), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));

        List<Integer> accessListEventIds = valueCaptor.getAllValues();

        Assert.assertEquals(2, accessListEventIds.size());
        Assert.assertTrue(accessListEventIds.contains(al.getId()));
        Assert.assertTrue(accessListEventIds.contains(al2.getId()));

        verify(accessListService, never()).diffRequestAccessListWithDomainAccessList(lb, lb);

    }

}
