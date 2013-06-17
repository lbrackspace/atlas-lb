package org.openstack.atlas.logs.hadoop.comparators;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;

public class LogSortComparator extends WritableComparator {

    protected LogSortComparator(){
        super(LogMapperOutputKey.class,true);
    }
    
    @Override
    public int compare(WritableComparable objA,WritableComparable objB){
        LogMapperOutputKey a=(LogMapperOutputKey)objA;
        LogMapperOutputKey b=(LogMapperOutputKey)objB;
        
        return a.compareTo(b);
    }
}
