package org.openstack.atlas.service.domain.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.ZeusEvent;
import org.openstack.atlas.service.domain.services.impl.CallbackServiceImpl;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;
import org.openstack.atlas.service.domain.services.impl.NotificationServiceImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class CallbackServiceImplTest {

    public static class handleZeusEvent {
        CallbackServiceImpl callbackService;
        NodeServiceImpl nodeService;
        NotificationServiceImpl notificationService;
        ZeusEvent zEvent;

        String mFail = "WARN monitors/571432_62203 monitorfail Monitor has detected a failure in node '10.178.224.134:443': Invalid HTTP response received; premature end of headers";
        String mOK = "INFO monitors/571432_62203 monitorok Monitor is working for node '10.178.224.134:443'.";

        @Before
        public void standUp() {
            nodeService = mock(NodeServiceImpl.class);
            notificationService = mock(NotificationServiceImpl.class);
            callbackService = new CallbackServiceImpl();
            callbackService.setNotificationService(notificationService);
            callbackService.setNodeService(nodeService);
            zEvent = new ZeusEvent();
            zEvent.setEventType("EventType");
            zEvent.setParamLine(mFail);

        }

        @Test(expected = BadRequestException.class)
        public void shouldFailWithBadParamLine() throws EntityNotFoundException, BadRequestException {
            String mFail = "Bad paramLine";
            zEvent.setParamLine(mFail);
            callbackService.handleZeusEvent(zEvent);
        }

        @Test
        public void shouldUpdateNodeStatusOnline() throws EntityNotFoundException, BadRequestException {
            zEvent.setParamLine(mOK);

            Node node = new Node();
            node.setId(373);
            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(12345);
            node.setLoadbalancer(lb);

            when(nodeService.getNodeByLoadBalancerIdIpAddressAndPort(Matchers.<Integer>any(), Matchers.<String>any(), Matchers.<Integer>any())).thenReturn(node);
            callbackService.handleZeusEvent(zEvent);

            Assert.assertEquals(NodeStatus.ONLINE, node.getStatus());
        }

        @Test
        public void shouldUpdateNodeStatusOffline() throws EntityNotFoundException, BadRequestException {
            zEvent.setParamLine(mFail);

            Node node = new Node();
            node.setId(373);
            LoadBalancer lb = new LoadBalancer();
            lb.setAccountId(12345);
            node.setLoadbalancer(lb);

            when(nodeService.getNodeByLoadBalancerIdIpAddressAndPort(Matchers.<Integer>any(), Matchers.<String>any(), Matchers.<Integer>any())).thenReturn(node);
            callbackService.handleZeusEvent(zEvent);

            Assert.assertEquals(NodeStatus.OFFLINE, node.getStatus());
        }
    }
}
