package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.data.AuthUser;
import org.openstack.atlas.util.FileSystemUtils;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.activation.FileTypeMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CloudFilesDaoImpl implements CloudFilesDao {

    private static final Log LOG = LogFactory.getLog(CloudFilesDaoImpl.class);

    private FilesClient client;

    private FileTypeMap fileMap = FileTypeMap.getDefaultFileTypeMap();

    private FileSystemUtils fileSystemUtils;

    @Required
    public void setFileSystemUtils(FileSystemUtils fileSystemUtils) {
        this.fileSystemUtils = fileSystemUtils;
    }

    private void storeObject(String containerName, File file, String contentType, String remoteFilename, Map values) throws IOException {
        try {
            client.storeObjectAs(containerName, file, contentType, remoteFilename, values);
        } catch (Exception e) {
            // Files has a tendency to crap out on uploads. Just try again.
            client.storeObjectAs(containerName, file, contentType, remoteFilename, values);
        }
    }

    public void uploadLocalFile(AuthUser user, String containerName, String localFilename, String remoteFileName) throws FilesException {
        client = new FilesClient(user.getUsername(), user.getAuthKey(), null, 5000);
        client.setAuthenticationURL(user.getCloudFilesAuthUrl());

        String fullFilename = localFilename.replaceAll("\\.\\./", "./");
        // the localFilename will always be before the last slash, if we are using
        // slashes (directories)
        String restOfFilename = fileSystemUtils.getRestOfFilename(fullFilename);

        try {
            login();
            verifyContainerExistance(containerName);
            try {
                if (client.getObjectMetaData(containerName, restOfFilename) != null) {
                    throw new FilesException("Could not add file [" + localFilename + "]", null);
                }
            } catch (FilesNotFoundException e) {
                //file does nto exist, good.
            }
            File file = new File(fullFilename);
            String contentType = fileMap.getContentType(file);

            storeObject(containerName, file, contentType, remoteFileName, new HashMap());

        } catch (Exception e) {
            throw new FilesException(fullFilename + "-Failed to upload file:" + e.getMessage(), e);
        }
    }

    public void verifyContainerExistance(String containerName) throws Exception {
        login();
        if (!client.containerExists(containerName)) {
            client.createContainer(containerName);
        }
    }

    public boolean login() throws Exception {
        // Hack used elsewhere in the App to simulate login via cloud files api.
        try {
            client.login();
        } catch (Exception e) {
            // try again to log in, sometimes this fails randomly
            client.login();
        }
        return true;
    }

}