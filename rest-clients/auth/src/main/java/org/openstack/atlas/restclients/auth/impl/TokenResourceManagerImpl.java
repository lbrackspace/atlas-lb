package org.openstack.atlas.restclients.auth.impl;

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.TokenResourceManager;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;
import org.openstack.identity.client.endpoints.EndpointList;
import org.openstack.identity.client.token.AuthenticateResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

public class TokenResourceManagerImpl extends ResponseManagerImpl implements TokenResourceManager {


    /**
     * Validate token with admin account
     *
     * @param client
     * @param url
     * @param token
     * @param username
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    @Override
    public AuthenticateResponse validateToken(Client client, String url, String adminToken, String token, String username) throws IdentityFault, URISyntaxException {
       Response response = null;
        MultivaluedStringMap params = new MultivaluedStringMap();
        params.add(IdentityConstants.BELONGS_TO, username);
        try {
            response = get(client, new URI(url + IdentityConstants.TOKEN_PATH + "/" + token), adminToken, params);
        } catch (ResponseProcessingException ux) {
            throw IdentityResponseWrapper.buildFaultMessage(ux.getResponse());
        }

        if (!isResponseValid(response)) {
            handleBadResponse(response);
        }

        return response.readEntity(AuthenticateResponse.class);
    }
}
