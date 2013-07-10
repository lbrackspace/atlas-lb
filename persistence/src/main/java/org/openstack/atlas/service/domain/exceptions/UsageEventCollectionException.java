package org.openstack.atlas.service.domain.exceptions;

public class UsageEventCollectionException extends Exception {

    public UsageEventCollectionException(String message) {
        super(message);
    }

    public UsageEventCollectionException(String message, Throwable th) {
        super(message, th);
    }

    public UsageEventCollectionException(Throwable th) {
        super(th);
    }

    public UsageEventCollectionException() {
        super();
    }
}
