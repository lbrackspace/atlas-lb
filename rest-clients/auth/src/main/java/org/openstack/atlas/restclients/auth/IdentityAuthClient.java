package org.openstack.atlas.restclients.auth;

import org.openstack.identity.client.fault.IdentityFault;
import org.openstack.identity.client.token.AuthenticateResponse;

import java.net.URISyntaxException;

public interface IdentityAuthClient {

    AuthenticateResponse getAuthResponse() throws IdentityFault, URISyntaxException;

    AuthenticateResponse getAuthResponse(String username, String password) throws IdentityFault, URISyntaxException;

    String getAuthToken() throws URISyntaxException, IdentityFault;

    String getAuthToken(String username, String password) throws URISyntaxException, IdentityFault;


}
