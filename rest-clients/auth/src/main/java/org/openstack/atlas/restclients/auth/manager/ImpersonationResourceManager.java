package org.openstack.atlas.restclients.auth.manager;

import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.access.Access;

import javax.ws.rs.client.Client;
import javax.xml.bind.JAXBException;
import java.net.URISyntaxException;

public interface ImpersonationResourceManager {

    public Access impersonateUser(Client client, String url, String token, String userName, int epireInSeconds) throws IdentityFault, URISyntaxException, JAXBException;

}