package org.openstack.atlas.util.snmp.comparators;

import java.util.Comparator;
import org.openstack.atlas.util.snmp.RawSnmpUsage;

public class BandwidthInComparator implements Comparator<RawSnmpUsage> {

    @Override
    public int compare(RawSnmpUsage o1, RawSnmpUsage o2) {
        long l1;
        long l2;

        l1 = o1.getBytesInHi();
        l2 = o2.getBytesInHi();
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }

        l1 = o1.getBytesInLo();
        l2 = o2.getBytesInLo();
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }

        return 0;
    }
}
