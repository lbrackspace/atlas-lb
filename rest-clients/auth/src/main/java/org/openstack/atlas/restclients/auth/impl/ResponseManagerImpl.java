package org.openstack.atlas.restclients.auth.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.atlas.restclients.auth.manager.ResponseManager;
import org.openstack.atlas.restclients.auth.wrapper.IdentityResponseWrapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;


public abstract class ResponseManagerImpl implements ResponseManager {
    private final Log logger = LogFactory.getLog(ResponseManagerImpl.class);

    @Override
    public Response get(Client client, URI uri, String token) {
        return get(client, uri, token, new MultivaluedStringMap());
    }


    @Override
    public Response get(Client client, URI uri, String token, MultivaluedStringMap params) {
        WebTarget target = client.target(uri);

        for (String param : params.keySet()) {
            target.queryParam(param, params.getFirst(param));
        }
        return target.request(MediaType.APPLICATION_XML_TYPE)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .header(IdentityConstants.X_TOKEN_HEADER, token)
                .get();
    }

    @Override
    public Response post(Client client, URI uri, String body) {
        return client.target(uri).request(MediaType.APPLICATION_XML_TYPE)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.xml(body));
    }

    @Override
    public Response post(Client client, URI uri, String token, String body) {
        return client.target(uri).request(MediaType.APPLICATION_XML_TYPE)
                .header(IdentityConstants.X_TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.xml(body));
    }

    @Override
    public Response put(Client client, URI uri, String token, String body) {
        return client.target(uri).request(MediaType.APPLICATION_XML_TYPE)
                .header(IdentityConstants.X_TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.xml(body));
    }

    @Override
    public Response delete(Client client, URI uri, String token) {
        return client.target(uri).request(MediaType.APPLICATION_XML_TYPE)
                .header(IdentityConstants.X_TOKEN_HEADER, token)
                .accept(MediaType.APPLICATION_XML_TYPE)
                .delete();
    }

    @Override public Response head(Client client, URI uri, String body) {
        return null;
    }

    public boolean isResponseValid(Response response) {
        return (response != null && (response.getStatus() == IdentityConstants.ACCEPTED
                || response.getStatus() == IdentityConstants.NON_AUTHORATIVE
                || response.getStatus() == IdentityConstants.OK
                || response.getStatus() == IdentityConstants.NO_CONTENT
                || response.getStatus() == IdentityConstants.CREATED));
    }

    public boolean handleBadResponse(Response response) throws IdentityFault {
        if (response != null) {
            throw IdentityResponseWrapper.buildFaultMessage(response);
        } else {
            logger.error("Unable to retrieve response from server. Response: " + response);
            throw new IdentityFault("Network communication error, please try again.  ", "", 500);
        }
    }

}
