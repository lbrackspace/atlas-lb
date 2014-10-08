package org.openstack.atlas.service.domain.operations;

import java.io.Serializable;

public class OperationResponse implements Serializable {

    private final static long serialVersionUID = 532512316L;
    private Operation operation;
    private Object entity;
    private boolean executedOkay;
    private ErrorReason errorReason;
    private String message;

    public enum ErrorReason {
        BAD_REQUEST,
        CLUSTER_STATUS,
        ENTITY_NOT_FOUND,
        GONE,
        IMMUTABLE_ENTITY,
        METHOD_NOT_ALLOWED,
        NO_AVAILABLE_CLUSTER,
        OUT_OF_VIPS,
        OVER_LIMIT,
        SERVICE_UNAVAILABLE,
        STINGRAY_TIMEOUT,
        UNAUTHORIZED,
        UNKNOWN,
        UNPROCESSABLE_ENTITY
    }

    public OperationResponse() {
    }

    public OperationResponse(Operation operation, Object entity, boolean executedOkay, ErrorReason errorReason, String message) {
        this.operation = operation;
        this.entity = entity;
        this.executedOkay = executedOkay;
        this.errorReason = errorReason;
        this.message = message;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
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
