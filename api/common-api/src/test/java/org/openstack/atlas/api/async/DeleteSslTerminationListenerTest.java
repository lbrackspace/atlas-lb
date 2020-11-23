package org.openstack.atlas.api.async;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.VTMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.ConfigurationKey;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.service.domain.entities.CertificateMapping;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.CertificateMappingService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.SslTerminationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.collection.UsageEventCollection;

import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeleteSslTerminationListenerTest extends VTMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private List<SnmpUsage> usages;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private MessageDataContainer messageDataContainer;
    @Mock
    private SslTermination previousSslTermination;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private CertificateMappingService certificateMappingService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @Mock
    private UsageEventCollection usageEventCollection;
    @Mock
    private SslTerminationService sslTerminationService;
    @Mock
    private RestApiConfiguration config;

    private DeleteSslTerminationListener deleteSslTerminationListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        usages = new ArrayList<SnmpUsage>();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setStatus(LoadBalancerStatus.ACTIVE);
        // Certificatemapping verifications
        CertificateMapping cm = new CertificateMapping();
        cm.setCertificate("cert");
        cm.setId(1);
        cm.setHostName("Host1");
        CertificateMapping cm2 = new CertificateMapping();
        cm2.setCertificate("cert2");
        cm2.setId(2);
        cm2.setHostName("Host2");
        lb.getCertificateMappings().add(cm);
        lb.getCertificateMappings().add(cm2);
        deleteSslTerminationListener = new DeleteSslTerminationListener();
        deleteSslTerminationListener.setLoadBalancerService(loadBalancerService);
        deleteSslTerminationListener.setCertificateMappingService(certificateMappingService);
        deleteSslTerminationListener.setNotificationService(notificationService);
        deleteSslTerminationListener.setReverseProxyLoadBalancerVTMService(reverseProxyLoadBalancerVTMService);
        deleteSslTerminationListener.setUsageEventCollection(usageEventCollection);
        deleteSslTerminationListener.setSslTerminationService(sslTerminationService);
        deleteSslTerminationListener.setConfiguration(config);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteSslTermination() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getPreviousSslTermination()).thenReturn(previousSslTermination);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(reverseProxyLoadBalancerVTMService).removeSslTermination(lb);
        verify(sslTerminationService).deleteSslTermination(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(usageEventCollection).processUsageEvent(eq(usages), eq(lb), eq(UsageEvent.SSL_OFF), any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveLoadBalancerEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, "Load Balancer SSL Termination Successfully Deleted", "Load balancer ssl termination successfully deleted", EventType.DELETE_SSL_TERMINATION, CategoryType.DELETE, EventSeverity.INFO);
    }

    @Test
    public void testDeleteSslTerminationWithCertificateMappings() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getPreviousSslTermination()).thenReturn(previousSslTermination);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(usageEventCollection.getUsage(lb)).thenReturn(usages);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(reverseProxyLoadBalancerVTMService).removeSslTermination(lb);
        // Should call to remove two certification mappings
        verify(certificateMappingService, times(2)).deleteByIdAndLoadBalancerId(Matchers.anyInt(), Matchers.anyInt());
        for (CertificateMapping cm : lb.getCertificateMappings()) {
            // Verify it was called with both cm ids
            verify(certificateMappingService).deleteByIdAndLoadBalancerId(cm.getId(), lb.getId());
        }
        verify(sslTerminationService).deleteSslTermination(LOAD_BALANCER_ID, ACCOUNT_ID);
        verify(usageEventCollection).processUsageEvent(eq(usages), eq(lb), eq(UsageEvent.SSL_OFF), any(Calendar.class));
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveLoadBalancerEvent(USERNAME, ACCOUNT_ID, LOAD_BALANCER_ID, "Load Balancer SSL Termination Successfully Deleted", "Load balancer ssl termination successfully deleted", EventType.DELETE_SSL_TERMINATION, CategoryType.DELETE, EventSeverity.INFO);
    }


    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(EntityNotFoundException.class);

        deleteSslTerminationListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(EntityNotFoundException.class), eq(AlertType.DATABASE_FAILURE.name()), eq(String.format("Load balancer '%d' not found in database.", lb.getId())));
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_SSL_TERMINATION), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testDeleteInvalidSslTermination() throws Exception {
        when(objectMessage.getObject()).thenReturn(messageDataContainer);
        when(messageDataContainer.getAccountId()).thenReturn(ACCOUNT_ID);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(LOAD_BALANCER_ID);
        when(messageDataContainer.getUserName()).thenReturn(USERNAME);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(Exception.class).when(reverseProxyLoadBalancerVTMService).removeSslTermination(lb);
        when(config.getString(Matchers.<ConfigurationKey>any())).thenReturn("REST");

        deleteSslTerminationListener.doOnMessage(objectMessage);

        verify(loadBalancerService).getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID);
        Assert.assertEquals(lb.getUserName(), USERNAME);
        verify(reverseProxyLoadBalancerVTMService).removeSslTermination(lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), isA(Exception.class), eq(AlertType.ZEUS_FAILURE.name()), eq(String.format("Error deleting loadbalancer '%d' ssl termination backend...", lb.getId())));
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.DELETE_SSL_TERMINATION), eq(CategoryType.DELETE), eq(EventSeverity.CRITICAL));
        verify(usageEventCollection).collectUsageAndProcessUsageRecords(eq(lb), eq(UsageEvent.SSL_OFF), any(Calendar.class));
    }
}
