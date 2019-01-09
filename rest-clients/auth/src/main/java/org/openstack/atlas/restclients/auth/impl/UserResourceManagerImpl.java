package org.openstack.atlas.restclients.auth.impl;

import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.UserResourceManager;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;
import org.openstack.identity.client.user.UserList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

public class UserResourceManagerImpl extends ResponseManagerImpl implements UserResourceManager {

    /**
     * List tenant users
     *
     * @param client
     * @param url
     * @param token
     * @param tenantId
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    @Override
    public UserList listTenantUsers(Client client, String url, String token, String tenantId) throws IdentityFault, URISyntaxException {
        Response response = null;
        try {
            response = get(client, new URI(url + IdentityConstants.TENANT_PATH
                    + "/" + tenantId + "/" + IdentityConstants.USER_PATH), token);
        } catch (ResponseProcessingException ux) {
            throw IdentityResponseWrapper.buildFaultMessage(ux.getResponse());
        }

        if (!isResponseValid(response)) {
            handleBadResponse(response);
        }
        return response.readEntity(UserList.class);
    }
}
