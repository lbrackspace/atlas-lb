package org.openstack.atlas.service.domain.operation;

import java.io.Serializable;

public class OperationResponse implements Serializable {

    private final static long serialVersionUID = 532512316L;
    private CoreOperation operation;
    private Object entity;
    private boolean executedOkay;
    private ErrorReason errorReason;
    private String message;

    public enum ErrorReason {

        UNAUTHORIZED, ENTITY_NOT_FOUND, IMMUTABLE_ENTITY, SERVICE_UNAVAILABLE, OUT_OF_VIPS, OVER_LIMIT, BAD_REQUEST, GONE, UNPROCESSABLE_ENTITY, UNKNOWN
    }

    public OperationResponse() {
    }

    public OperationResponse(CoreOperation operation, Object entity, boolean executedOkay, ErrorReason errorReason, String message) {
        this.operation = operation;
        this.entity = entity;
        this.executedOkay = executedOkay;
        this.errorReason = errorReason;
        this.message = message;
    }

    public CoreOperation getOperation() {
        return operation;
    }

    public void setOperation(CoreOperation operation) {
        this.operation = operation;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public boolean isExecutedOkay() {
        return executedOkay;
    }

    public void setExecutedOkay(boolean executedOkay) {
        this.executedOkay = executedOkay;
    }

    public ErrorReason getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(ErrorReason errorReason) {
        this.errorReason = errorReason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
