package org.openstack.atlas.util.snmp.comparators;

import java.util.Comparator;
import org.openstack.atlas.util.snmp.RawSnmpUsage;

public class BandwidthoutComparator implements Comparator<RawSnmpUsage> {

    @Override
    public int compare(RawSnmpUsage o1, RawSnmpUsage o2) {
        long l1;
        long l2;

        l1 = o1.getBytesOutHi();
        l2 = o2.getBytesOutHi();
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }

        l1 = o1.getBytesOutLo();
        l2 = o2.getBytesOutLo();
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }

        return 0;
    }
}
