package org.openstack.atlas.cloudfiles.objs;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.openstack.atlas.util.debug.Debug;

public class ResponseContainer<E> {

    private Exception exception = null;
    private String comment = null;
    private String rawEntity = null;
    private ClientResponse clientResponse = null;
    private E entity = null;
    private int statusCode = -1;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exception != null) {
            sb.append(Debug.getEST(exception));
            return sb.toString();
        }
        sb.append("{ comment=").append(comment).
                append(", statusCode=").append(statusCode).
                append(", rawEntity=").append(rawEntity).
                append(", entity=");
        if (entity instanceof Integer) {
            int val = (Integer) entity;
            sb.append(val);
        } else if (entity instanceof String) {
            sb.append((String) entity);
        } else if (entity instanceof Boolean) {
            boolean val = (Boolean) entity;
            sb.append(val);
        }
        sb.append("}");
        return sb.toString();
    }
}
