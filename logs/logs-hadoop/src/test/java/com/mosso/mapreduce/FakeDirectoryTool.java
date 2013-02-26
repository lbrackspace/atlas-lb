package com.mosso.mapreduce;

import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.tools.HadoopConfiguration;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;

import java.io.IOException;

public class FakeDirectoryTool extends DirectoryTool {

    @Override
    protected Class<? extends Mapper> getMapperClass() {
        return FakeMapper.class;
    }

    @Override
    protected Class<? extends Reducer> getReducerClass() {
        return FakeReducer.class;
    }

    @Override
    protected void setSpecialConfigurations(HadoopConfiguration conf, QuartzSchedulerConfigs schedulerConfigs) throws IOException {
    }

}
