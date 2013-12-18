package org.openstack.atlas.io;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class LbLogsAccountDateKeyDateComparator extends WritableComparator {

    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
        LbLogsAccountDateKey k1 = (LbLogsAccountDateKey) w1;
        LbLogsAccountDateKey k2 = (LbLogsAccountDateKey) w2;

        int i = Long.valueOf(k1.getAccountId()).compareTo(Long.valueOf(k2.getAccountId()));
        if(i != 0) return i;

        int j = Long.valueOf(k1.getLoadBalancerId()).compareTo(Long.valueOf(k2.getLoadBalancerId()));
        if(j != 0) return j;

        //return k1.compareTo(k2);
        return 0;
    }


    protected LbLogsAccountDateKeyDateComparator() {
        super(LbLogsAccountDateKey.class, true);
    }

}
