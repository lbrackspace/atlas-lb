package org.openstack.atlas.mapreduce;

import com.hadoop.mapred.DeprecatedLzoTextInputFormat;
import org.openstack.atlas.io.*;
import org.openstack.atlas.tools.DirectoryTool;
import org.openstack.atlas.tools.HadoopConfiguration;
import org.openstack.atlas.tools.QuartzSchedulerConfigs;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.openstack.atlas.util.Constants;

import java.io.IOException;
import java.util.List;

public class LbStatsTool extends DirectoryTool {
    @Override
    protected Class<? extends Mapper> getMapperClass() {
        return LbStatsMapper.class;
    }

    @Override
    protected Class<? extends Reducer> getReducerClass() {
        return LbStatsReducer.class;
    }

    @Override
    protected String getOutputFolderPrefix() {
        return "/lb_logs_split/";
    }

    @Override
    protected void setSpecialConfigurations(HadoopConfiguration specialConfigurations, QuartzSchedulerConfigs localRunner) throws IOException {
        specialConfigurations.getJobConf().setJobName("LB_STATS"); //NameVal.FQDN.toString()
        if (localRunner.isLzoInput()) {
            //INPUT THE THING INTO MVN AND GET THE JARS ADDED
            specialConfigurations.getJobConf().setInputFormat(DeprecatedLzoTextInputFormat.class);
        }

        specialConfigurations.getJobConf().setMapOutputKeyClass(LbLogsAccountDateKey.class);
        specialConfigurations.getJobConf().setMapOutputValueClass(LbLogsWritable.class);

        specialConfigurations.getJobConf().setPartitionerClass(LbLogsAccountDateKeyPartitioner.class);

        specialConfigurations.getJobConf().setOutputKeyComparatorClass(LbLogsAccountDateKeyComparator.class);
        specialConfigurations.getJobConf().setOutputValueGroupingComparator(LbLogsAccountDateKeyDateComparator.class);

        specialConfigurations.getJobConf().setOutputKeyClass(Text.class);
        specialConfigurations.getJobConf().setOutputValueClass(FileBytesWritable.class);

        specialConfigurations.getJobConf().setInputFormat(TextInputFormat.class);
        specialConfigurations.getJobConf().setOutputFormat(SequenceFileOutputFormat.class);
        
        SequenceFileOutputFormat.setOutputCompressionType(specialConfigurations.getJobConf(), SequenceFile.CompressionType.BLOCK);

        specialConfigurations.getJobConf().set(Constants.FILEDATE, localRunner.getRawlogsFileTime());
        
    }

    private String createInput(List<String> inputForMultiPathJobs) {
        String returnString = "";
        for (String input : inputForMultiPathJobs) {
            returnString += input + ",";
        }
        return returnString.substring(0, returnString.length() - 1);
    }    
}
