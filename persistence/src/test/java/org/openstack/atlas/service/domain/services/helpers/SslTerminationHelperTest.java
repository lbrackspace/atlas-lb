package org.openstack.atlas.service.domain.services.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;

@RunWith(Enclosed.class)
public class SslTerminationHelperTest {

        public static class whenCheckingIfProtocolIsSecure {


            LoadBalancer loadBalancer;


            @Before
            public void setUp() {

                loadBalancer = new LoadBalancer();

            }

            @Test
            public void shouldReturnTrueForHTTPProtocol() throws Exception {
                loadBalancer.setProtocol(LoadBalancerProtocol.HTTP);
                Boolean isSecure = SslTerminationHelper.isProtocolSecure(loadBalancer);
                Assert.assertEquals(true, isSecure);
            }

            @Test(expected = BadRequestException.class)
            public void shouldThrowExceptionForTCPCLIENTFIRSTProtocol() throws Exception {
                loadBalancer.setProtocol(LoadBalancerProtocol.TCP_CLIENT_FIRST);
                Boolean isSecure = SslTerminationHelper.isProtocolSecure(loadBalancer);
            }


        }


}
