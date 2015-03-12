package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.util.common.CloudFilesSegment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;

public class WriteObjectThread extends Thread {

    private CloudFilesUtils cloudFilesUtils;
    private String containerName;
    private CloudFilesSegment segment;
    private ResponseContainer<Boolean> response;
    private Exception exception;

    public CloudFilesUtils getCloudFilesUtils() {
        return cloudFilesUtils;
    }

    public WriteObjectThread(CloudFilesUtils cloudFilesUtils, String containerName, CloudFilesSegment segment) {
        this.cloudFilesUtils = cloudFilesUtils;
        this.containerName = containerName;
        this.segment = segment;
    }

    @Override
    public void run() {
        response = cloudFilesUtils.writeObjectSegment(containerName, segment);

    }

    public CloudFilesSegment getSegment() {
        return segment;
    }

    public void setSegment(CloudFilesSegment segment) {
        this.segment = segment;
    }

    public ResponseContainer<Boolean> getResponse() {
        return response;
    }

    public void setResponse(ResponseContainer<Boolean> response) {
        this.response = response;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
