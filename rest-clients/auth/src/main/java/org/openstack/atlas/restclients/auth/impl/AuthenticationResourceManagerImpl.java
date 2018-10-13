package org.openstack.atlas.restclients.auth.impl;


import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.AuthenticationResourceManager;
import org.openstack.atlas.restclients.auth.util.ResourceUtil;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;
import org.openstack.identity.client.credentials.AuthenticationRequest;
import org.openstack.identity.client.credentials.ObjectFactory;
import org.openstack.identity.client.credentials.PasswordCredentialsRequiredUsername;
import org.openstack.identity.client.token.AuthenticateResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class AuthenticationResourceManagerImpl extends ResponseManagerImpl implements AuthenticationResourceManager {
    public AuthenticationRequest authenticationRequest;

    public AuthenticationResourceManagerImpl(AuthenticationRequest authenticationRequest) {
        this.authenticationRequest = authenticationRequest;
    }

    public AuthenticationResourceManagerImpl() {
    }

    /**
     * Authenticate by username password
     *
     * @param client
     * @param url
     * @param username
     * @param password
     * @return
     * @throws IdentityFault
     * @throws URISyntaxException
     */
    @Override
    public AuthenticateResponse authenticateUsernamePassword(Client client, String url, String username, String password) throws IdentityFault, URISyntaxException {
        Response response = null;
        try {
            buildUsernamePasswordCredentials(username, password);
            response = post(client, new URI(url + IdentityConstants.TOKEN_PATH),
                    generateAuthenticateRequest());
        } catch (ResponseProcessingException ux) {
            throw IdentityResponseWrapper.buildFaultMessage(ux.getResponse());
        } catch (JAXBException e) {
            throw IdentityFault(e);
        }

        if (!isResponseValid(response)) {
            handleBadResponse(response);
        }

        return response.readEntity(AuthenticateResponse.class);
    }

    /**
     * Generate authenticateRequest object
     *
     * @param authenticationRequest
     * @return
     * @throws JAXBException
     * @throws IdentityFault
     */
    private String generateAuthenticateRequest(AuthenticationRequest authenticationRequest) throws JAXBException, IdentityFault {
        if (authenticationRequest != null) {
        ObjectFactory factory = new ObjectFactory();
        return ResourceUtil.marshallResource(factory.createAuth(authenticationRequest),
                JAXBContext.newInstance(AuthenticationRequest.class)).toString();
        } else {
            throw new IdentityFault("Request object must be initialized first. ", "Internal ERROR", 500);
        }
    }

    //Request generation:

    /**
     * Return the generated authentication request
     *
     * @return
     * @throws JAXBException
     * @throws IdentityFault
     */
    private String generateAuthenticateRequest() throws JAXBException, IdentityFault {
        return generateAuthenticateRequest(authenticationRequest);
    }

    /**
     * Generate username password authentication request object
     *
     * @param username
     * @param password
     */
    private void buildUsernamePasswordCredentials(String username, String password) {
        ObjectFactory factory = new ObjectFactory();
        PasswordCredentialsRequiredUsername requiredUsername = new PasswordCredentialsRequiredUsername();
        requiredUsername.setUsername(username);
        requiredUsername.setPassword(password);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setCredential(factory.createPasswordCredentials(requiredUsername));
        this.authenticationRequest = authenticationRequest;
    }

    /**
     * Return generic IdentityFault
     *
     * @param ex
     * @return
     */
    private IdentityFault IdentityFault(Exception ex) {
        return new IdentityFault("Error processing request. ", Arrays.toString(ex.getStackTrace()), 500);
    }
}
