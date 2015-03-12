package org.openstack.atlas.cloudfiles.objs;

import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class FilesObjectList {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FilesObjectList{objectsList=");
        sb.append(StaticStringUtils.collectionToString(getList(), ","));
        return sb.toString();
    }
    private List<FilesObject> list;

    public FilesObjectList() {
        list = new ArrayList<FilesObject>();
    }

    public List<FilesObject> getList() {
        if (list == null) {
            list = new ArrayList<FilesObject>();
        }
        return list;
    }

    public void setList(List<FilesObject> objects) {
        this.list = objects;
    }

    public int size() {
        return getList().size();
    }
}
