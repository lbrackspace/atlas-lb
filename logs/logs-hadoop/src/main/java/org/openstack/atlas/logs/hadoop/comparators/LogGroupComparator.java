
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
        int aAccountId = a.getAccountId();
        int bAccountId = b.getAccountId();
        int aLoadbalancerId = a.getLoadbalancerId();
        int bLoadBalancerId = b.getLoadbalancerId();

        if(aAccountId > bAccountId){
            return 1;
        }
        if(aAccountId < bAccountId){
            return -1;
        }
        if(aLoadbalancerId > bLoadBalancerId){
            return 1;
        }
        if(aLoadbalancerId < bLoadBalancerId){
            return -1;
        }
        return 0;
    }

}
