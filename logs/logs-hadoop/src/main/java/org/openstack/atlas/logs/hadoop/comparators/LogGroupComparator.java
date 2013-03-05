
package org.openstack.atlas.logs.hadoop.comparators;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
public class LogGroupComparator extends WritableComparator{
    protected LogGroupComparator(){
        super(LogMapperOutputKey.class,true);
    }

    @Override
    public int compare(WritableComparable objA,WritableComparable objB){
        LogMapperOutputKey  a = (LogMapperOutputKey)objA;
        LogMapperOutputKey  b = (LogMapperOutputKey)objB;
        int aaid = a.getAccountId();
        int baid = b.getAccountId();
        int alid = a.getLoadbalancerId();
        int blid = b.getLoadbalancerId();

        if(aaid > baid){
            return 1;
        }
        if(aaid < baid){
            return -1;
        }
        if(alid > blid){
            return 1;
        }
        if(alid < blid){
            return -1;
        }
        return 0;
    }

}
