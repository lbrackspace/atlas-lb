package org.openstack.atlas.cloudfiles.objs;

import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class FilesContainerList {

    @Override
    public String toString() {
        if (list == null) {
            list = new ArrayList<FilesContainer>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FilesContainerList{containers=");
        sb.append(StaticStringUtils.collectionToString(getList(), ","));
        sb.append("}");
        return sb.toString();
    }
    private List<FilesContainer> list;

    public FilesContainerList() {
    }

    public List<FilesContainer> getList() {
        if (list == null) {
            list = new ArrayList<FilesContainer>();
        }
        return list;
    }

    public void setList(List<FilesContainer> containers) {
        this.list = containers;
    }

    public int size() {
        return getList().size();
    }
}
