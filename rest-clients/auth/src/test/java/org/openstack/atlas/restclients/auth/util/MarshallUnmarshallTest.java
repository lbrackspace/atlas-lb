package org.openstack.atlas.restclients.auth.util;

import org.junit.Test;
import org.openstack.identity.client.credentials.AuthenticationRequest;
import org.openstack.identity.client.credentials.PasswordCredentialsRequiredUsername;
import org.openstack.identity.client.group.Group;
import org.openstack.identity.client.group.GroupList;

import javax.xml.bind.JAXBContext;

import static junit.framework.Assert.assertTrue;

public class MarshallUnmarshallTest {

    @Test
    public void shouldMarshallAuthRequest() throws Exception {
        org.openstack.identity.client.credentials.ObjectFactory factory = new org.openstack.identity.client.credentials.ObjectFactory();
        AuthenticationRequest areq = factory.createAuthenticationRequest();
        PasswordCredentialsRequiredUsername requiredUsername = new PasswordCredentialsRequiredUsername();
        requiredUsername.setUsername("bob");
        requiredUsername.setPassword("bobspass");
        areq.setCredential(factory.createPasswordCredentials(requiredUsername));

        String result = ResourceUtil.marshallResource(factory.createAuth(areq),
                JAXBContext.newInstance(AuthenticationRequest.class)).toString();

        // Could probably pull in xmlunit to better verify xml as there are issues with ordering that makes this a pain
        assertTrue(result.trim().contains("<?xml version=\"1.0\""));
        assertTrue(result.trim().contains("<auth"));
        assertTrue(result.trim().contains("xmlns=\"http://docs.openstack.org/identity/api/v2.0\""));
        assertTrue(result.trim().contains("<passwordCredentials"));
        assertTrue(result.trim().contains("username=\"bob\""));
        assertTrue(result.trim().contains("password=\"bobspass\""));
        assertTrue(result.trim().contains("</auth>"));
    }

    @Test
    public void shouldMarshallGroup() throws Exception {
        org.openstack.identity.client.group.ObjectFactory factory = new org.openstack.identity.client.group.ObjectFactory();
        Group group = factory.createGroup();
        GroupList groupList = factory.createGroupList();
        group.setId("234");
        group.setName("bobBuilding");
        group.setDescription("desc");
        groupList.getGroup().add(group);

        String result = ResourceUtil.marshallResource(factory.createGroups(groupList),
                JAXBContext.newInstance(GroupList.class)).toString();

        // Could probably pull in xmlunit to better verify xml as there are issues with ordering that makes this a pain
        assertTrue(result.trim().contains("<?xml version=\"1.0\""));
        assertTrue(result.trim().contains("xmlns:ns2=\"http://www.w3.org/2005/Atom\""));
        assertTrue(result.trim().contains("xmlns=\"http://docs.rackspace.com/identity/api/ext/RAX-KSGRP/v1.0\""));
        assertTrue(result.trim().contains("<group"));
        assertTrue(result.trim().contains("name=\"bobBuilding\""));
        assertTrue(result.trim().contains("id=\"234\""));
        assertTrue(result.trim().contains("<description>desc"));
        assertTrue(result.trim().contains("</group"));
        assertTrue(result.trim().contains("</groups"));
    }
}

