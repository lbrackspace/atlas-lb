package org.openstack.atlas.cloudfiles.objs.comparators;

import java.util.Comparator;
import org.openstack.atlas.cloudfiles.objs.FilesObject;
import org.openstack.atlas.cloudfiles.objs.FilesObject;

public class FilesObjectComparator implements Comparator<FilesObject>{

    @Override
    public int compare(FilesObject o1, FilesObject o2) {
        return o1.getName().compareTo(o2.getName());
    }

}
