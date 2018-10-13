package org.openstack.atlas.restclients.auth.manager;


import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.token.AuthenticateResponse;

import javax.ws.rs.client.Client;
import java.net.URISyntaxException;

public interface AuthenticationResourceManager {

    AuthenticateResponse authenticateUsernamePassword(Client client, String url, String userName, String password) throws IdentityFault, URISyntaxException;
}
