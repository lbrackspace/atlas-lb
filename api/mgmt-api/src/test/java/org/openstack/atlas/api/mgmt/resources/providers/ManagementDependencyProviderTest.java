package org.openstack.atlas.api.mgmt.resources.providers;

import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openstack.atlas.api.mgmt.helpers.LDAPTools.MossoAuthConfig;
import org.openstack.atlas.api.resources.providers.RequestStateContainer;

import javax.ws.rs.core.HttpHeaders;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class ManagementDependencyProviderTest {
    public static class whenRetrievingAccountDetails {
        private ManagementDependencyProvider managementDependencyProvider;
        private RequestStateContainer requestStateContainer;
        private MossoAuthConfig mossoAuthConfig;
        private HttpHeaders httpHeaders;

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
                + "    \"support\": \"support_group\", \n"
                + "    \"dev\": \"dev_group\", \n"
                + "    \"user\": \"user_group\", \n"
                + "    \"ops\": \"ops_group,special_group\"\n"
                + "  }, \n"
                + "  \"isactivedirectory\": true, \n"
                + "  \"usehostverify\": true, \n"
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
        public void setUp() throws IOException, GeneralSecurityException {
            managementDependencyProvider = new ManagementDependencyProvider();
            requestStateContainer = mock(RequestStateContainer.class);
            mossoAuthConfig = mock(MossoAuthConfig.class);
            managementDependencyProvider.setRequestStateContainer(requestStateContainer);
            managementDependencyProvider.setMossoAuthConfig(mossoAuthConfig);
            httpHeaders = mock(HttpHeaders.class);

            String fileName = "/tmp/ldap.json";
            FileWriter fw = new FileWriter(fileName);
            PrintWriter pw = new PrintWriter(fw);
            pw.print(exampleJson);
            pw.close();
            fw.close();
            MossoAuthConfig tConf = new MossoAuthConfig(fileName);
            managementDependencyProvider.setMossoAuthConfig(tConf);

            List<String> groupList = new ArrayList<>();
            groupList.add("support_group");
            groupList.add("user_group");
            groupList.add("ops_group");
            groupList.add("dev_group");

            when(requestStateContainer.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("LDAPGroups")).thenReturn(groupList);
        }

        @Test
        public void shouldReturnLdapGroups() {
            Set<String> groups = managementDependencyProvider.getLDAPGroups();
            Assert.assertNotNull(groups);
            Assert.assertFalse(groups.isEmpty());
            Assert.assertEquals(4, groups.size());
            Assert.assertTrue(groups.contains("user_group"));
            Assert.assertTrue(groups.contains("ops_group"));
            Assert.assertTrue(groups.contains("dev_group"));
            Assert.assertTrue(groups.contains("support_group"));
        }

        @Test
        public void shouldReturnLdapGroupsWithoutDupes() {
            List<String> groupList = new ArrayList<>();
            groupList.add("user_group");
            groupList.add("ops_group");
            groupList.add("dev_group");
            groupList.add("dev_group");

            when(requestStateContainer.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("LDAPGroups")).thenReturn(groupList);

            Set<String> groups = managementDependencyProvider.getLDAPGroups();
            Assert.assertNotNull(groups);
            Assert.assertFalse(groups.isEmpty());
            Assert.assertEquals(3, groups.size());
            Assert.assertTrue(groups.contains("user_group"));
            Assert.assertTrue(groups.contains("ops_group"));
            Assert.assertTrue(groups.contains("dev_group"));
        }

        @Test
        public void shouldReturnMatchingRoles() {
            Set<String> roles = managementDependencyProvider.userRoles();
            Assert.assertNotNull(roles);
            Assert.assertFalse(roles.isEmpty());
            Assert.assertEquals(4, roles.size());
            Assert.assertTrue(roles.contains("user"));
            Assert.assertTrue(roles.contains("ops"));
            Assert.assertTrue(roles.contains("dev"));
            Assert.assertTrue(roles.contains("support"));
        }

        @Test
        public void shouldReturnOneMatchingRole() {
            List<String> groupList = new ArrayList<>();
            groupList.add("special_group");

            when(requestStateContainer.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("LDAPGroups")).thenReturn(groupList);

            Set<String> roles = managementDependencyProvider.userRoles();
            Assert.assertNotNull(roles);
            Assert.assertFalse(roles.isEmpty());
            Assert.assertEquals(1, roles.size());
            Assert.assertTrue(roles.contains("ops"));
        }

        @Test
        public void shouldReturnZeroMatchingRoles() {
            List<String> groupList = new ArrayList<>();

            when(requestStateContainer.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("LDAPGroups")).thenReturn(groupList);

            Set<String> roles = managementDependencyProvider.userRoles();
            Assert.assertNotNull(roles);
            Assert.assertTrue(roles.isEmpty());
        }
    }
}
