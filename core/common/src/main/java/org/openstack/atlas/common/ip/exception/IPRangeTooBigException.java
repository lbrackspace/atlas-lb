package org.openstack.atlas.common.ip.exception;


public class IPRangeTooBigException extends IPStringException{
        public IPRangeTooBigException() {
    }

    public IPRangeTooBigException(String msg) {
        super(msg);
    }

}
