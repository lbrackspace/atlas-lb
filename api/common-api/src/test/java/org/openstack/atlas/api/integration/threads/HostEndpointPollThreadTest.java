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

        hostEndpointPollThread.setEndPointWorking(false);
        hostEndpointPollThread.setRestEndPointWorking(false);
        hostEndpointPollThread.run();
        verify(reverseProxyLoadBalancerVTMService, times(1)).isEndPointWorking(any());
        Assert.assertTrue(hostEndpointPollThread.isRestEndPointWorking());
    }

    @Test
    public void testPollEndpointsInActive() throws Exception {
        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenReturn(Boolean.FALSE);

        hostEndpointPollThread.setEndPointWorking(true);
        hostEndpointPollThread.setRestEndPointWorking(true);
        hostEndpointPollThread.run();
        verify(reverseProxyLoadBalancerVTMService, times(1)).isEndPointWorking(any());
        Assert.assertFalse(hostEndpointPollThread.isRestEndPointWorking());
    }

    @Test
    public void testPollVTMServiceEndpointsThrowException() throws Exception {
        when(reverseProxyLoadBalancerVTMService.isEndPointWorking(any())).thenThrow(Exception.class);

        hostEndpointPollThread.run();
        Assert.assertNotNull(hostEndpointPollThread.getException());
        Assert.assertTrue(hostEndpointPollThread.getException() instanceof Exception);
    }
}