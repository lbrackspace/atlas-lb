package org.openstack.atlas.util.common.comparators;

import java.util.Comparator;
import org.openstack.atlas.util.common.CloudFilesSegment;

public class CloudFilesSegmentComparator implements Comparator<CloudFilesSegment> {

    public CloudFilesSegmentComparator() {
    }

    @Override
    public int compare(CloudFilesSegment o1, CloudFilesSegment o2) {
        int f1 = o1.getFragNumber();
        int f2 = o2.getFragNumber();
        String n1 = o1.getFileName();
        String n2 = o2.getFileName();
        if (n1 == null) {
            n1 = "";
        }
        if (n2 == null) {
            n2 = "";
        }

        int cmp = n1.compareTo(n2);
        if (cmp != 0) {
            return cmp;
        }
        if (f1 < f2) {
            return -1;
        } else if (f1 > f2) {
            return 1;
        }
        return 0;
    }
}
