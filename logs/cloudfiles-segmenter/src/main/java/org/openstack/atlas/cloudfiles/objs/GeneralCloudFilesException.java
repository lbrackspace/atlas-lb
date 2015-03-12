package org.openstack.atlas.cloudfiles.objs;

public class GeneralCloudFilesException extends Exception {

    public GeneralCloudFilesException(Throwable cause) {
        super(cause);
    }

    public GeneralCloudFilesException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralCloudFilesException(String message) {
        super(message);
    }

    public GeneralCloudFilesException() {
    }
}
