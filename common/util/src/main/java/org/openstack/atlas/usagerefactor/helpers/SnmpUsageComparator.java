package org.openstack.atlas.usagerefactor.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.openstack.atlas.usagerefactor.SnmpUsage;

public class SnmpUsageComparator implements Comparator<SnmpUsage> {

    private List<SnmpUsageComparatorType> comparatorTypes = new ArrayList<SnmpUsageComparatorType>();

    @Override
    public int compare(SnmpUsage u1, SnmpUsage u2) {
        int comp = 0;
        for (SnmpUsageComparatorType comparatorType : comparatorTypes) {
            switch (comparatorType) {
                case BYTES_IN:
                    comp = longComp(u1.getBytesIn(), u2.getBytesIn());
                    break;
                case BYTES_OUT:
                    comp = longComp(u1.getBytesOut(), u2.getBytesOut());
                    break;
                case BYTES_SSL_IN:
                    comp = longComp(u1.getBytesInSsl(), u2.getBytesOutSsl());
                    break;
                case BYTES_SSL_OUT:
                    comp = longComp(u1.getBytesOutSsl(), u2.getBytesOutSsl());
                    break;
                case CONCURRENT_CONNECTIONS:
                    comp = intComp(u1.getConcurrentConnections(), u2.getConcurrentConnections());
                    break;
                case CONCURRENT_SSL_CONNECTIONS:
                    comp = intComp(u1.getConcurrentConnectionsSsl(), u2.getConcurrentConnectionsSsl());
                    break;
                case HOST_ID:
                    comp = intComp(u1.getHostId(), u2.getHostId());
                    break;
                case LOADBALANCER_ID:
                    comp = intComp(u1.getLoadbalancerId(), u2.getLoadbalancerId());
                    break;
            }
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }

    private static int intComp(int i1, int i2) {
        if (i1 < i2) {
            return -1;
        }
        if (i1 > i2) {
            return 1;
        }
        return 0;
    }

    private static int longComp(long l1, long l2) {
        if (l1 < l2) {
            return -1;
        }
        if (l1 > l2) {
            return 1;
        }
        return 0;
    }

    public void clear() {
        comparatorTypes = new ArrayList<SnmpUsageComparatorType>();
    }

    public List<SnmpUsageComparatorType> getComparatorTypes() {
        if (comparatorTypes == null) {
            comparatorTypes = new ArrayList<SnmpUsageComparatorType>();
        }
        return comparatorTypes;
    }

    public void setComparatorTypes(List<SnmpUsageComparatorType> comparatorTypes) {
        this.comparatorTypes = comparatorTypes;
    }
}
