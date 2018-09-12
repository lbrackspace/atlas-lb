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
    private Long port;
    private GroupConfig groupMap;
    private String oClass;
    private String memberField;
    private String userQuery;
    private String dn;
    private String sdn;

    private ClassConfig classMap;
    private String user_dn;
    private String user_sdn;
    private ClassPathResource cpf = new ClassPathResource("ldap-auth.xml");
    private XmlBeanFactory bf = new XmlBeanFactory(cpf);

    @Before
    public void setUp() throws GeneralSecurityException {

        host = "127.0.0.1";
        port = 389L;
        config = new MossoAuthConfig(host, port);

        dn = "ou=Users,o=rackspace";
        sdn = "cn";
        oClass = "(objectClass=*)";
        memberField = "groupMembership";
        userQuery = "cn=%s";

        groupMap = new GroupConfig(oClass, memberField, userQuery, dn, sdn);
        config.setGroupConfig(groupMap);

        user_dn = "ou=People,dc=stabletransit,dc=com";
        user_sdn = "cn";
        classMap = new ClassConfig(user_dn, user_sdn);
        config.setClassConfig(classMap);
        mossoAuth = new MossoAuth(config);

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
        GroupConfig mossoGroupMap;
        GroupConfig groups;

        mossoGroupMap = this.mossoAuth.getGroupMap();
        assertEquals(oClass, mossoGroupMap.getObjectClass());
        assertEquals(memberField, mossoGroupMap.getMemberField());
        assertEquals(userQuery, mossoGroupMap.getUserQuery());
        assertEquals(dn, mossoGroupMap.getDn());
        assertEquals(sdn, mossoGroupMap.getSdn());
    }

    @Test
    public void testClassMap() {
        ClassConfig mossoClassMap;
        ClassConfig usermap;

        mossoClassMap = this.mossoAuth.getClassMap();
        assertEquals(user_dn, mossoClassMap.getDn());
        assertEquals(user_sdn, mossoClassMap.getSdn());
    }

    private void nop() {
    }
}
