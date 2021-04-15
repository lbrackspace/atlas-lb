package org.openstack.atlas.api.mgmt.async;


import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class MigrateVipsToClusterListenerTest {

    public static class whenMigratingVips {

        @Mock
        private ObjectMessage objectMessage;
        @Mock
        private EsbRequest esbRequest;
        @Mock
        VirtualIpServiceImpl virtualIpService;
        @InjectMocks
        MigrateVipsToClusterListener migrateVipsToClusterListener;

        Cidr cidr;
        Integer newClusterId;

        @Before
        public void setUp() throws JMSException {
            MockitoAnnotations.initMocks(this);
            migrateVipsToClusterListener = new MigrateVipsToClusterListener();
            migrateVipsToClusterListener.setVirtualIpService(virtualIpService);
            cidr = new Cidr();
            cidr.setBlock("10.25.0.0/24");
            newClusterId = 1;
            when(objectMessage.getObject()).thenReturn(esbRequest);
            when(esbRequest.getCidr()).thenReturn(cidr);
            when(esbRequest.getClusterId()).thenReturn(newClusterId);
        }

        @Test
        public void shouldSendClusterIdAndCidrToServiceLayer() throws Exception {
            migrateVipsToClusterListener.doOnMessage(objectMessage);
            verify(virtualIpService, times(1)).migrateVipsToClusterByCidrBlock(newClusterId, cidr);
        }
    }

}
