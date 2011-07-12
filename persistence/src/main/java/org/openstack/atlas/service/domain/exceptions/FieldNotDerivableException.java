
package org.openstack.atlas.service.domain.exceptions;

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
    public FieldNotDerivableException(){
        super();
    }
}
