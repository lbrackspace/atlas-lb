package org.openstack.atlas.restclients.auth.manager;

import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.roles.Role;
import org.openstack.identity.client.roles.RoleList;

import javax.ws.rs.client.Client;
import java.net.URISyntaxException;

public interface RolesResourceManager {
    public RoleList listUserGlobalRoles(Client client, String url, String token, String userId) throws IdentityFault, URISyntaxException;
}
