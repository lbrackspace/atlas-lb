package org.openstack.atlas.api.integration.threads;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.service.domain.entities.Host;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HostEndpointPollThreadTest {

    @Mock
    private ReverseProxyLoadBalancerVTMService reverseProxyLoadBalancerVTMService;
    @InjectMocks
    private HostEndpointPollThread hostEndpointPollThread;

    @Before
    public void standUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        hostEndpointPollThread = new HostEndpointPollThread();
        hostEndpointPollThread.setVTMProxyService(reverseProxyLoadBalancerVTMService);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPollEndpointsActive() throws Exception {
        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenReturn(Boolean.TRUE);

        hostEndpointPollThread.run();
        verify(reverseProxyLoadBalancerVTMService, times(1)).isEndPointWorking(any());
        Assert.assertTrue(hostEndpointPollThread.isRestEndPointWorking());
        Assert.assertFalse(hostEndpointPollThread.isEndPointWorking());
    }

    @Test
    public void testPollEndpointThreadToString() throws Exception {
        Host h = new Host();
        h.setId(1);
        h.setRestEndpoint("http://restendpoint.co/config/active");

        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenReturn(Boolean.TRUE);
        hostEndpointPollThread.setHost(h);
        hostEndpointPollThread.run();
        String expectedString = "HostEndpointPollThread{ host_id=1, url=http://restendpoint.co/config/active}";
        Assert.assertEquals(expectedString, hostEndpointPollThread.toString());
        verify(reverseProxyLoadBalancerVTMService, times(1)).isEndPointWorking(any());
        Assert.assertTrue(hostEndpointPollThread.isRestEndPointWorking());
        Assert.assertFalse(hostEndpointPollThread.isEndPointWorking());
    }

    @Test
    public void testPollEndpointsInActive() throws Exception {
        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenReturn(Boolean.FALSE);

        hostEndpointPollThread.run();
        verify(reverseProxyLoadBalancerVTMService, times(1)).isEndPointWorking(any());
        Assert.assertFalse(hostEndpointPollThread.isRestEndPointWorking());
        Assert.assertFalse(hostEndpointPollThread.isEndPointWorking());
    }

    @Test
    public void testPollVTMServiceEndpointsThrowException() throws Exception {
        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenThrow(Exception.class);

        hostEndpointPollThread.run();
        Assert.assertNotNull(hostEndpointPollThread.getException());
        Assert.assertTrue(hostEndpointPollThread.getException() instanceof Exception);
    }
}