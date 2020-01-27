package org.rackspace.stingray.client.manager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client.manager.VTMRequestManager;
import org.rackspace.stingray.client.manager.util.VTMRequestManagerUtil;
import org.rackspace.stingray.client.util.ClientConstants;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public class VTMRequestManagerImpl implements VTMRequestManager {
    private static final Log LOG = LogFactory.getLog(VTMRequestManagerImpl.class);

    /**
     * Implementation of the method to retrieve a list from the REST api
     *
     *
     * @param endpoint The REST URL endpoint for retrieving the list items
     * @param client   The client to handle the specific request
     * @return Returns the entity inside the response of the REST api
     */
    @Override
    public Response getList(URI endpoint, Client client, String path)  throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        Response response = null;
        VTMRequestManagerUtil rmu = new VTMRequestManagerUtil();
        try {
            LOG.debug(String.format("GET requested for endpoint: %s", endpoint));
            response = client.target(URI.create(endpoint + path))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Response.class);
        } catch (Exception e) {
            throw new VTMRestClientException(ClientConstants.REQUEST_ERROR, e);
        }

        if (!rmu.isResponseValid(response)) {
            rmu.buildFaultMessage(response);
        }
        return response;
    }

    /**
     * Implementation of the method to retrieve a specific item from the REST api
     *
     * @param endpoint The REST URL endpoint for retrieving the item
     * @param client   The client to handle the specific request
     * @param path     The path to the specific object, including its name descriptor
     * @return Returns the entity inside the response of the REST api
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Override
    public Response getItem(URI endpoint, Client client, String path, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        VTMRequestManagerUtil rmu = new VTMRequestManagerUtil();

        Response response;
        try {
            response = client.target(endpoint + path)
                    .request(cType)
                    .get(Response.class);
        } catch (Exception e) {
            throw new VTMRestClientException(ClientConstants.REQUEST_ERROR, e);
        }

        if (!rmu.isResponseValid(response)) {
            rmu.buildFaultMessage(response);
        }
        return response;
    }

    /**
     * Implementation of the object creation method through the REST api.
     * In the case of the REST api, creation is the same method as updating,
     * so this method will simply call the update method.
     *
     * @param endpoint The REST URL endpoint for creating an item
     * @param client   The client to handle the specific request
     * @param path     The path to the specific object, including its name descriptor
     * @param object   The generic object to be created on Stingray's side
     * @return Returns the entity inside the response of the REST api
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Override
    public Response createItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        return updateItem(endpoint, client, path, object, cType);
    }

    /**
     * Implementation of the method for updating an item through the REST api
     *
     * @param endpoint The REST URL endpoint for updating an item
     * @param client   The client to handle the specific request
     * @param path     The path to the specific object, including its name descriptor
     * @param object   The generic object to be updated on Stingray's side
     * @return Returns the entity inside the response of the REST api
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Override
    public Response updateItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        Response response;
        VTMRequestManagerUtil rmu = new VTMRequestManagerUtil();

        try {
            response = client.target(endpoint + path)
                    .request(cType)
                    .put(Entity.entity(object, cType));
        } catch (Exception e) {
            throw new VTMRestClientException(ClientConstants.REQUEST_ERROR, e);
        }

        if (!rmu.isResponseValid(response)) {
            rmu.buildFaultMessage(response);
        }

        return response;
    }

    /**
     * Implementation of the method for deleting an item through the REST api
     *
     * @param endpoint The REST URL endpoint for deleting an item
     * @param client   The client to handle the specific request
     * @param path     The path to the specific object, including its name descriptor
     * @return Returns a boolean value evaluating the result of the operation
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     */
    @Override
    public Response deleteItem(URI endpoint, Client client, String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException {
        Response response;
        VTMRequestManagerUtil rmu = new VTMRequestManagerUtil();

        try {
            response = client.target(endpoint + path)
                    .request(MediaType.APPLICATION_JSON)
                    .delete(Response.class);
        } catch (Exception e) {
            throw new VTMRestClientException(ClientConstants.REQUEST_ERROR, e);
        }

        if (!rmu.isResponseValid(response)) {
            rmu.buildFaultMessage(response);
        }
        return Response.noContent().build();
    }


}

