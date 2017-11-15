package org.openstack.atlas.api.async;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.atom.EntryHelper;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Ignore
//TODO: Need to get this running again. The methods inside the listener are private and should be re-worked
public class UpdateSslTerminationListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private Integer SSL_TERMINATION_ID = 15;
    private List<SnmpUsage> usages;

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
    private ZeusSslTermination queTermination;
    @Mock
    private SslTermination sslTermination;
    @Mock
    private RestApiConfiguration config;

    private UpdateSslTerminationListener updateSslTerminationListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        usages = new ArrayList<SnmpUsage>();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        updateSslTerminationListener = new UpdateSslTerminationListener();
        updateSslTerminationListener.setLoadBalancerService(loadBalancerService);
        updateSslTerminationListener.setNotificationService(notificationService);
        updateSslTerminationListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
        updateSslTerminationListener.setUsageEventCollection(usageEventCollection);
        updateSslTerminationListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUpdateLoadBalancerWithValidSslTerminationEnabledSecureOnly() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getZeusSslTermination()).thenReturn(queTermination);
        when(sslTermination.getEnabled()).thenReturn(true);
        when(sslTermination.getSecureTrafficOnly()).thenReturn(true);
        when(sslTermination.getSecurePort()).thenReturn(80);
        when(sslTermination.getId()).thenReturn(SSL_TERMINATION_ID);
        when(queTermination.getSslTermination()).thenReturn(sslTermination);
        lb.setSslTermination(sslTermination);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        //TODO: method is private and cannot get around this until the listener is updated...sy
//        when(updateSslTerminationListener.getUsagesToInsert(Matchers.anyInt(), Matchers.<SslTermination>any(),  Matchers.<SslTermination>any(), Matchers.<Map>any(), Matchers.<Map>any())).thenReturn(usages);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).get(LOAD_BALANCER_ID, ACCOUNT_ID);
        Assert.assertEquals(lb.getUserName(), USERNAME);
        verify(usageEventCollection, times(2)).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).updateSslTermination(lb, queTermination);
        //TODO: Update for new usage behaviour...
//        verify(usageEventCollection).processUsageEvent(eq(usages), eq(lb), eq(UsageEvent.SSL_ONLY_ON), any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveSslTerminationEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, SSL_TERMINATION_ID, EntryHelper.UPDATE_SSL_TERMINATION_TITLE, EntryHelper.createSslTerminationSummary(sslTermination), EventType.UPDATE_SSL_TERMINATION, CategoryType.UPDATE, EventSeverity.INFO);
    }

    @Test
    public void testUpdateLoadBalancerWithValidSslTerminationEnabledMixed() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getZeusSslTermination()).thenReturn(queTermination);
        when(sslTermination.getEnabled()).thenReturn(true);
        when(sslTermination.getSecureTrafficOnly()).thenReturn(false);
        when(sslTermination.getSecurePort()).thenReturn(80);
        when(sslTermination.getId()).thenReturn(SSL_TERMINATION_ID);
        when(queTermination.getSslTermination()).thenReturn(sslTermination);
        lb.setSslTermination(sslTermination);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).get(LOAD_BALANCER_ID, ACCOUNT_ID);
        Assert.assertEquals(lb.getUserName(), USERNAME);
        verify(usageEventCollection, times(2)).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).updateSslTermination(lb, queTermination);
        verify(usageEventCollection).processUsageEvent(eq(usages), eq(lb), eq(UsageEvent.SSL_MIXED_ON), any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveSslTerminationEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, SSL_TERMINATION_ID, EntryHelper.UPDATE_SSL_TERMINATION_TITLE, EntryHelper.createSslTerminationSummary(sslTermination), EventType.UPDATE_SSL_TERMINATION, CategoryType.UPDATE, EventSeverity.INFO);
    }

    @Test
    public void testUpdateLoadBalancerWithValidSslTerminationDisabled() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getZeusSslTermination()).thenReturn(queTermination);
        when(sslTermination.getEnabled()).thenReturn(false);
        when(sslTermination.getSecurePort()).thenReturn(80);
        when(sslTermination.getId()).thenReturn(SSL_TERMINATION_ID);
        when(queTermination.getSslTermination()).thenReturn(sslTermination);
        lb.setSslTermination(sslTermination);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).get(LOAD_BALANCER_ID, ACCOUNT_ID);
        Assert.assertEquals(lb.getUserName(), USERNAME);
        verify(usageEventCollection, times(2)).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).updateSslTermination(lb, queTermination);
        verify(usageEventCollection).processUsageEvent(eq(usages), eq(lb), eq(UsageEvent.SSL_OFF), any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveSslTerminationEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, SSL_TERMINATION_ID, EntryHelper.UPDATE_SSL_TERMINATION_TITLE, EntryHelper.createSslTerminationSummary(sslTermination), EventType.UPDATE_SSL_TERMINATION, CategoryType.UPDATE, EventSeverity.INFO);
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        updateSslTerminationListener.doOnMessage(objectMessage);

        //verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_SSL_TERMINATION), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidSslTermination() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(messageDataContainer.getZeusSslTermination()).thenReturn(queTermination);
        when(sslTermination.getEnabled()).thenReturn(true);
        when(sslTermination.getSecureTrafficOnly()).thenReturn(true);
        when(sslTermination.getSecurePort()).thenReturn(80);
        when(sslTermination.getId()).thenReturn(SSL_TERMINATION_ID);
        when(queTermination.getSslTermination()).thenReturn(sslTermination);
        lb.setSslTermination(sslTermination);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.get(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).updateSslTermination(lb, queTermination);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        updateSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).get(LOAD_BALANCER_ID, ACCOUNT_ID);
        Assert.assertEquals(lb.getUserName(), USERNAME);
        verify(usageEventCollection).getUsage(lb);
        verify(reverseProxyLoadBalancerStmService).updateSslTermination(lb, queTermination);
        Assert.assertEquals(lb.getStatus(), LoadBalancerStatus.ERROR);
        verify(loadBalancerService).update(lb);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_SSL_TERMINATION), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }
}

