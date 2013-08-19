package org.openstack.atlas.cloudfiles;

import com.rackspacecloud.client.cloudfiles.FilesException;
import org.openstack.atlas.auth.AuthUser;

public interface CloudFilesDao {

    void uploadLocalFile(AuthUser user, String containerName, String filename, String remoteFileName) throws FilesException;

}