package org.openstack.atlas.io;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class LbLogsAccountDateKeyComparator extends WritableComparator {

    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
        LbLogsAccountDateKey k1 = (LbLogsAccountDateKey) w1;
        LbLogsAccountDateKey k2 = (LbLogsAccountDateKey) w2;

        return k1.compareTo(k2);

    }


    protected LbLogsAccountDateKeyComparator() {
        super(LbLogsAccountDateKey.class, true);
    }

}
