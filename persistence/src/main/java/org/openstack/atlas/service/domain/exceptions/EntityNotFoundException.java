package org.openstack.atlas.service.domain.exceptions;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message,Throwable throable){
        super(message,throable);
    }

    public EntityNotFoundException(Throwable throable){
        super(throable);
    }
    public EntityNotFoundException(){
        super();
    }
}
