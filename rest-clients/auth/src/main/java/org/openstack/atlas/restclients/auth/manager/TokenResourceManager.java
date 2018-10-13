package org.openstack.atlas.restclients.auth.manager;

import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.endpoints.EndpointList;
import org.openstack.identity.client.token.AuthenticateResponse;

import javax.ws.rs.client.Client;
import java.net.URISyntaxException;

public interface TokenResourceManager {

    AuthenticateResponse validateToken(Client client, String url, String adminToken, String token, String username) throws IdentityFault, URISyntaxException;

   }
