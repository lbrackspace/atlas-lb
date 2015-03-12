package org.openstack.atlas.cloudfiles.objs;

import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class FilesObjectList {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (objectsList == null) {
            objectsList = new ArrayList<FilesObject>();
        }
        sb.append("FilesObjectList{objectsList=");
        sb.append(StaticStringUtils.collectionToString(objectsList, ","));
        return sb.toString();
    }
    private List<FilesObject> objectsList;

    public FilesObjectList() {
        objectsList = new ArrayList<FilesObject>();
    }

    public List<FilesObject> getObjectsList() {
        if (objectsList == null) {
            objectsList = new ArrayList<FilesObject>();
        }
        return objectsList;
    }

    public void setObjectsList(List<FilesObject> objectsList) {
        this.objectsList = objectsList;
    }
}
