package org.openstack.atlas.api.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.ClassPathResource;
import javax.xml.validation.SchemaFactory;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.bind.JAXBContext;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.openstack.atlas.util.debug.Debug;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstack.atlas.api.filters.helpers.XmlValidationExceptionHandler;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.xml.sax.SAXException;

public class XmlValidationFilterTest {

    private static final String FPKG = "org.openstack.atlas.docs.loadbalancers.api.v1.faults";
    private static final String PPKG = "org.openstack.atlas.docs.loadbalancers.api.v1";
    private static final String FXSD = "META-INF/xsd/LoadBalancerApiFaults.xsd";
    private static final String PXSD = "META-INF/xsd/LoadBalancerApi.xsd";
    XmlValidationExceptionHandler errHandler;
    private JAXBContext pCtx;
    private JAXBContext fCtx;
    private Schema pSchema;
    private Schema fSchema;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws JAXBException, IOException, SAXException {
        pCtx = JAXBContext.newInstance(PPKG);
        fCtx = JAXBContext.newInstance(FPKG);
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        pSchema = sf.newSchema((new ClassPathResource(PXSD)).getURL());
        fSchema = sf.newSchema((new ClassPathResource(FXSD)).getURL());
        errHandler = new XmlValidationExceptionHandler();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void shouldNotThrowExceptionOnValidLoadBalancer() {

        String xml = ""
                + "<loadBalancer xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\"\n"
                + "    name=\"a-new-loadbalancer\"\n"
                + "    port=\"80\"\n"
                + "    protocol=\"HTTP\">\n"
                + "    <virtualIps>\n"
                + "        <virtualIp type=\"PUBLIC\"/>\n"
                + "    </virtualIps>\n"
                + "    <nodes>\n"
                + "        <node address=\"10.1.1.1\" port=\"80\" condition=\"ENABLED\"/>\n"
                + "    </nodes>\n"
                + "</loadBalancer>\n"
                + "\n"
                + "";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof LoadBalancer);
    }

    @Test
    public void shouldNotThrowExceptionOnValidNodes() {
        String xml = ""
                + "<nodes xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\">\n"
                + "    <node address=\"10.1.1.1\" port=\"80\" condition=\"ENABLED\" type=\"PRIMARY\"/>\n"
                + "    <node address=\"10.2.2.1\" port=\"80\" condition=\"ENABLED\" type=\"SECONDARY\"/>\n"
                + "    <node address=\"www.myrackspace.com\" port=\"88\" condition=\"ENABLED\" type=\"SECONDARY\" weight=\"10\"/>\n"
                + "</nodes>\n"
                + "\n"
                + "";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof Nodes);
    }

    @Test
    public void shouldNotThrowExceptionOnValidIPs() {
        String xml = "<virtualIp xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\" type=\"PUBLIC\" ipVersion=\"IPV6\" />";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof VirtualIp);
    }

    public void shouldNotThrowExceptionOnValidMonitor() {
        String xml = "<healthMonitor xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\""
                + "type=\"CONNECT\" delay=\"10\" timeout=\"10\" "
                + "attemptsBeforeDeactivation=\"3\" />";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof HealthMonitor);
    }

    @Test
    public void shouldNotThrowExceptionOnAccessList() {
        String xml = ""
                + "<accessList xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\">\n"
                + "    <networkItem\n"
                + "        id=\"1000\"\n"
                + "        address=\"206.160.165.40\"\n"
                + "        type=\"ALLOW\" />\n"
                + "    <networkItem\n"
                + "        id=\"1001\"\n"
                + "        address=\"206.160.165.0/24\"\n"
                + "        type=\"DENY\" />\n"
                + "</accessList>\n"
                + "\n"
                + "";

        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof AccessList);
    }

    @Test
    public void shouldNotThrowExceptionOnValidSSLTerm() {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<sslTermination xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\" \n"
                + "enabled=\"true\" securePort=\"500\" \n"
                + "secureTrafficOnly=\"false\">\n"
                + "<privatekey>-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIBOwIBAAJBAKOHSQtbpmrdkEvqv/mSYVoAk4vi6JY6AwwB0guCxvydD3MwYCtg\n"
                + "HlYBCUO0/ob/ClnVcI891oxEJVE3hzLS1SECAwEAAQJAOBMm+A8YdOK/HVlFNUZ9\n"
                + "vYgfEEPh78m6y75AEZRpuatwMHgopRUj1qMQzkge4puSBwn3NfLrO3eGJpnwoMsC\n"
                + "wQIhAOtzzhQYz59Qj67O3tFz7xnzRJ7TDdt7XMK/Yssj+7b5AiEAscykeuw8EzcT\n"
                + "kADt8rgbldTqK8M/pz5NMdu/wqUwUWkCIQDk/E5Km0qo/VePwd9Pwrmh+kYdVNNg\n"
                + "RpyTRkCVodw3OQIgadMSxvMaYplydFCJT+ES0iAy7K8+kK19RsKzpQeq9ZkCIQDg\n"
                + "E9MQ6R7zQhGNLOyGB2I0Jz0khu4NDzi5IPGyrrwtPw==\n"
                + "-----END RSA PRIVATE KEY-----</privatekey>\n"
                + "<certificate>-----BEGIN CERTIFICATE-----\n"
                + "MIICFTCCAb+gAwIBAgIBATANBgkqhkiG9w0BAQUFADB5MRgwFgYDVQQDEw93d3cu\n"
                + "bm93aGVyZS5vcmcxETAPBgNVBAsTCEpva2Uga2V5MRowGAYDVQQKExFUZXN0IEtl\n"
                + "eSg1MTIgYml0KTEUMBIGA1UEBxMLU2FuIEFudG9uaW8xCzAJBgNVBAgTAlRYMQsw\n"
                + "CQYDVQQGEwJVUzAeFw0xMzA5MjgwMjIwMTBaFw0xMzEwMDYwMjIwMTBaMHkxGDAW\n"
                + "BgNVBAMTD3d3dy5ub3doZXJlLm9yZzERMA8GA1UECxMISm9rZSBrZXkxGjAYBgNV\n"
                + "BAoTEVRlc3QgS2V5KDUxMiBiaXQpMRQwEgYDVQQHEwtTYW4gQW50b25pbzELMAkG\n"
                + "A1UECBMCVFgxCzAJBgNVBAYTAlVTMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKOH\n"
                + "SQtbpmrdkEvqv/mSYVoAk4vi6JY6AwwB0guCxvydD3MwYCtgHlYBCUO0/ob/ClnV\n"
                + "cI891oxEJVE3hzLS1SECAwEAAaMyMDAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4E\n"
                + "FgQUKXRxYocmyGVc4aQBH37kYT7Yi9wwDQYJKoZIhvcNAQEFBQADQQCEbip462IS\n"
                + "cvoPmITr0cYs1Jh0AtoQX0to/22Mis4Q401eQDd9oi8QUWJ++Lr+CzOTsc7IZkJF\n"
                + "L4w3iAQnOXiW\n"
                + "-----END CERTIFICATE-----</certificate>\n"
                + "</sslTermination>\n"
                + "";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof SslTermination);

    }

    @Test
    public void shouldNotThrowExceptionOnValidMetadata() {
        String xml = ""
                + "<metadata xmlns=\"http://docs.openstack.org/loadbalancers/api/v1.0\">\n"
                + "    <meta key=\"color\">red</meta>\n"
                + "    <meta key=\"label\">web-load-balancer</meta>\n"
                + "</metadata>\n"
                + "";
        errHandler = new XmlValidationExceptionHandler();
        Object obj = null;
        try {
            obj = xml2pojo(xml);
        } catch (Exception ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            fail(excMsg);
        }
        assertFalse(errHandler.getErrList().size() > 0);
        assertTrue(obj instanceof Metadata);
    }

    private Object xml2pojo(String xml) throws JAXBException, UnsupportedEncodingException, IOException {
        return XmlValidationFilter.xml2pojo(xml, pCtx, pSchema, errHandler);
    }
}
