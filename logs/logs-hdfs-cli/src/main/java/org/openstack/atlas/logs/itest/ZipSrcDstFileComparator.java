
package org.openstack.atlas.logs.itest;

import java.util.Comparator;

public class ZipSrcDstFileComparator implements Comparator<ZipSrcDstFile>{

    @Override
    public int compare(ZipSrcDstFile o1, ZipSrcDstFile o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        try {
            long o1Hour = Long.parseLong(o1.getHourKey());
            long o2Hour = Long.parseLong(o2.getHourKey());
            if (o1Hour < o2Hour) {
                return -1;
            }
            if (o1Hour > o2Hour) {
                return 1;
            }
        } catch (NumberFormatException ex) {
        }
        int o1aid = o1.getAccountId();
        int o2aid = o2.getAccountId();

        if (o1aid < o2aid) {
            return -1;
        }
        if (o1aid > o2aid) {
            return 1;
        }

        int o1lid = o1.getLoadbalancerId();
        int o2lid = o2.getLoadbalancerId();
        if (o1lid < o2lid) {
            return -1;
        }
        if (o1lid > o2lid) {
            return 1;
        }
        return 0;
    }
}
