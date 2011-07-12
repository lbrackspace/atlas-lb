package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.data.AuthUser;
import com.rackspacecloud.client.cloudfiles.FilesException;

public interface CloudFilesDao {

    void uploadLocalFile(AuthUser user, String containerName, String filename, String remoteFileName) throws FilesException;

}