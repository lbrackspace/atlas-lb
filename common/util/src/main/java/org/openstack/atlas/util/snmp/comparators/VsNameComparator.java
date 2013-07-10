package org.openstack.atlas.util.snmp.comparators;

import java.util.Comparator;
import org.openstack.atlas.util.snmp.RawSnmpUsage;

public class VsNameComparator implements Comparator<RawSnmpUsage> {

    @Override
    public int compare(RawSnmpUsage o1, RawSnmpUsage o2) {
        return o1.getVsName().compareTo(o2.getVsName());
    }
}
