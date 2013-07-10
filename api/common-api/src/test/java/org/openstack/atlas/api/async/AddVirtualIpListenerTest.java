package org.openstack.atlas.api.async;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.api.async.util.STMTestBase;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerStmService;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import javax.jms.Message;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(LoadBalancerService.class)
public class AddVirtualIpListenerTest extends STMTestBase{

    private Message message;
    private AddVirtualIpListener virtualIpListener;
    private MessageDataContainer messageDataContainer;
    private LoadBalancerService loadBalancerService;
    private ReverseProxyLoadBalancerStmService reverseProxyLoadBalancerStmService;


    @Before
    public void standUp() {
        PowerMockito.mockStatic(LoadBalancerService.class);
        setupIvars();
        message =  mock(Message.class);
        loadBalancerService = mock(LoadBalancerService.class);
        messageDataContainer = mock(MessageDataContainer.class);
        reverseProxyLoadBalancerStmService = mock(ReverseProxyLoadBalancerStmService.class);
        when(messageDataContainer.getLoadBalancerId()).thenReturn(234);
        try {
            when(virtualIpListener.getDataContainerFromMessage(message)).thenReturn(messageDataContainer);
            when(loadBalancerService.get(anyInt())).thenReturn(lb);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



    }

    @Ignore
    @Test
    public void testValidDoOneMessage() {
        try {
            virtualIpListener = new AddVirtualIpListener();
            virtualIpListener.doOnMessage(message);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            Assert.fail(e.getMessage());
        }


    }



}
