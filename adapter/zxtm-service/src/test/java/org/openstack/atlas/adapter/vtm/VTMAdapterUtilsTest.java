package org.openstack.atlas.adapter.vtm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.SslTermination;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class VTMAdapterUtilsTest {

    public static class WhenCompilingVSNames {
        LoadBalancer loadBalancer;
        SslTermination sslTermination;


        @Before
        public void standUp() throws Exception {
            loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.setAccountId(22);
            loadBalancer.setHttpsRedirect(false);

            sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(false);
            loadBalancer.setSslTermination(sslTermination);
        }

        @After
        public void tearDown() {
        }

        @Test
        public void testGetVSNamesSslMixedWithRedirect() throws Exception {
            loadBalancer.getSslTermination().setEnabled(true);
            loadBalancer.setHttpsRedirect(true);

            Map<VTMAdapterUtils.VSType, String> vsNames = VTMAdapterUtils.getVSNamesForLB(loadBalancer);

            assertEquals(2, vsNames.size());
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.REDIRECT_VS));
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.SECURE_VS));
            assertTrue(vsNames.containsValue("22_1_R"));
            assertTrue(vsNames.containsValue("22_1_S"));
        }

        @Test
        public void testGetVSNamesSslMixedNoRedirect() throws Exception {
            loadBalancer.getSslTermination().setEnabled(true);
            Map<VTMAdapterUtils.VSType, String> vsNames = VTMAdapterUtils.getVSNamesForLB(loadBalancer);

            assertEquals(2, vsNames.size());
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.DEFAULT_VS));
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.SECURE_VS));
            assertTrue(vsNames.containsValue("22_1"));
            assertTrue(vsNames.containsValue("22_1_S"));
        }

        @Test
        public void testGetVSNamesSslOnlyNoRedirect() throws Exception {
            loadBalancer.getSslTermination().setEnabled(true);
            loadBalancer.getSslTermination().setSecureTrafficOnly(true);
            Map<VTMAdapterUtils.VSType, String> vsNames = VTMAdapterUtils.getVSNamesForLB(loadBalancer);

            assertEquals(1, vsNames.size());
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.SECURE_VS));
            assertTrue(vsNames.containsValue("22_1_S"));
        }

        @Test
        public void testGetVSNamesRedirectNoSsl() throws Exception {
            loadBalancer.setHttpsRedirect(true);
            Map<VTMAdapterUtils.VSType, String> vsNames = VTMAdapterUtils.getVSNamesForLB(loadBalancer);

            assertEquals(2, vsNames.size());
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.DEFAULT_VS));
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.REDIRECT_VS));
            assertTrue(vsNames.containsValue("22_1"));
            assertTrue(vsNames.containsValue("22_1_R"));
        }

        @Test
        public void testGetVSNamesDefault() throws Exception {
            Map<VTMAdapterUtils.VSType, String> vsNames = VTMAdapterUtils.getVSNamesForLB(loadBalancer);

            assertEquals(1, vsNames.size());
            assertTrue(vsNames.containsKey(VTMAdapterUtils.VSType.DEFAULT_VS));
            assertTrue(vsNames.containsValue("22_1"));
        }

    }
}