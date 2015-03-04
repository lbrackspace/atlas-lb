package org.openstack.atlas.logs.itest;

import java.util.Comparator;

public class IdCounterComparator implements Comparator<IdCounter> {

    @Override
    public int compare(IdCounter o1, IdCounter o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        long  i1 = o1.getId();
        long i2 = o2.getId();
        long c1 = o1.getCount();
        long c2 = o2.getCount();
        if (c1 < c2) {
            return -1;
        }
        if (c1 > c2) {
            return 1;
        }
        if (i1 < i2) {
            return -1;
        }
        if (i1 > i2) {
            return 1;
        }
        return 0;
    }
}
