package org.openstack.atlas.restclients.auth.impl;

import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.ImpersonationResourceManager;
import org.openstack.atlas.restclients.auth.util.ResourceUtil;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;
import org.openstack.identity.client.access.Access;
import org.openstack.identity.client.impersonation.Impersonation;
import org.openstack.identity.client.impersonation.ObjectFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.URI;
import java.net.URISyntaxException;

public class ImpersonationResourceManagerImpl extends ResponseManagerImpl implements ImpersonationResourceManager {

    @Override
    public Access impersonateUser(Client client, String url, String token, String userName, int epireInSeconds) throws IdentityFault, URISyntaxException, JAXBException {
        Response response = null;
        try {
            response = post(client, new URI(url + IdentityConstants.RAX_AUTH + "/" + IdentityConstants.IMPERSONATION_TOKENS_PATH), token, buildImpersonationRequestObject(userName, epireInSeconds));
        } catch (ResponseProcessingException ux) {
            throw IdentityResponseWrapper.buildFaultMessage(ux.getResponse());
        } catch (JAXBException e) {
            throw new IdentityFault(e.getMessage(), e.getLinkedException().getLocalizedMessage(), Integer.valueOf(e.getErrorCode()));
        }

        if (!isResponseValid(response)) {
            handleBadResponse(response);
        }

        return response.readEntity(Access.class);
    }


    private String buildImpersonationRequestObject(String userName, int expireInSeconds) throws JAXBException {
        ObjectFactory factory = new ObjectFactory();

        org.openstack.identity.client.impersonation.User user = factory.createUser();
        user.setUsername(userName);

        Impersonation imp = factory.createImpersonation();
        imp.setUser(user);
        imp.setExpireInSeconds(expireInSeconds);

        return ResourceUtil.marshallResource(factory.createImpersonation(imp),
                JAXBContext.newInstance(Impersonation.class)).toString();
    }
}