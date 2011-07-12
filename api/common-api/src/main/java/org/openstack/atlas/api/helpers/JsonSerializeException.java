package org.openstack.atlas.api.helpers;

import org.codehaus.jackson.JsonProcessingException;

public class JsonSerializeException extends JsonProcessingException {

    public JsonSerializeException(String msg,Throwable th) {
        super(msg,th);
    }
    
}
