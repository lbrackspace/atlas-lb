package org.openstack.atlas.restclients.auth;


import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.access.Access;
import org.openstack.identity.client.token.AuthenticateResponse;

import javax.xml.bind.JAXBException;
import java.net.URISyntaxException;

public interface IdentityAuthClient {

    AuthenticateResponse getAuthResponse() throws IdentityFault, URISyntaxException;

    AuthenticateResponse getAuthResponse(String username, String password) throws IdentityFault, URISyntaxException;

    String getAuthToken() throws URISyntaxException, IdentityFault;

    String getAuthToken(String username, String password) throws URISyntaxException, IdentityFault;

    Access impersonateUser(String adminToken, String impUser, int expiresInSeconds) throws URISyntaxException, IdentityFault, JAXBException;

    Access impersonateUser(String adminToken, String impUser) throws URISyntaxException, IdentityFault, JAXBException;

    String getImpersonationToken(String adminToken, String impUser) throws URISyntaxException, IdentityFault, JAXBException;
}
