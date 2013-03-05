package org.openstack.atlas.logs.hadoop.mappers;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputKey;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;

public class LogMapper extends Mapper<LongWritable,Text,LogMapperOutputKey,LogMapperOutputValue>{
    @Override
    public void map(LongWritable mKey,Text mVal,Context ctx) throws IOException,InterruptedException{
        
    }

}
