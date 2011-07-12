package org.openstack.atlas.lb.helpers.ipstring.exceptions;


public class IPRangeTooBigException extends IPStringException{
        public IPRangeTooBigException() {
    }

    public IPRangeTooBigException(String msg) {
        super(msg);
    }

}
