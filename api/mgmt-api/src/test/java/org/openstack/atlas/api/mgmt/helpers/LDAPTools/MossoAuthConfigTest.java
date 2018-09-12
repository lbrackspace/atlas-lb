package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.security.GeneralSecurityException;
import org.junit.Before;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MossoAuthConfigTest {

    private String uid;
    private String ctext;
    private String password;
    private String host;
    private int port = 636;

    public MossoAuthConfigTest() {
    }

    private static final String exampleJson = ""
            + "{\n"
            + "  \"groupConfig\": {\n"
            + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
            + "    \"memberField\": \"memberOf\", \n"
            + "    \"sdn\": \"cn\", \n"
            + "    \"userQuery\": \"(uid=%s)\", \n"
            + "    \"objectClass\": \"(objectClass=*)\"\n"
            + "  }, \n"
            + "  \"appendtoname\": \"@rackspace.corp\", \n"
            + "  \"roles\": {\n"
            + "    \"support\": \"lbaas_support\", \n"
            + "    \"cp\": \"lbaas_cloud_control\", \n"
            + "    \"billing\": \"legacy_billing\", \n"
            + "    \"ops\": \"lbaas_ops\"\n"
            + "  }, \n"
            + "  \"isactivedirectory\": true, \n"
            + "  \"userConfig\": {\n"
            + "    \"dn\": \"ou=Accounts,dc=rackspace,dc=corp\", \n"
            + "    \"sdn\": \"uid\"\n"
            + "  }, \n"
            + "  \"host\": \"ad.auth.rackspace.com\", \n"
            + "  \"connect\": \"ssl\", \n"
            + "  \"scope\": \"subtree\", \n"
            + "  \"port\": 636\n"
            + "}";

    @Before
    public void setUp() throws GeneralSecurityException {
        uid = "uid=support-password-reset,ou=special users,dc=stabletransit,dc=com";
        host = "ad.auth.rackspace.com";
    }

    @Test
    public void testMossoAuthConfigFromFile() throws IOException, GeneralSecurityException {
        String fileName = "/tmp/ldap.json";
        FileWriter fw = new FileWriter(fileName);
        PrintWriter pw = new PrintWriter(fw);
        pw.print(exampleJson);
        pw.close();
        fw.close();
        MossoAuthConfig tConf = new MossoAuthConfig(fileName);
        assertEquals(this.host, tConf.getHost());
        assertEquals(this.port, tConf.getPort());
        assertEquals("ou=Accounts,dc=rackspace,dc=corp", tConf.getGroupConfig().getDn());
        assertEquals("(objectClass=*)", tConf.getGroupConfig().getObjectClass());
        assertEquals("(uid=%s)", tConf.getGroupConfig().getUserQuery());
        assertEquals("cn", tConf.getGroupConfig().getSdn());
        assertEquals("memberOf", tConf.getGroupConfig().getMemberField());
        assertEquals("@rackspace.corp", tConf.getAppendName());
        assertEquals(true, tConf.isIsActiveDirectory());
        assertEquals(MossoAuthConfig.LDAPConnectMethod.SSL, tConf.getConnectMethod());
        assertEquals(2, tConf.getScope());
        assertEquals("ou=Accounts,dc=rackspace,dc=corp", tConf.getClassConfig().getDn());
        assertEquals("uid", tConf.getClassConfig().getSdn());
        assertEquals("ops", tConf.getRoles().get("lbaas_ops"));
        assertEquals("billing", tConf.getRoles().get("legacy_billing"));
        assertEquals("cp", tConf.getRoles().get("lbaas_cloud_control"));
        assertEquals("support", tConf.getRoles().get("lbaas_support"));

    }
}
