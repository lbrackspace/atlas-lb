package org.openstack.atlas.cloudfiles.objs;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ResponseContainer<E> {

    private String rawEntity;
    private ClientResponse clientResponse;
    private E entity;
    private int statusCode;

    public ResponseContainer() {
    }

    public static ResponseContainer newResponseContainer(ClientResponse resp) {
        ResponseContainer rc = new ResponseContainer();
        rc.setClientResponse(resp);
        try {
            rc.setRawEntity(resp.getEntity(String.class));
        } catch (UniformInterfaceException ex) {
            // no response found. not setting
        }
        rc.setStatusCode(resp.getStatus());
        return rc;
    }

    public String getRawEntity() {
        return rawEntity;
    }

    public void setRawEntity(String rawEntity) {
        this.rawEntity = rawEntity;
    }

    public ClientResponse getClientResponse() {
        return clientResponse;
    }

    public void setClientResponse(ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
    }

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
