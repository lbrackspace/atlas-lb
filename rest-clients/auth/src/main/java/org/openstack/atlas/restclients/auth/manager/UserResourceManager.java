package org.openstack.atlas.restclients.auth.manager;

import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.user.User;
import org.openstack.identity.client.user.UserList;

import javax.ws.rs.client.Client;
import java.net.URISyntaxException;

public interface UserResourceManager {

    UserList listTenantUsers(Client client, String url, String token, String tenantId) throws IdentityFault, URISyntaxException;
}
