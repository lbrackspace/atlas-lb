package org.openstack.atlas.adapter.vtm;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.*;
import org.openstack.atlas.adapter.zxtm.ZxtmConversionUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ip.exception.IPStringConversionException;
import org.rackspace.vtm.client.bandwidth.Bandwidth;
import org.rackspace.vtm.client.bandwidth.BandwidthBasic;
import org.rackspace.vtm.client.bandwidth.BandwidthProperties;
import org.rackspace.vtm.client.exception.ClientException;
import org.rackspace.vtm.client.monitor.Monitor;
import org.rackspace.vtm.client.monitor.MonitorBasic;
import org.rackspace.vtm.client.monitor.MonitorHttp;
import org.rackspace.vtm.client.monitor.MonitorProperties;
import org.rackspace.vtm.client.pool.*;
import org.rackspace.vtm.client.protection.Protection;
import org.rackspace.vtm.client.protection.ProtectionAccessRestriction;
import org.rackspace.vtm.client.ssl.keypair.Keypair;
import org.rackspace.vtm.client.traffic.ip.TrafficIp;
import org.rackspace.vtm.client.traffic.ip.TrafficIpBasic;
import org.rackspace.vtm.client.virtualserver.*;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class VTMResourceTranslatorTest extends VTMTestBase {


    public static class WhenTranslatingAVirtualServer {

        private String vsName;
        private Boolean isHalfClosed;
        private Boolean isConnectionLogging;
        private Boolean isContentCaching;
        private String logFormat;
        private VirtualServerTcp expectedTcp;
        private VirtualServerConnectionErrors expectedError;
        private ArrayList<String> rules;
        private VTMResourceTranslator translator;
        private String errorFile;
        private AccessList accessListAllowed;
        private String ipAddressAllowed;
        private Set<AccessList> lists;
        @Mock
        private LoadBalancerEndpointConfiguration config;


        public void initializeVars(String logFormat, LoadBalancerProtocol protocol) throws InsufficientRequestException {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            vsName = ZxtmNameBuilder.genVSName(lb);
            this.isConnectionLogging = true;
            isHalfClosed = true;
            isContentCaching = true;
            expectedTcp = new VirtualServerTcp();
            expectedError = new VirtualServerConnectionErrors();
            errorFile = "Default";
            expectedError.setErrorFile(errorFile);
            rules = new ArrayList<String>();

            lb.setProtocol(protocol);

            if (lb.getProtocol() == LoadBalancerProtocol.HTTP) {
                rules.add(VTMConstants.XFPORT);
            }
            translator = new VTMResourceTranslator();

            ConnectionLimit connectionLimit = new ConnectionLimit();
            Set<AccessList> accessListSet = new HashSet<AccessList>();
            accessListSet.add(new AccessList());
            this.logFormat = logFormat;


            lb.setConnectionLogging(isConnectionLogging);
            lb.setHalfClosed(isHalfClosed);
            lb.setContentCaching(isContentCaching);
            lb.setConnectionLimit(connectionLimit);
            lb.setAccessLists(accessListSet);
        }

        public void initializeVars(String logFormat, LoadBalancerProtocol protocol, String errorFile) throws InsufficientRequestException {

            initializeVars(logFormat, protocol);
            this.errorFile = errorFile;
            UserPages userPages = new UserPages();
            userPages.setErrorpage(this.errorFile);
            lb.setUserPages(userPages);
            expectedError.setErrorFile(this.errorFile);
            lb.setConnectionLimit(null);

            ipAddressAllowed = "10.1.1.1";
            accessListAllowed = new AccessList();
            accessListAllowed.setIpAddress(ipAddressAllowed);
            accessListAllowed.setType(AccessListType.ALLOW);

            lists = new HashSet<AccessList>();
            lists.add(accessListAllowed);
            lb.setAccessLists(lists);


        }

        public void pathOne() {
            lists = new HashSet<AccessList>();
            lb.setAccessLists(lists);
            lb.setConnectionLimit(null);
            isConnectionLogging = false;
            lb.setConnectionLogging(isConnectionLogging);


        }

        public void pathTwo() {
            lists.add(accessListAllowed);
            lb.setConnectionLimit(null);

        }

        public void pathThree() {
            lists.add(accessListAllowed);
            ConnectionLimit connectionLimit = new ConnectionLimit();
            lb.setConnectionLimit(connectionLimit);


        }

        public void pathFour() {
            lb.setConnectionLimit(null);
            lb.setAccessLists(null);
        }


        @Test
        public void stringToObjTest() throws InsufficientRequestException, IOException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n",
                    LoadBalancerProtocol.HTTP);
            String refString = "{\"properties\":{\"basic\":{\"bandwidth_class\":\"999998_999998\",\"failure_pool\":" +
                    "\"\",\"lard_size\":2047,\"max_connection_attempts\":0,\"max_idle_connections_pernode\":50,\"" +
                    "max_timed_out_connection_attempts\":2,\"monitors\":[\"999998_999998\"],\"node_close_with_rst\"" +
                    ":false,\"node_connection_attempts\":3,\"node_delete_behavior\":\"immediate\",\"" +
                    "node_drain_to_delete_timeout\":0,\"nodes_table\":[{\"node\":\"27.0.0.1:80\",\"priority\":1," +
                    "\"state\":\"active\",\"weight\":1,\"source_ip\":\"\"}],\"note\":\"\",\"passive_monitoring\"" +
                    ":false,\"persistence_class\":\"999998_999998\",\"transparent\":false},\"connection\":" +
                    "{\"max_connect_time\":4,\"max_connections_per_node\":0,\"max_queue_size\":0,\"max_reply_time\"" +
                    ":10,\"max_transactions_per_node\":0,\"queue_timeout\":10},\"load_balancing\":{\"algorithm\":\"" +
                    "weighted_least_connections\",\"priority_enabled\":true,\"priority_nodes\":1}}}";

            Pool createdPool = translator.stringToObject(refString, Pool.class);
            System.out.println("Pool String AFter Mapping: " + createdPool.toString());
            Assert.assertEquals(1, createdPool.getProperties().getBasic().getNodesTable().size());
            Assert.assertEquals("27.0.0.1:80", createdPool.getProperties().getBasic().getNodesTable().get(0).getNode());
            Assert.assertEquals(Integer.valueOf(1), createdPool.getProperties().getBasic().getNodesTable().get(0).getWeight());
            Assert.assertEquals(vsName, createdPool.getProperties().getBasic().getPersistenceClass());
            Assert.assertEquals(false, createdPool.getProperties().getBasic().getPassiveMonitoring());
            Assert.assertEquals(true, createdPool.getProperties().getLoadBalancing().getPriorityEnabled());
            Assert.assertEquals(PoolLoadbalancing.Algorithm.WEIGHTED_LEAST_CONNECTIONS, createdPool.getProperties().getLoadBalancing().getAlgorithm());
            String poolString = translator.objectToString(createdPool, Pool.class);
            Assert.assertEquals(refString, poolString);

        }

        @Test
        public void ObjToStringTest() throws InsufficientRequestException, IOException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n",
                    LoadBalancerProtocol.HTTP);

            String rstr = "{\"properties\":{\"basic\":{\"bandwidth_class\":\"999998_999998\",\"failure_pool\":\"\"," +
                    "\"lard_size\":2047,\"max_connection_attempts\":0,\"max_idle_connections_pernode\":50,\"" +
                    "max_timed_out_connection_attempts\":2,\"monitors\":[\"999998_999998\"],\"node_close_with_rst\"" +
                    ":false,\"node_connection_attempts\":3,\"node_delete_behavior\":\"immediate\",\"" +
                    "node_drain_to_delete_timeout\":0,\"nodes_table\":[{\"node\":\"27.0.0.1:80\",\"priority\":1," +
                    "\"state\":\"active\",\"weight\":1,\"source_ip\":\"\"}],\"note\":\"\",\"passive_monitoring\"" +
                    ":false,\"persistence_class\":\"999998_999998\",\"transparent\":false},\"connection\"" +
                    ":{\"max_connect_time\":4,\"max_connections_per_node\":0,\"max_queue_size\":0,\"" +
                    "max_reply_time\":10,\"max_transactions_per_node\":0,\"queue_timeout\":10},\"" +
                    "load_balancing\":{\"algorithm\":\"weighted_least_connections\",\"priority_enabled\"" +
                    ":true,\"priority_nodes\":1}}}";
            Pool pool = new Pool();
            PoolProperties properties = new PoolProperties();
            PoolBasic basic = new PoolBasic();
            List<PoolNodesTable> poolNodesTable = new ArrayList<>();
            PoolLoadbalancing poollb = new PoolLoadbalancing();
            PoolConnection connection = new PoolConnection();

            PoolNodesTable pnt = new PoolNodesTable();
            pnt.setWeight(1);
            pnt.setState(PoolNodesTable.State.ACTIVE);
            pnt.setNode("27.0.0.1:80");
            pnt.setPriority(1);
            poolNodesTable.add(pnt);

            poollb.setAlgorithm(PoolLoadbalancing.Algorithm.fromValue("weighted_least_connections"));
            poollb.setPriorityEnabled(true);

            connection.setMaxReplyTime(10);

            basic.setMonitors(new HashSet<String>(Arrays.asList(vsName)));
            basic.setPersistenceClass(vsName);
            basic.setBandwidthClass(vsName);
            basic.setPassiveMonitoring(false);

            basic.setNodesTable(poolNodesTable);
            properties.setBasic(basic);
            properties.setLoadBalancing(poollb);
            properties.setConnection(connection);
            pool.setProperties(properties);

            String poolString = translator.objectToString(pool, Pool.class);
            Assert.assertEquals(rstr, poolString);

        }

        @Ignore
        @Test
        public void experimental() throws InsufficientRequestException, IOException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            String refString = "{\"error_id\":\"resource.validation_error\",\"error_text\":\"The resource provided is " +
                    "invalid\",\"error_info\": {\"load_balancing\":{\"node_weighting\":{\"127.0.0.1:80\":{\"weight\":" +
                    "{\"error_id\":\"table.no_default\",\"error_text\":\"Table field 'weight' is not set and has no " +
                    "default value\"}},\"127.0.0.2:80\":{\"weight\":{\"error_id\":\"table.no_default\",\"error_text\":\"" +
                    "Table field 'weight' is not set and has no default value\"}}}}}}";
            ClientException exception = translator.stringToObject(refString, ClientException.class);

        }

        @Test
        public void shouldCreateValidVirtualServer() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxyClose(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWebCache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(ZxtmConversionUtils.mapProtocol(lb.getProtocol()).getValue()), createdBasic.getProtocol());
            Assert.assertEquals(lb.getPort(), createdBasic.getPort());
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListenOnAny());
            if (lb.isContentCaching())
                rules.add(VTMConstants.CONTENT_CACHING);
            Assert.assertEquals(rules.size(), createdBasic.getRequestRules().size());
//            Assert.assertTrue(rules.containsAll(createdBasic.getRequest_rules()));

//            Assert.assertEquals(expectedError, createdProperties.getConnectionErrors());
        }

        @Test
        public void shouldCreateValidVirtualServerWithNoSslReferences() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            Assert.assertNull(createdProperties.getSsl());
            expectedTcp.setProxyClose(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWebCache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(VirtualServerBasic.Protocol.fromValue(ZxtmConversionUtils.mapProtocol(lb.getProtocol()).getValue()), createdBasic.getProtocol());
            Assert.assertEquals(lb.getPort(), createdBasic.getPort());
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListenOnAny());
            if (lb.isContentCaching())
                rules.add(VTMConstants.CONTENT_CACHING);
            Assert.assertEquals(rules.size(), createdBasic.getRequestRules().size());
//            Assert.assertTrue(rules.containsAll(createdBasic.getRequest_rules()));

//            Assert.assertEquals(expectedError, createdProperties.getConnectionErrors());
        }

        @Test
        public void shouldCreateValidVirtualServerWithSSLTermination() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            String secureName = ZxtmNameBuilder.genSslVSName(lb);

            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(VTMTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(VTMTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(VTMTestConstants.SSL_KEY);
            sslTermination.setTls10Enabled(true);
            sslTermination.setTls11Enabled(false);

            SslCipherProfile cipherProfile = new SslCipherProfile();
            cipherProfile.setCiphers(VTMTestConstants.CIPHER_LIST);
            cipherProfile.setComments("cipherpro1");
            cipherProfile.setName("datenameid");
            sslTermination.setCipherProfile(cipherProfile);
            sslTermination.setCipherList(cipherProfile.getCiphers());

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(VTMTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(VTMTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(VTMTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            VirtualServer createdServer = translator.translateVirtualServerResource(config, secureName, lb);
            Assert.assertNotNull(createdServer);

            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxyClose(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWebCache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListenOnAny());
            if (lb.isContentCaching()) {
                rules.add(VTMConstants.CONTENT_CACHING);
            }
            Assert.assertEquals(rules.size(), createdBasic.getRequestRules().size());


            Assert.assertEquals(VTMTestConstants.LB_SECURE_PORT, (int) createdBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(createdBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getPool().toString());
            Assert.assertEquals(true, createdBasic.getSslDecrypt());
            Assert.assertEquals(VTMTestConstants.CIPHER_LIST, createdServer.getProperties().getSsl().getCipherSuites());
            Assert.assertEquals(VirtualServerSsl.SupportTls1.ENABLED, createdServer.getProperties().getSsl().getSupportTls1());
            Assert.assertEquals(VirtualServerSsl.SupportTls11.DISABLED, createdServer.getProperties().getSsl().getSupportTls11());
            Assert.assertEquals(secureName, createdProperties.getSsl().getServerCertDefault());
            Assert.assertTrue(createdProperties.getHttp().getAddXForwardedFor());
            Assert.assertTrue(createdProperties.getHttp().getAddXForwardedProto());
            Assert.assertEquals(VirtualServerHttp.LocationRewrite.NEVER, createdProperties.getHttp().getLocationRewrite());

        }

        @Test
        public void shouldCreateValidVirtualServerWithSSLTerminationNonHTTP() throws InsufficientRequestException {
            // Verify for that any other potentially allowed non-secure protocols httpHeaders are not set.

            initializeVars("%v %t %h %A:%p %n %B %b %T", LoadBalancerProtocol.TCP);
            String secureName = ZxtmNameBuilder.genSslVSName(lb);

            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(VTMTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(VTMTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(VTMTestConstants.SSL_KEY);
            sslTermination.setTls10Enabled(true);
            sslTermination.setTls11Enabled(false);

            SslCipherProfile cipherProfile = new SslCipherProfile();
            cipherProfile.setCiphers(VTMTestConstants.CIPHER_LIST);
            cipherProfile.setComments("cipherpro1");
            cipherProfile.setName("datenameid");
            sslTermination.setCipherProfile(cipherProfile);
            sslTermination.setCipherList(cipherProfile.getCiphers());

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(VTMTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(VTMTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(VTMTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            VirtualServer createdServer = translator.translateVirtualServerResource(config, secureName, lb);
            Assert.assertNotNull(createdServer);

            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxyClose(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWebCache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListenOnAny());
            if (lb.isContentCaching()) {
                rules.add(VTMConstants.CONTENT_CACHING);
                Assert.assertTrue(createdBasic.getRequestRules().contains(VTMConstants.CONTENT_CACHING));
            }
            if (lb.getProtocol() == LoadBalancerProtocol.HTTP) {
                Assert.assertTrue(createdBasic.getRequestRules().contains(VTMConstants.XFPORT));

            }
            Assert.assertEquals(rules.size(), createdBasic.getRequestRules().size());



            Assert.assertEquals(VTMTestConstants.LB_SECURE_PORT, (int) createdBasic.getPort());
            // TCP Maps to server_first
            Assert.assertTrue("server_first".equalsIgnoreCase(createdBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getPool().toString());
            Assert.assertEquals(true, createdBasic.getSslDecrypt());
            Assert.assertEquals(VTMTestConstants.CIPHER_LIST, createdServer.getProperties().getSsl().getCipherSuites());
            Assert.assertEquals(VirtualServerSsl.SupportTls1.ENABLED, createdServer.getProperties().getSsl().getSupportTls1());
            Assert.assertEquals(VirtualServerSsl.SupportTls11.DISABLED, createdServer.getProperties().getSsl().getSupportTls11());
            Assert.assertEquals(secureName, createdProperties.getSsl().getServerCertDefault());
            Assert.assertNull(createdProperties.getHttp());

        }

        @Test
        public void shouldCreateValidVirtualServerWithCertificateMappings() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);
            String secureName = ZxtmNameBuilder.genSslVSName(lb);

            boolean isVsEnabled = true;
            SslTermination sslTermination = new SslTermination();
            sslTermination.setSecureTrafficOnly(false);
            sslTermination.setEnabled(true);
            sslTermination.setSecurePort(VTMTestConstants.LB_SECURE_PORT);
            sslTermination.setCertificate(VTMTestConstants.SSL_CERT);
            sslTermination.setPrivatekey(VTMTestConstants.SSL_KEY);
            sslTermination.setTls10Enabled(true);
            sslTermination.setTls11Enabled(false);

            SslCipherProfile cipherProfile = new SslCipherProfile();
            cipherProfile.setCiphers(VTMTestConstants.CIPHER_LIST);
            cipherProfile.setComments("cipherpro1");
            cipherProfile.setName("datenameid");
            sslTermination.setCipherProfile(cipherProfile);
            sslTermination.setCipherList(cipherProfile.getCiphers());

            ZeusCrtFile zeusCertFile = new ZeusCrtFile();
            zeusCertFile.setPublic_cert(VTMTestConstants.SSL_CERT);
            zeusCertFile.setPrivate_key(VTMTestConstants.SSL_KEY);

            ZeusSslTermination zeusSslTermination = new ZeusSslTermination();
            zeusSslTermination.setCertIntermediateCert(VTMTestConstants.SSL_CERT);
            zeusSslTermination.setSslTermination(sslTermination);

            lb.setSslTermination(zeusSslTermination.getSslTermination());

            Set<CertificateMapping> cms = new HashSet<>();
            CertificateMapping cm = new CertificateMapping();
            cm.setId(1);
            cm.setHostName("h1");
            cm.setPrivateKey("p1");
            cm.setCertificate("c1");
            cm.setIntermediateCertificate("ic1");
            CertificateMapping cm2 = new CertificateMapping();
            cm2.setId(2);
            cm2.setHostName("h2");
            cm2.setPrivateKey("p2");
            cm2.setCertificate("c2");
            cm2.setIntermediateCertificate("ic2");
            cms.add(cm);
            cms.add(cm2);

            lb.setCertificateMappings(cms);

            VirtualServer createdServer = translator.translateVirtualServerResource(config, secureName, lb);
            Assert.assertNotNull(createdServer);

            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerTcp createdTcp = createdProperties.getTcp();
            expectedTcp.setProxyClose(isHalfClosed);
            VirtualServerLog log = createdProperties.getLog();
            Boolean cacheEnabled = createdProperties.getWebCache().getEnabled();
            Assert.assertNotNull(log);
            Assert.assertEquals(logFormat, log.getFormat());
            Assert.assertTrue(cacheEnabled);
            Assert.assertEquals(vsName, createdBasic.getPool());
            Assert.assertTrue(createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            Assert.assertEquals(expectedTcp, createdTcp);
            Assert.assertFalse(createdBasic.getListenOnAny());
            if (lb.isContentCaching()) {
                rules.add(VTMConstants.CONTENT_CACHING);
            }
            Assert.assertEquals(rules.size(), createdBasic.getRequestRules().size());


            Assert.assertEquals(VTMTestConstants.LB_SECURE_PORT, (int) createdBasic.getPort());
            Assert.assertTrue(lb.getProtocol().toString().equalsIgnoreCase(createdBasic.getProtocol().toString()));
            Assert.assertEquals(isVsEnabled, createdBasic.getEnabled());
            Assert.assertEquals(vsName, createdBasic.getPool().toString());
            Assert.assertEquals(true, createdBasic.getSslDecrypt());
            Assert.assertEquals(VTMTestConstants.CIPHER_LIST, createdServer.getProperties().getSsl().getCipherSuites());
            Assert.assertEquals(VirtualServerSsl.SupportTls1.ENABLED, createdServer.getProperties().getSsl().getSupportTls1());
            Assert.assertEquals(VirtualServerSsl.SupportTls11.DISABLED, createdServer.getProperties().getSsl().getSupportTls11());

            String cname = lb.getId() + "_" + lb.getAccountId();
            List<VirtualServerServerCertHostMapping> vshm = new ArrayList<>();
            VirtualServerServerCertHostMapping vs1 = new VirtualServerServerCertHostMapping();
            vs1.setHost("h1");
            vs1.setCertificate(cname + "_1");
            vshm.add(vs1);
            VirtualServerServerCertHostMapping vs2 = new VirtualServerServerCertHostMapping();
            vs2.setHost("h2");
            vs2.setCertificate(cname + "_2");
            vshm.add(vs2);

            VirtualServerSsl vssl = createdServer.getProperties().getSsl();
            Assert.assertEquals(2, vssl.getServerCertHostMapping().size());
            Assert.assertTrue(vssl.getServerCertHostMapping().containsAll(vshm));
        }


        @Test
        public void shouldTranslateKeyPairCertMappings() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);

            Set<CertificateMapping> cms = new HashSet<>();
            CertificateMapping cm = new CertificateMapping();
            cm.setId(1);
            cm.setHostName("h1");
            cm.setPrivateKey(VTMTestConstants.SSL_KEY);
            cm.setCertificate(VTMTestConstants.SSL_CERT);
            cms.add(cm);

            lb.setCertificateMappings(cms);

            Map<String, Keypair> translatedMappings = translator.translateKeypairMappingsResource(lb, true);
            Assert.assertNotNull(translatedMappings);

            String cname = lb.getId() + "_" + lb.getAccountId() + "_1";

            Keypair m1 = translatedMappings.get(cname);
            Assert.assertNotNull(m1);
            Assert.assertEquals(VTMTestConstants.SSL_KEY, m1.getProperties().getBasic().getPrivate());
            Assert.assertEquals(VTMTestConstants.SSL_CERT, m1.getProperties().getBasic().getPublic());

        }

        @Test
        public void shouldTranslateKeyPairCertMappingsMultiple() throws InsufficientRequestException {
            initializeVars("%v %{Host}i %h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\" %n", LoadBalancerProtocol.HTTP);

            Set<CertificateMapping> cms = new HashSet<>();
            CertificateMapping cm = new CertificateMapping();
            cm.setId(1);
            cm.setHostName("h1");
            cm.setPrivateKey(VTMTestConstants.SSL_KEY);
            cm.setCertificate(VTMTestConstants.SSL_CERT);

            CertificateMapping cm2 = new CertificateMapping();
            cm2.setId(2);
            cm2.setHostName("h2");
            cm2.setPrivateKey(VTMTestConstants.SSL_KEY);
            cm2.setCertificate(VTMTestConstants.SSL_CERT);
            cms.add(cm);
            cms.add(cm2);

            lb.setCertificateMappings(cms);

            Map<String, Keypair> translatedMappings = translator.translateKeypairMappingsResource(lb, true);
            Assert.assertNotNull(translatedMappings);

            String cname = lb.getId() + "_" + lb.getAccountId() + "_1";
            String cname2 = lb.getId() + "_" + lb.getAccountId() + "_2";

            Keypair m1 = translatedMappings.get(cname);
            Assert.assertNotNull(m1);
            Assert.assertEquals(VTMTestConstants.SSL_KEY, m1.getProperties().getBasic().getPrivate());
            Assert.assertEquals(VTMTestConstants.SSL_CERT, m1.getProperties().getBasic().getPublic());

            Keypair m2 = translatedMappings.get(cname2);
            Assert.assertNotNull(m2);
            Assert.assertEquals(VTMTestConstants.SSL_KEY, m1.getProperties().getBasic().getPrivate());
            Assert.assertEquals(VTMTestConstants.SSL_CERT, m1.getProperties().getBasic().getPublic());

        }

        @Test
        public void checkAlternateValidPaths() throws InsufficientRequestException {
            initializeVars("%v %t %h %A:%p %n %B %b %T", LoadBalancerProtocol.FTP, "HI");
            VirtualServer createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            VirtualServerProperties createdProperties = createdServer.getProperties();
            VirtualServerBasic createdBasic = createdServer.getProperties().getBasic();
            VirtualServerLog log = createdProperties.getLog();
            Assert.assertNotNull(createdServer.getProperties().getLog());
            Assert.assertEquals(logFormat, log.getFormat());
//            Assert.assertEquals(expectedError, createdProperties.getConnectionErrors());
            Assert.assertEquals(vsName, createdBasic.getProtectionClass());
            pathOne();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals("", createdServer.getProperties().getBasic().getProtectionClass());
            pathTwo();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals(vsName, createdServer.getProperties().getBasic().getProtectionClass());
            pathThree();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals(vsName, createdServer.getProperties().getBasic().getProtectionClass());
            pathFour();
            createdServer = translator.translateVirtualServerResource(config, vsName, lb);
            Assert.assertEquals("", createdServer.getProperties().getBasic().getProtectionClass());

        }
    }

    public static class whenTranslatingATrafficIpGroup {

        private String expectedGroupName6;
        private String expectedGroupName4;
        private String failoverHost;
        private String ipAddress4;
        private String ipAddress6;
        private String expectedVip6Ip;

        private VTMResourceTranslator translator;
        @Mock
        private LoadBalancerEndpointConfiguration config;

        @Before
        public void standUp() throws IPStringConversionException {
            MockitoAnnotations.initMocks(this);
            setupIvars();
            int acctId = 1234567890;
            int ipv4Id = 1111;
            int ipv6Id = 2222;
            int ipv6Octet = 1;
            ipAddress4 = "000.000.000.000";
            ipAddress6 = "fe80::a00:27ff:fe05:d0d5/64";

            Set<LoadBalancerJoinVip> vip4s = new HashSet<LoadBalancerJoinVip>();
            LoadBalancerJoinVip vip4 = new LoadBalancerJoinVip();
            VirtualIp ip4 = new VirtualIp();
            ip4.setId(ipv4Id);
            ip4.setIpAddress(ipAddress4);
            vip4.setVirtualIp(ip4);
            vip4s.add(vip4);
            lb.setLoadBalancerJoinVipSet(vip4s);

            Set<LoadBalancerJoinVip6> vip6s = new HashSet<LoadBalancerJoinVip6>();
            LoadBalancerJoinVip6 vip6 = new LoadBalancerJoinVip6();
            VirtualIpv6 ip6 = new VirtualIpv6();
            ip6.setAccountId(acctId);
            ip6.setId(ipv6Id);
            ip6.setVipOctets(ipv6Octet);
            vip6.setVirtualIp(ip6);
            Cluster cluster = new Cluster();
            cluster.setClusterIpv6Cidr(ipAddress6);
            ip6.setCluster(cluster);
            vip6s.add(vip6);
            expectedVip6Ip = ip6.getDerivedIpString();
            lb.setLoadBalancerJoinVip6Set(vip6s);

            //found in /etc/openstack/atlas/
            failoverHost = "development.lbaas.rackspace.net";

            //traffic group name Lb ID _ VIP ID
            expectedGroupName6 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip6.getId());
            expectedGroupName4 = Integer.toString(TEST_ACCOUNT_ID) + "_" + Integer.toString(ip4.getId());
            List<String> failoverHosts = new ArrayList<String>();
            failoverHosts.add(failoverHost);
            when(config.getFailoverTrafficManagerNames()).thenReturn(failoverHosts);
            Host tmHost = new Host();
            tmHost.setCluster(cluster);
            when(config.getTrafficManagerHost()).thenReturn(tmHost);
        }

        @Test
        public void shouldCreateValidTrafficIpGroup() throws IPStringConversionException, InsufficientRequestException {
            translator = new VTMResourceTranslator();
            Map<String, TrafficIp> mappedGroups = translator.translateTrafficIpGroupsResource(config, lb, true);

            Assert.assertTrue(mappedGroups.containsKey(expectedGroupName4));
            Assert.assertTrue(mappedGroups.containsKey(expectedGroupName6));

            TrafficIp retrievedTrafficIp4 = mappedGroups.get(expectedGroupName4);
            TrafficIp retrievedTrafficIp6 = mappedGroups.get(expectedGroupName6);

            TrafficIpBasic basic4 = retrievedTrafficIp4.getProperties().getBasic();
            TrafficIpBasic basic6 = retrievedTrafficIp6.getProperties().getBasic();

            Assert.assertTrue(basic4.getIpaddresses().contains(ipAddress4));
            Assert.assertTrue(basic4.getMachines().contains(failoverHost));
            Assert.assertTrue(basic4.getEnabled());

            Assert.assertTrue(basic6.getMachines().contains(failoverHost));
            Assert.assertTrue(basic6.getIpaddresses().contains(expectedVip6Ip));
            Assert.assertTrue(basic6.getEnabled());
        }


    }

    public static class whenTranslatingAPool {

        private HealthMonitor healthMonitor;
        private int numAttemptsCheck;
        private String vsName;
        private int expectedTimeout;
        private Node nodeEnabled;
        private Node nodeDraining;
        private Node nodeDisabled;
        private String nodeEnabledAddress;
        private String nodeDrainingAddress;
        private String nodeDisabledAddress;
        private int nodePort;
        private int nodeEnabledWeight;
        private int nodeDrainingWeight;
        private int nodeDisabledWeight;
        private ZeusNodePriorityContainer container;
        private String nodeEnabledName;
        private String nodeDisabledName;
        private String nodeDrainingName;


        public void standUp(LoadBalancerAlgorithm algorithm) {
            setupIvars();
            vsName = "qwertyuiop";
            numAttemptsCheck = 90;
            expectedTimeout = 132;
            nodeEnabledAddress = "10.1.1.1";
            nodeDrainingAddress = "10.1.1.2";
            nodeDisabledAddress = "10.1.1.3";
            nodeEnabledWeight = 1;
            nodeDrainingWeight = 2;
            nodeDisabledWeight = 3;
            nodePort = 1107;
            healthMonitor = new HealthMonitor();
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setAlgorithm(algorithm);
            lb.setHealthMonitor(healthMonitor);
            lb.setSessionPersistence(SessionPersistence.HTTP_COOKIE);
            lb.setTimeout(expectedTimeout);
            Set<Node> nodes = new HashSet<Node>();
            nodeEnabled = new Node();
            nodeDraining = new Node();
            nodeDisabled = new Node();
            nodeEnabled.setCondition(NodeCondition.ENABLED);
            nodeDraining.setCondition(NodeCondition.DRAINING);
            nodeDisabled.setCondition(NodeCondition.DISABLED);
            nodeEnabled.setIpAddress(nodeEnabledAddress);
            nodeDraining.setIpAddress(nodeDrainingAddress);
            nodeDisabled.setIpAddress(nodeDisabledAddress);
            nodeEnabled.setWeight(nodeEnabledWeight);
            nodeDraining.setWeight(nodeDrainingWeight);
            nodeDisabled.setWeight(nodeDisabledWeight);
            nodeEnabled.setPort(nodePort);
            nodeDraining.setPort(nodePort);
            nodeDisabled.setPort(nodePort);
            nodes.add(nodeEnabled);
            nodes.add(nodeDraining);
            nodes.add(nodeDisabled);

            nodeEnabledName = nodeEnabledAddress + ":" + nodePort;
            nodeDisabledName = nodeDisabledAddress + ":" + nodePort;
            nodeDrainingName = nodeDrainingAddress + ":" + nodePort;


            lb.setNodes(nodes);
            container = new ZeusNodePriorityContainer(lb.getNodes());


        }

        public void tearDown() {
            lb.setHealthMonitor(null);
            lb.setSessionPersistence(null);
            lb.setAlgorithm(LoadBalancerAlgorithm.LEAST_CONNECTIONS);

        }


        @Test
        public void shouldCreateAValidPool() throws InsufficientRequestException {
            VTMResourceTranslator translator = new VTMResourceTranslator();
            standUp(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
            Pool createdPool = translator.translatePoolResource(vsName, lb, lb);
            Assert.assertNotNull(createdPool);
            PoolProperties createdProperties = createdPool.getProperties();
            Assert.assertNotNull(createdProperties);
            PoolBasic createdBasic = createdPool.getProperties().getBasic();
            Assert.assertTrue(createdBasic.getMonitors().contains(vsName));
            Assert.assertFalse(createdBasic.getPassiveMonitoring());
            Assert.assertEquals(SessionPersistence.HTTP_COOKIE.toString(), createdBasic.getPersistenceClass());

//            Assert.assertTrue(createdBasic.getNodes().contains(nodeEnabledName));
//            Assert.assertTrue(createdBasic.getDraining().contains(nodeDrainingName));
//            Assert.assertTrue(createdBasic.getDisabled().contains(nodeDisabledName));

            List<PoolNodesTable> nodesTables = createdBasic.getNodesTable();
            Assert.assertEquals(3, nodesTables.size());

//            Assert.assertEquals(expectedTimeout, (int) createdProperties.getConnection().getMaxReplyTime());
//            Assert.assertEquals(container.getPriorityValuesSet(), createdProperties.getLoadBalancing().getPriorityValues());
//            Assert.assertEquals(container.hasSecondary(), createdProperties.getLoadBalancing().getPriorityEnabled());
//            Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN.toString().toLowerCase(), createdProperties.getLoadBalancing().getAlgorithm());
//            List<PoolNodeWeight> weights = createdProperties.getLoadBalancing().getNodeWeighting();
//            Assert.assertNotNull(weights);
//            Assert.assertTrue(weights.contains(poolNodeEnabledWeight));
//            Assert.assertTrue(weights.contains(poolNodeDrainingWeight));
//            Assert.assertTrue(weights.contains(poolNodeDisabledWeight));

        }

        @Test
        public void testAlternatePaths() throws InsufficientRequestException {
//            VTMResourceTranslator translator = new VTMResourceTranslator();
//            standUp(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS);
//            Pool createdPool = translator.translatePoolResource(vsName, lb, lb);
//            PoolProperties createdProperties = createdPool.getProperties();
//            Assert.assertNotNull(createdProperties);
//            Assert.assertEquals(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS.toString().toLowerCase(), createdProperties.getLoadBalancing().getAlgorithm());
//            List<PoolNodeWeight> weights = createdProperties.getLoadBalancing().getNodeWeighting();
//            Assert.assertNotNull(weights);
//            Assert.assertTrue(weights.contains(poolNodeEnabledWeight));
//            Assert.assertTrue(weights.contains(poolNodeDrainingWeight));
//            Assert.assertTrue(weights.contains(poolNodeDisabledWeight));
//            standUp(LoadBalancerAlgorithm.RANDOM);
//            createdPool = translator.translatePoolResource(vsName, lb, lb);
//            Assert.assertTrue(createdPool.getProperties().getLoadBalancing().getNodeWeighting().size() == 0);
//            tearDown();
//            createdPool = translator.translatePoolResource(vsName, lb, lb);
//            createdProperties = createdPool.getProperties();
//            PoolBasic createdBasic = createdProperties.getBasic();
//            Assert.assertEquals("", createdBasic.getPersistenceClass());
//            Assert.assertTrue(createdBasic.getMonitors().size() == 0);

        }


    }

    public static class whenTranslatingAHealthMonitor {

        private HealthMonitor healthMonitor;
        private HealthMonitorType monitorType;
        private Integer numAttemptsCheck;
        private Integer delay;
        private Integer timeout;
        private String hostHeader;
        private String path;
        private String bodyRegex;
        private String statusRegex;
        private VTMResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
//            numAttemptsCheck = 90;
//            delay = 30;
//            timeout = 20;
//            hostHeader = "host123";
//            path = "path123";
//            bodyRegex = "br123";
//            statusRegex = "sr123";
//            useSsl = true; //This is set automatically on the LoadBalancer object when type is HTTPS
//            healthMonitor = new HealthMonitor();
//            healthMonitor.setType(monitorType);
//            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
//            healthMonitor.setDelay(delay);
//            healthMonitor.setTimeout(timeout);
//            healthMonitor.setHostHeader(hostHeader);
//            healthMonitor.setPath(path);
//            healthMonitor.setBodyRegex(bodyRegex);
//            healthMonitor.setStatusRegex(statusRegex);
//            lb.setHealthMonitor(healthMonitor);
        }

        @Test
        public void shouldCreateAValidHealthMonitor() throws InsufficientRequestException {
            translator = new VTMResourceTranslator();
            Monitor createdMonitor;
            MonitorProperties createdProperties;
            MonitorBasic createdBasic;
            MonitorHttp createdHttp;

            // Test MonitorType HTTPS
            monitorType = HealthMonitorType.HTTPS;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(MonitorBasic.Type.fromValue(HealthMonitorType.HTTP.toString().toLowerCase()), createdBasic.getType()); //The REST API does not use HTTPS as a type
            Assert.assertTrue(createdBasic.getUseSsl());

            // Test MonitorType HTTP
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(MonitorBasic.Type.fromValue(monitorType.toString().toLowerCase()), createdBasic.getType());
            Assert.assertFalse(createdBasic.getUseSsl());

            // Test MonitorType CONNECT
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(MonitorBasic.Type.fromValue(monitorType.toString().toLowerCase()), createdBasic.getType());

            // Test Number of Attempts (type doesn't matter)
            numAttemptsCheck = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getFailures(), numAttemptsCheck);

            numAttemptsCheck = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setAttemptsBeforeDeactivation(numAttemptsCheck);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getFailures(), numAttemptsCheck);

            // Test Delay (type doesn't matter)
            delay = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setDelay(delay);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getDelay(), delay);

            delay = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setDelay(delay);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getDelay(), delay);

            // Test Timeout (type doesn't matter)
            timeout = 20;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setTimeout(timeout);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getTimeout(), timeout);

            timeout = 30;
            monitorType = HealthMonitorType.CONNECT;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setTimeout(timeout);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getTimeout(), timeout);

            // Test Host Header (must be HTTP or HTTPS)
            hostHeader = "host123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setHostHeader(hostHeader);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getHostHeader(), hostHeader);

            hostHeader = "host456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setHostHeader(hostHeader);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getHostHeader(), hostHeader);

            // Test Path (must be HTTP or HTTPS)
            path = "path123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setPath(path);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getPath(), path);

            path = "path456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setPath(path);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getPath(), path);

            // Test Body Regex (must be HTTP or HTTPS)
            bodyRegex = "br123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setBodyRegex(bodyRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getBodyRegex(), bodyRegex);

            bodyRegex = "br456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setBodyRegex(bodyRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getBodyRegex(), bodyRegex);

            // Test Status Regex (must be HTTP or HTTPS)
            statusRegex = "sr123";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setStatusRegex(statusRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getStatusRegex(), statusRegex);

            statusRegex = "sr456";
            monitorType = HealthMonitorType.HTTP;
            healthMonitor = new HealthMonitor();
            healthMonitor.setType(monitorType);
            healthMonitor.setStatusRegex(statusRegex);
            lb.setHealthMonitor(healthMonitor);

            createdMonitor = translator.translateMonitorResource(lb);
            createdProperties = createdMonitor.getProperties();
            createdBasic = createdProperties.getBasic();
            createdHttp = createdProperties.getHttp();

            Assert.assertNotNull(createdMonitor);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertNotNull(createdHttp);
            Assert.assertEquals(createdHttp.getStatusRegex(), statusRegex);
        }

    }

    public static class whenTranslatingABandwidthResource {

        private RateLimit rateLimit;
        public Integer maxRequestsPerSecond;
        private String comment;
        private VTMResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
            comment = "This is a comment.";
            Ticket ticket = new Ticket();
            ticket.setComment(comment);
            rateLimit = new RateLimit();
            rateLimit.setMaxRequestsPerSecond(maxRequestsPerSecond);
            rateLimit.setTicket(ticket);

            lb.setRateLimit(rateLimit);
        }

        @Test
        public void shouldCreateAValidBandwidth() throws InsufficientRequestException {
            translator = new VTMResourceTranslator();
            Bandwidth createdBandwidth = translator.translateBandwidthResource(lb);
            BandwidthProperties createdProperties = createdBandwidth.getProperties();
            BandwidthBasic createdBasic = createdProperties.getBasic();

            Assert.assertNotNull(createdBandwidth);
            Assert.assertNotNull(createdProperties);
            Assert.assertNotNull(createdBasic);
            Assert.assertEquals(createdBasic.getMaximum(), maxRequestsPerSecond);
            Assert.assertEquals(createdBasic.getNote(), comment);
        }
    }

    public static class whenTranslatingAProtection {

        String vsName;
        int maxConnections;
        int maxRateInterval;
        int minConnections;
        int rateTiming;
        String ipAddressAllowed;
        String ipAddressBanned;
        ConnectionLimit connectionLimit;
        AccessList accessListAllowed;
        AccessList accessListBanned;


        @Before
        public void standUp() {
            setupIvars();


            vsName = "HI";

            maxConnections = 90;
            minConnections = 35;
            rateTiming = 78;
            maxRateInterval = 50;

            ipAddressAllowed = "10.1.1.4";
            ipAddressBanned = "10.1.1.5";

            connectionLimit = new ConnectionLimit();
            connectionLimit.setMaxConnections(maxConnections);
            connectionLimit.setMinConnections(minConnections);
            connectionLimit.setRateInterval(rateTiming);
            connectionLimit.setMaxConnectionRate(maxRateInterval);

            accessListAllowed = new AccessList();
            accessListAllowed.setIpAddress(ipAddressAllowed);
            accessListAllowed.setType(AccessListType.ALLOW);

            accessListBanned = new AccessList();
            accessListBanned.setIpAddress(ipAddressBanned);
            accessListBanned.setType(AccessListType.DENY);

            Set<AccessList> accessListSet = new HashSet<AccessList>();
            accessListSet.add(accessListAllowed);
            accessListSet.add(accessListBanned);

            lb.setConnectionLimit(connectionLimit);
            lb.setAccessLists(accessListSet);


        }

        @Test
        public void shouldCreateAValidProtection() {
            VTMResourceTranslator translator = new VTMResourceTranslator();
            Protection createdProtection = translator.translateProtectionResource(lb);
            Assert.assertNotNull(createdProtection.getProperties().getConnectionRate());
            Assert.assertEquals(maxConnections, (int) createdProtection.getProperties().getConcurrentConnections().getMax1Connections());
            /* Due to a Zeus bug, this all has to be ignored and set to static values */
            // Attempting to verify behavior. Set back to what we want and revist TODO:

            Assert.assertFalse(createdProtection.getProperties().getConcurrentConnections().getPerProcessConnectionCount());
            Assert.assertEquals(0, (int) createdProtection.getProperties().getConnectionRate().getMaxConnectionRate());
            Assert.assertEquals(0, (int) createdProtection.getProperties().getConcurrentConnections().getMinConnections());
            Assert.assertEquals(1, (int) createdProtection.getProperties().getConnectionRate().getRateTimer());
            ProtectionAccessRestriction createdRestriction = createdProtection.getProperties().getAccessRestriction();
            Assert.assertNotNull(createdRestriction);
            Assert.assertTrue(createdRestriction.getAllowed().contains(accessListAllowed.getIpAddress()));
            Assert.assertTrue(createdRestriction.getBanned().contains(accessListBanned.getIpAddress()));
        }
    }


    public static class whenGenGroupNameSet {

        private VTMResourceTranslator translator;


        @Test
        public void shouldGenGroupNameSet() throws InsufficientRequestException {
            translator = new VTMResourceTranslator();
            Set<String> groupNameSet = translator.genGroupNameSet(lb);
            Assert.assertFalse(groupNameSet.isEmpty());
            //Should probably verify if they are generated correctly..
        }
    }

    public static class whenTranslatingAPersistenceResource {

        private String vsName;
        private PersistenceType persistenceType;
        private VTMResourceTranslator translator;

        @Before
        public void standUp() {
            setupIvars();
            vsName = "asdfgh";
            persistenceType = PersistenceType.HTTP_COOKIE;
            SessionPersistence
                    persistence = SessionPersistence.fromDataType(persistenceType);
            lb.setSessionPersistence(persistence);
        }

        @Test
        public void test() {
            //gotta initialize test or tests break
        }
    }
}
