package org.openstack.atlas.adapter.itest;

import com.zxtm.service.client.VirtualServerBasicInfo;
import com.zxtm.service.client.VirtualServerRule;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.IpHelper;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.adapter.zxtm.ZxtmAdapterImpl;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@RunWith(Enclosed.class)
public class NodelessIntegrationTest extends ZeusTestBase {
    public static class testBasicNodeless {
        protected static Node node;

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            lb.setNodes(new HashSet<Node>());
            setupSimpleLoadBalancer();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Test
        public void testBasicNodeless() {
            try {
                verifyActiveLBNodeState();
                addNode();
                verifyActiveLBNodeState();
                removeNode();
                verifyActiveLBNodeState();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        public void addNode() throws InsufficientRequestException, RollBackException, RemoteException {
            node = new Node();
            node.setIpAddress("127.0.0.1");
            node.setPort(lb.getPort());
            node.setCondition(NodeCondition.ENABLED);
            node.setWeight(1);
            lb.getNodes().add(node);

            zxtmAdapter.setNodes(config, lb);
        }

        public void removeNode() throws InsufficientRequestException, RollBackException, RemoteException {
            lb.getNodes().remove(node);
            zxtmAdapter.setNodes(config, lb);
        }

        public void verifyActiveLBNodeState() throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
            ArrayList<String> vsNames;

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(secureLoadBalancerName()));
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);

            VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{loadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedPort, virtualServerRules[0][0]);

            String[][] nodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, nodes.length);
            HashSet<String> expectedNodes = new HashSet<String>();
            for (Node n : lb.getNodes()) {
                expectedNodes.add(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
            }
            for (String n : nodes[0]) {
                if (!expectedNodes.remove(n)) {
                    Assert.fail("Unexpected Node '" + n + "' found in pool '" + poolName() + "'!");
                }
            }
            if (!expectedNodes.isEmpty()) {
                Assert.fail("Nodes not found in pool '" + poolName() + "': " + expectedNodes.toString());
            }
        }
    }

    public static class testingSslTerminationNodeless {
        protected static Node node;

        @BeforeClass
        public static void setupClass() throws InterruptedException {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            lb.setNodes(new HashSet<Node>());
            setupSimpleLoadBalancer();
        }

        @AfterClass
        public static void tearDownClass() {
            removeSimpleLoadBalancer();
        }

        @Before
        public void setupSsl() {
            setSslTermination();
        }

        @Test
        public void testingSslTerminationNodeless() {
            try {
                verifyActiveSslLBNodeState();
                addNode();
                verifyActiveSslLBNodeState();
                removeNode();
                verifyActiveSslLBNodeState();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

        public void addNode() throws InsufficientRequestException, RollBackException, RemoteException {
            node = new Node();
            node.setIpAddress("127.0.0.1");
            node.setPort(lb.getPort());
            node.setCondition(NodeCondition.ENABLED);
            node.setWeight(1);
            lb.getNodes().add(node);

            zxtmAdapter.setNodes(config, lb);
        }

        public void removeNode() throws InsufficientRequestException, RollBackException, RemoteException {
            lb.getNodes().remove(node);
            zxtmAdapter.setNodes(config, lb);
        }

        public void verifyActiveSslLBNodeState() throws RemoteException, InsufficientRequestException, ZxtmRollBackException {
            ArrayList<String> vsNames;

            vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));

            Assert.assertTrue(vsNames.contains(loadBalancerName()));
            Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));
            Assert.assertFalse(vsNames.contains(redirectLoadBalancerName()));
            Assert.assertFalse(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{loadBalancerName()})[0]);
            Assert.assertTrue(getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()})[0]);

            VirtualServerRule[][] virtualServerRules = getServiceStubs().getVirtualServerBinding().getRules(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, virtualServerRules.length);
            Assert.assertEquals(1, virtualServerRules[0].length);
            Assert.assertEquals(ZxtmAdapterImpl.ruleXForwardedPort, virtualServerRules[0][0]);

            boolean[] addForwardedFor = getServiceStubs().getVirtualServerBinding().getAddXForwardedForHeader(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, addForwardedFor.length);
            Assert.assertTrue(addForwardedFor[0]);

            boolean[] addForwardedProto = getServiceStubs().getVirtualServerBinding().getAddXForwardedProtoHeader(new String[]{secureLoadBalancerName()});
            Assert.assertEquals(1, addForwardedProto.length);
            Assert.assertTrue(addForwardedProto[0]);

            String[][] nodes = getServiceStubs().getPoolBinding().getNodes(new String[]{poolName()});
            Assert.assertEquals(1, nodes.length);
            HashSet<String> expectedNodes = new HashSet<String>();
            for (Node n : lb.getNodes()) {
                expectedNodes.add(IpHelper.createZeusIpString(n.getIpAddress(), n.getPort()));
            }
            for (String n : nodes[0]) {
                if (!expectedNodes.remove(n)) {
                    Assert.fail("Unexpected Node '" + n + "' found in pool '" + poolName() + "'!");
                }
            }
            if (!expectedNodes.isEmpty()) {
                Assert.fail("Nodes not found in pool '" + poolName() + "': " + expectedNodes.toString());
            }
        }

        private void setSslTermination() {
            try {
                String sVs = secureLoadBalancerName();
                SslTermination sslTermination = new SslTermination();
                sslTermination.setSecureTrafficOnly(true);
                sslTermination.setEnabled(true);
                sslTermination.setSecurePort(443);
                sslTermination.setCertificate(SslTerminationIntegrationTest.testCert);
                sslTermination.setPrivatekey(SslTerminationIntegrationTest.testKey);

                ZeusCrtFile zeusCrtFile = new ZeusCrtFile();
                zeusCrtFile.setPublic_cert(SslTerminationIntegrationTest.testCert);
                zeusCrtFile.setPrivate_key(SslTerminationIntegrationTest.testKey);

                ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
                zeusSslTermination.setCertIntermediateCert(SslTerminationIntegrationTest.testCert);
                zeusSslTermination.setSslTermination(sslTermination);

                lb.setSslTermination(zeusSslTermination.getSslTermination());

                zxtmAdapter.updateSslTermination(config, lb, zeusSslTermination);

                ArrayList<String> vsNames = new ArrayList<String>(Arrays.asList(getServiceStubs().getVirtualServerBinding().getVirtualServerNames()));
                Assert.assertTrue(vsNames.contains(secureLoadBalancerName()));

                String[] certificate = getServiceStubs().getVirtualServerBinding().getSSLCertificate(new String[]{sVs});
                Assert.assertEquals(sVs, certificate[0]);

                final VirtualServerBasicInfo[] serverBasicInfos = getServiceStubs().getVirtualServerBinding().getBasicInfo(new String[]{sVs});
                Assert.assertEquals(sslTermination.getSecurePort(), serverBasicInfos[0].getPort());
                Assert.assertEquals(true, lb.getProtocol().toString().equalsIgnoreCase(serverBasicInfos[0].getProtocol().toString()));
                Assert.assertEquals(ZxtmNameBuilder.genVSName(lb), serverBasicInfos[0].getDefault_pool());

                boolean[] vsEnabled = getServiceStubs().getVirtualServerBinding().getEnabled(new String[]{secureLoadBalancerName()});
                Assert.assertEquals(true, vsEnabled[0]);

                boolean[] vsNonSecureEnabled = getServiceStubs().getVirtualServerBinding().getSSLDecrypt(new String[]{sVs});
                Assert.assertEquals(sslTermination.getEnabled(), vsNonSecureEnabled[0]);

                String[] vsSecureInfo = getServiceStubs().getZxtmCatalogSSLCertificatesBinding().getRawCertificate(new String[]{sVs});
                Assert.assertEquals(sslTermination.getCertificate(), vsSecureInfo[0]);

            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        }

    }

}