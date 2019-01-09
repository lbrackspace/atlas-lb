package org.openstack.atlas.restclients.auth.impl;

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.RolesResourceManager;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;
import org.openstack.identity.client.roles.ObjectFactory;
import org.openstack.identity.client.roles.Role;
import org.openstack.identity.client.roles.RoleList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.URI;
import java.net.URISyntaxException;

public class RolesResourceManagerImpl extends ResponseManagerImpl implements RolesResourceManager {

    /**
     * List global roles for user by userId
     *
     * @param client
     * @param url
     * @param token
     * @param userId
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    @Override
    public RoleList listUserGlobalRoles(Client client, String url, String token, String userId) throws IdentityFault, URISyntaxException {
        Response response = null;
        try {
            response = get(client, new URI(url + IdentityConstants.USER_PATH + "/" + userId + "/" + IdentityConstants.ROLES_PATH), token);
        } catch (ResponseProcessingException ux) {
            throw IdentityResponseWrapper.buildFaultMessage(ux.getResponse());
        }

        if (!isResponseValid(response)) {
            handleBadResponse(response);
        }

        return response.readEntity(RoleList.class);
    }
}
