package org.openstack.atlas.restclients.dns.exp;

import org.openstack.atlas.restclients.dns.helpers.DnsMockObjectGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openstack.atlas.restclients.dns.pub.objects.Rdns;
import org.openstack.atlas.restclients.dns.pub.objects.Record;

import org.openstack.atlas.util.debug.Debug;

public class JaxbExpTest {

    private static final String xmlIn = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<ns2:rdns xmlns:ns2=\"http://docs.rackspacecloud.com/dns/api/v1.0\" \n"
            + "    xmlns=\"http://www.w3.org/2005/Atom\">\n"
            + "    <link rel=\"cloudLoadBalancers\" \n"
            + "        href=\"http://somedomain/somelb/blah/blah\"/>\n"
            + "        <ns2:recordsList>\n"
            + "            <ns2:record data=\"127.0.0.1\" name=\"www.home.org\" type=\"A\"/>\n"
            + "            <ns2:record data=\"10.0.0.1\" name=\"www.somedomain.org\" type=\"A\"/>\n"
            + "        </ns2:recordsList>\n"
            + "</ns2:rdns>\n"
            + "";

    public JaxbExpTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeserialize() throws Exception {
        Object obj;
        Rdns rdns;
        String exMsg;
        try {
            obj = JaxbExp.deserialize(Rdns.class, xmlIn);
        } catch (Exception ex) {
            exMsg = Debug.getEST(ex);
            fail("Could not deseialize\n" + exMsg);
            return;
        }
            rdns = (Rdns) obj;
            Record home = rdns.getRecordsList().getRecords().get(0);
            Record someDomain = rdns.getRecordsList().getRecords().get(1);
            assertEquals(home.getName(), "www.home.org");
            assertEquals(someDomain.getName(), "www.somedomain.org");
            assertEquals(home.getData(), "127.0.0.1");
            assertEquals(someDomain.getData(),"10.0.0.1");
    }

    @Test
    public void testSerialize() throws Exception {
        Rdns rdns = DnsMockObjectGenerator.newRdns();
        String xml;
        String exMsg;
        try {
            xml = JaxbExp.serialize(rdns);
        } catch (Exception ex) {
            exMsg = Debug.getEST(ex);
            fail("could not Serailize\n" + exMsg);
        }
    }

    @Test
    public void testStrToInputStream() throws Exception {
    }
}
