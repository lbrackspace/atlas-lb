package org.openstack.atlas.api.mgmt.helpers.LDAPTools;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import java.security.GeneralSecurityException;
import java.io.IOException;

import org.junit.Ignore;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import javax.naming.NamingException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MossoAuthTest {
    private static final String username = "your.mother";
    private static final String passwd = "xxxxxxxxxxx";


    private MossoAuth mossoAuth;
    private MossoAuthConfig config;
    private String host;
    private int port;
    private Map<String, GroupConfig> groupMap;
    private String oClass;
    private String memberField;
    private String userQuery;
    private String dn;
    private String sdn;
    
    private Map<String, ClassConfig> classMap;
    private String user_dn;
    private String user_sdn;
    private ClassPathResource cpf = new ClassPathResource("ldap-auth.xml");
    private XmlBeanFactory bf = new XmlBeanFactory(cpf);

    @Before
    public void setUp() throws GeneralSecurityException {
        
        host = "127.0.0.1";
        port = 389;
        config = new MossoAuthConfig(host, port);

        dn = "ou=Users,o=rackspace";
        sdn = "cn";
        oClass = "(objectClass=*)";
        memberField = "groupMembership";
        userQuery = "cn=%s";

        groupMap = new HashMap<String, GroupConfig>();
        groupMap.put("groups", new GroupConfig(oClass, memberField, userQuery, dn, sdn));

        user_dn = "ou=People,dc=stabletransit,dc=com";
        user_sdn = "cn";
        classMap = new HashMap<String, ClassConfig>();
        classMap.put("user", new ClassConfig(user_dn, user_sdn));
        mossoAuth = new MossoAuth(config, groupMap, classMap);

        cpf = new ClassPathResource("ldap-auth.xml");
        bf = new XmlBeanFactory(cpf);
    }

    @Test
    public void testConfig() {
        MossoAuthConfig mossoConf = this.mossoAuth.getConfig();
        assertEquals(host, mossoConf.getHost());
        assertEquals(port, mossoConf.getPort());
    }

    @Test
    public void testGroupMap() {
        Map<String, GroupConfig> mossoGroupMap;
        GroupConfig groups;

        mossoGroupMap = this.mossoAuth.getGroupMap();
        assertTrue("expected ClassMap to contain posixgroups", mossoGroupMap.containsKey("groups"));
        groups = mossoGroupMap.get("groups");
        assertEquals(oClass, groups.getObjectClass());
        assertEquals(memberField, groups.getMemberField());
        assertEquals(userQuery, groups.getUserQuery());
        assertEquals(dn, groups.getDn());
        assertEquals(sdn, groups.getSdn());
    }

    @Test
    public void testClassMap() {
        Map<String, ClassConfig> mossoClassMap;
        ClassConfig usermap;

        mossoClassMap = this.mossoAuth.getClassMap();
        assertTrue("expected ClassConfig to contain user", mossoClassMap.containsKey("user"));
        usermap = mossoClassMap.get("user");
        assertEquals(user_dn, usermap.getDn());
        assertEquals(user_sdn, usermap.getSdn());
    }

    @Ignore
    @Test
    public void shouldListGroups() throws NamingException, IOException {
        MossoAuth auth = loadFromSpring();
        Set<String> groups;
        groups = auth.getGroups(username,passwd);
        nop();
    }

    @Ignore
    @Test
    public void shouldPasswordAuthenticate() {
        MossoAuth auth = loadFromSpring();
        boolean authenticated = auth.testAuth(username, passwd);
        assertTrue(authenticated);
    }

    @Ignore
    @Test
    public void testMossoConfigFromSpring() {
        MossoAuthConfig mossoAuthConfig = (MossoAuthConfig) bf.getBean("mossoAuthConfig");
        nop();
    }


    @Test
    public void testClassMapFromSpring() {
        Map<String, ClassConfig> mossoClassMap;
        mossoClassMap = (Map<String, ClassConfig>) bf.getBean("mossoClassMap");
        nop();
    }

    @Ignore
    @Test
    public void shouldLoadAllFromSpring() {
        MossoAuth springMossoAuth = loadFromSpring();
    }

    @Test
    public void testGroupMapFromSpring() {
        Map<String, GroupConfig> mossoGroupMap;
        mossoGroupMap = (Map<String, GroupConfig>) bf.getBean("mossoGroupMap");
        nop();
    }

    private String encryptPasswd(String passwd) throws GeneralSecurityException {
        return Aes.b64encrypt(passwd.getBytes(), "mossoFailure");
    }

    private String decryptPasswd(String base64str) throws GeneralSecurityException {
        byte[] ptext = Aes.b64decrypt(base64str, "mossoFailure");
        return Aes.bytes2str(ptext);
    }

    private MossoAuth loadFromSpring() {
        MossoAuth springMossoAuth;
        springMossoAuth = (MossoAuth) bf.getBean("mossoAuth");
        return springMossoAuth;
    }

    private void nop() {
    }
}
