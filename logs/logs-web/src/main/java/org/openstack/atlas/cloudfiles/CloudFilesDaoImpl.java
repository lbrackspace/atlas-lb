package org.openstack.atlas.cloudfiles;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.auth.AuthUser;
import org.openstack.atlas.util.FileSystemUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.activation.FileTypeMap;
import java.io.File;
import java.util.HashMap;
import org.openstack.atlas.util.StaticFileUtils;

public class CloudFilesDaoImpl implements CloudFilesDao {

    private static final Log LOG = LogFactory.getLog(CloudFilesDaoImpl.class);

    private FilesClient client;

    private FileTypeMap fileMap = FileTypeMap.getDefaultFileTypeMap();

    private FileSystemUtils fileSystemUtils;

    @Required
    public void setFileSystemUtils(FileSystemUtils fileSystemUtils) {
        this.fileSystemUtils = fileSystemUtils;
    }

    public synchronized void uploadLocalFile(AuthUser user, String containerName, String localFileName, String remoteFileName) throws FilesException {
        File localFile = new File(localFileName);
        if (!localFile.exists()) {
            throw new FilesException("Could not find the local file  " + localFileName + " anymore.", null);
        }
        client = new FilesClient(user.getUsername(), user.getAuthKey(), user.getCloudFilesAuthUrl(), null, 5000);
        client.setAuthenticationURL(user.getCloudFilesAuthUrl());

        String fullFilename = localFileName.replaceAll("\\.\\./", "./");
        // the localFilename will always be before the last slash, if we are using
        // slashes (directories)
        String restOfFilename = StaticFileUtils.getRestOfFilename(fullFilename);

        try {
            try {
                client.login();
            } catch (Exception e) {
                // try again to log in, sometimes this fails randomly
                client.login();
            }
            client.setCurrentRegion(user.getRegion());
            if (!client.containerExists(containerName)) {
                client.createContainer(containerName);
            }
            try {
                if (client.getObjectMetaData(containerName, restOfFilename) != null) {
                    throw new FilesException("Could not add file [" + localFileName + "]", null);
                }
            } catch (FilesNotFoundException e) {
                //file does nto exist, good.
            }
            File file = new File(fullFilename);
            String contentType = fileMap.getContentType(file);

            try {
                client.storeObjectAs(containerName, file, contentType, remoteFileName, new HashMap());
            } catch (Exception e) {
                // Files has a tendency to crap out on uploads. Just try again.
                client.storeObjectAs(containerName, file, contentType, remoteFileName, new HashMap());
            }


        } catch (Exception e) {
            throw new FilesException(fullFilename + "-Failed to upload file:" + e.getMessage(), e);
        }
    }


}