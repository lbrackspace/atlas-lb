package org.openstack.atlas.logs.itest;

import java.util.Comparator;
import org.apache.hadoop.fs.FileStatus;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class FileStatusDateComparator implements Comparator<FileStatus> {

    @Override
    public int compare(FileStatus o1, FileStatus o2) {
        Integer o1DateKey;
        Integer o2DateKey;

        try {
            o1DateKey = Integer.parseInt(StaticFileUtils.pathTail(o1.getPath().toUri().toString()));
        } catch (Exception ex) {
            o1DateKey = null;
        }
        try {
            o2DateKey = Integer.parseInt(StaticFileUtils.pathTail(o2.getPath().toUri().toString()));
        } catch (Exception ex) {
            o2DateKey = null;
        }
        if (o1DateKey == null && o2DateKey != null) {
            return 1;
        }
        if (o2DateKey != null && o2DateKey == null) {
            return -1;
        }
        if (o1DateKey == null && o2DateKey == null) {
            return 0;
        }
        if (o1DateKey < o2DateKey) {
            return -1;
        }
        if (o1DateKey > o2DateKey) {
            return 1;
        }
        return 0;
    }
}
