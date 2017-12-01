package org.openstack.atlas.api.helpers;


import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonSerializeException extends JsonProcessingException {

    public JsonSerializeException(String msg,Throwable th) {
        super(msg,th);
    }
    
}
