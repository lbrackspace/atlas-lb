package org.openstack.atlas.restclients.auth;


import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.access.Access;
import org.openstack.identity.client.roles.RoleList;
import org.openstack.identity.client.tenant.Tenant;
import org.openstack.identity.client.token.AuthenticateResponse;
import org.openstack.identity.client.user.User;
import org.openstack.identity.client.user.UserList;

import javax.ws.rs.client.Client;
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

    UserList getUsersByTenantId(String adminToken, String tenantId) throws URISyntaxException, IdentityFault, JAXBException;

    User getPrimaryUserForTenantId(String adminToken, String teantId) throws URISyntaxException, IdentityFault, JAXBException;

    RoleList listUserGlobalRoles(String token, String userId) throws IdentityFault, URISyntaxException, JAXBException;

}
