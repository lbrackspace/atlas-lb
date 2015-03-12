package org.openstack.atlas.cloudfiles.objs;

import java.util.ArrayList;
import java.util.List;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class FilesContainerList {

    @Override
    public String toString() {
        if (containers == null) {
            containers = new ArrayList<FilesContainer>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("FilesContainerList{containers=");
        sb.append(StaticStringUtils.collectionToString(containers, ","));
        sb.append("}");
        return sb.toString();
    }
    private List<FilesContainer> containers;

    public FilesContainerList() {
    }

    public List<FilesContainer> getContainers() {
        if (containers == null) {
            containers = new ArrayList<FilesContainer>();
        }
        return containers;
    }

    public void setContainers(List<FilesContainer> containers) {
        this.containers = containers;
    }
}
