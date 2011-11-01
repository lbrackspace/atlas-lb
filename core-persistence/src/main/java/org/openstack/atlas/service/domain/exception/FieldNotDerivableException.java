package org.openstack.atlas.service.domain.exception;

public class FieldNotDerivableException extends EntityNotFoundException{
    public FieldNotDerivableException(String message) {
        super(message);
    }

    public FieldNotDerivableException(String message,Throwable throable){
        super(message,throable);
    }

    public FieldNotDerivableException(Throwable throable){
        super(throable);
    }

}
