package org.openstack.atlas.restclients.auth.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.impl.*;
import org.openstack.atlas.restclients.auth.manager.*;
import org.openstack.identity.client.access.Access;
import org.openstack.identity.client.roles.RoleList;
import org.openstack.identity.client.tenant.Tenant;
import org.openstack.identity.client.token.AuthenticateResponse;
import org.openstack.identity.client.user.User;
import org.openstack.identity.client.user.UserList;

import javax.ws.rs.client.Client;
import javax.xml.bind.JAXBException;
import java.net.URISyntaxException;

public class IdentityClient extends IdentityManager {
    private final Log logger = LogFactory.getLog(IdentityClient.class);
    private AuthenticationResourceManager authenticationResourceManager = new AuthenticationResourceManagerImpl();
    private TokenResourceManager tokenResourceManager = new TokenResourceManagerImpl();
    private ImpersonationResourceManager impersonationResourceManager = new ImpersonationResourceManagerImpl();
    private UserResourceManager userResourceManager = new UserResourceManagerImpl();
    private RolesResourceManager rolesResourceManager = new RolesResourceManagerImpl();

    public IdentityClient(String authUrl, Client client) throws IdentityFault {
        super(authUrl, client, false);
    }

    public IdentityClient(String authUrl, Client client, boolean isDebugging) throws IdentityFault {
        super(authUrl, client, isDebugging);
    }

    public IdentityClient(String authUrl) throws IdentityFault {
        super(authUrl);
    }

    public IdentityClient(String authUrl, int timeout) throws IdentityFault {
        super(authUrl, timeout);
    }

    public IdentityClient(String authUrl, int timeout, boolean isDebugging) throws IdentityFault {
        super(authUrl, timeout, isDebugging);
    }

    public IdentityClient(String authUrl, boolean isDebugging) throws IdentityFault {
        super(authUrl, isDebugging);
    }

    public IdentityClient() throws IdentityFault {
        super();
    }

    public IdentityClient(boolean isDebugging) throws IdentityFault {
        super(isDebugging);
    }

    /* ******************************************************************************************************************/
    /*                                                 AUTHENTICATION                                                   */
    /* ******************************************************************************************************************/

    /**
     * Authenticate user with username and password
     *
     * @param username
     * @param password
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public AuthenticateResponse authenticateUsernamePassword(String username, String password) throws IdentityFault, URISyntaxException {
        return authenticateUsernamePassword(url, username, password);
    }

    /**
     * Authenticate user with specific url, username and password
     *
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public AuthenticateResponse authenticateUsernamePassword(String url, String username, String password) throws IdentityFault, URISyntaxException {
        return authenticationResourceManager.authenticateUsernamePassword(client, url, username, password);
    }

     /* ******************************************************************************************************************/
    /*                                                      TOKENS                                                      */
    /* ******************************************************************************************************************/

    /**
     * Validate token for tenantName
     *
     * @param token
     * @param tenantName
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public AuthenticateResponse validateToken(String token, String tenantName) throws IdentityFault, URISyntaxException {
        return validateToken(url, token, token, tenantName);
    }

    /**
     * Validate token for tenantName with admin account
     *
     * @param adminToken
     * @param token
     * @param tenantName
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public AuthenticateResponse validateToken(String adminToken, String token, String tenantName) throws IdentityFault, URISyntaxException {
        return validateToken(url, adminToken, token, tenantName);
    }


    /**
     * Validate token for tenantName with specific url with admin account
     *
     * @param url
     * @param adminToken
     * @param token
     * @param tenantName
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public AuthenticateResponse validateToken(String url, String adminToken, String token, String tenantName) throws IdentityFault, URISyntaxException {
        return tokenResourceManager.validateToken(client, url, adminToken, token, tenantName);
    }

     /* ******************************************************************************************************************/
    /*                                                IMPERSONATION                                                     */
    /* ******************************************************************************************************************/


    /**
     * Impersonate user by userName
     *
     * @param token
     * @param userName
     * @param expireInSeconds
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public Access impersonateUser(String token, String userName, int expireInSeconds) throws IdentityFault, URISyntaxException, JAXBException {
        return impersonateUser(url, token, userName, expireInSeconds);
    }

    /**
     * Impersonate user by userName with specif url
     *
     * @param url
     * @param token
     * @param userName
     * @param expireInSeconds
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public Access impersonateUser(String url, String token, String userName, int expireInSeconds) throws IdentityFault, URISyntaxException, JAXBException {
        return impersonationResourceManager.impersonateUser(client, url, token, userName, expireInSeconds);
    }


     /* ******************************************************************************************************************/
     /*                                                     USERS                                                       */
    /* ******************************************************************************************************************/


    /**
     * List tenant users
     *
     * @param token
     * @param tenantId
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public UserList listTenantUsers(String token, String tenantId) throws IdentityFault, URISyntaxException {
        return listTenantUsers(url, token, tenantId);
    }

    /**
     * List tenant users with specific url
     *
     * @param url
     * @param token
     * @param tenantId
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    public UserList listTenantUsers(String url, String token, String tenantId) throws IdentityFault, URISyntaxException {
        return userResourceManager.listTenantUsers(client, url, token, tenantId);
    }

    /* ******************************************************************************************************************/
     /*                                                     ROLES                                                       */
    /* ******************************************************************************************************************/


    /**
     * List global roles for userId
     *
     * @param token
     * @param userId
     * @return
     * @throws URISyntaxException
     * @throws IdentityFault
     */
    public RoleList listUserGlobalRoles(String token, String userId) throws URISyntaxException, IdentityFault {
        return listUserGlobalRoles(url, token, userId);
    }

    /**
     * List global roles for userId with specific url
     *
     * @param url
     * @param token
     * @param userId
     * @return
     * @throws URISyntaxException
     * @throws IdentityFault
     */
    public RoleList listUserGlobalRoles(String url, String token, String userId) throws URISyntaxException, IdentityFault {
        return rolesResourceManager.listUserGlobalRoles(client, url, token, userId);
    }

}

