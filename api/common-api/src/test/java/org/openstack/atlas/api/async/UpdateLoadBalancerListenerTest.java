package org.openstack.atlas.api.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NotificationService;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.util.converters.StringConverter;

import javax.jms.ObjectMessage;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateLoadBalancerListenerTest extends STMTestBase {
    private Integer LOAD_BALANCER_ID;
    private Integer ACCOUNT_ID;
    private String USERNAME = "SOME_USERNAME";
    private String LOAD_BALANCER_NAME = "SOME_LB_NAME";
    private LoadBalancerAlgorithm LOAD_BALANCER_ALGORITHM = LoadBalancerAlgorithm.ROUND_ROBIN;

    @Mock
    private ObjectMessage objectMessage;
    @Mock
    private LoadBalancerService loadBalancerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;

    private UpdateLoadBalancerListener updateLoadBalancerListener;

    @Before
    public void standUp() {
        MockitoAnnotations.initMocks(this);
        setupIvars();
        LOAD_BALANCER_ID = lb.getId();
        ACCOUNT_ID = lb.getAccountId();
        lb.setUserName(USERNAME);
        lb.setAlgorithm(LOAD_BALANCER_ALGORITHM);
        lb.setName(LOAD_BALANCER_NAME);
        updateLoadBalancerListener = new UpdateLoadBalancerListener();
        updateLoadBalancerListener.setLoadBalancerService(loadBalancerService);
        updateLoadBalancerListener.setNotificationService(notificationService);
        updateLoadBalancerListener.setReverseProxyLoadBalancerStmService(reverseProxyLoadBalancerStmService);
    }

    @After
    public void tearDown() {
        stmClient.destroy();
    }

    private String genAtomSummary() {
        StringBuilder atomSummary = new StringBuilder("Load balancer successfully updated with ");
        List<String> updateStrList = new ArrayList<String>();
        atomSummary.append("algorithm: '").append(lb.getAlgorithm().name()).append("', ");
        if (lb.getAlgorithm() != null) atomSummary.append("algorithm: '").append(lb.getAlgorithm().name()).append("', ");
        if (lb.getProtocol() != null) atomSummary.append("protocol: '").append(lb.getProtocol().name()).append("', ");
        if (lb.getPort() != null) atomSummary.append("port: '").append(lb.getPort()).append("', ");
        if (lb.getTimeout() != null) atomSummary.append("timeout: '").append(lb.getTimeout()).append("', ");
        if (lb.isHalfClosed() != null) atomSummary.append("half-close: '").append(lb.isHalfClosed()).append("', ");
        if (lb.getName() != null) updateStrList.add(String.format("%s: '%s'", "name", lb.getName()));
        atomSummary.append(StringConverter.commaSeperatedStringList(updateStrList));
        return atomSummary.toString();
    }

    @Test
    public void testUpdateValidLoadBalancer() throws Exception {
        String atomSummary = genAtomSummary();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);

        updateLoadBalancerListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ACTIVE);
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), eq(atomSummary), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.UPDATE), eq(EventSeverity.INFO));
    }

    @Test
    public void testUpdateInvalidLoadBalancer() throws Exception {
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenThrow(entityNotFoundException);

        updateLoadBalancerListener.doOnMessage(objectMessage);

        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(entityNotFoundException), eq(AlertType.DATABASE_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }

    @Test
    public void testUpdateLoadBalancerWithInvalidPayload() throws Exception {
        Exception exception = new Exception();
        when(objectMessage.getObject()).thenReturn(lb);
        when(loadBalancerService.getWithUserPages(LOAD_BALANCER_ID, ACCOUNT_ID)).thenReturn(lb);
        doThrow(exception).when(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);

        updateLoadBalancerListener.doOnMessage(objectMessage);

        verify(reverseProxyLoadBalancerStmService).updateLoadBalancer(lb, lb);
        verify(loadBalancerService).setStatus(lb, LoadBalancerStatus.ERROR);
        verify(notificationService).saveAlert(eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), eq(exception), eq(AlertType.ZEUS_FAILURE.name()), anyString());
        verify(notificationService).saveLoadBalancerEvent(eq(USERNAME), eq(ACCOUNT_ID), eq(LOAD_BALANCER_ID), anyString(), anyString(), eq(EventType.UPDATE_LOADBALANCER), eq(CategoryType.UPDATE), eq(EventSeverity.CRITICAL));
    }
}