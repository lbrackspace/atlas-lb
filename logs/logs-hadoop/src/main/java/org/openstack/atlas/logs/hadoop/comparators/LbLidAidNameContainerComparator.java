package org.openstack.atlas.logs.hadoop.comparators;

import java.util.Comparator;
import org.openstack.atlas.logs.hadoop.util.LbLidAidNameContainer;


public class LbLidAidNameContainerComparator implements Comparator<LbLidAidNameContainer>{

    @Override
    public int compare(LbLidAidNameContainer o1, LbLidAidNameContainer o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o1 != null && o2 == null) {
            return 1;
        }

        int o1Aid = o1.getAccountId();
        int o2Aid = o2.getAccountId();
        if (o1Aid < o2Aid) {
            return -1;
        }
        if (o1Aid > o2Aid) {
            return 1;
        }
        int o1Lid = o1.getLoadbalancerId();
        int o2Lid = o2.getLoadbalancerId();

        if (o1Lid < o2Lid) {
            return -1;
        }
        if (o1Lid > o2Lid) {
            return 1;
        }
        return 0;
    }
}
