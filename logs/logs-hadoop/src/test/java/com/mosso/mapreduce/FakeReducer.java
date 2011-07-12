package com.mosso.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

public class FakeReducer implements Reducer<Text, Text, Text, Text> {

    @Override
    public void close() throws IOException {
    }

    @Override
    public void configure(JobConf arg0) {
    }

    @Override
    public void reduce(Text arg0, Iterator<Text> arg1, OutputCollector<Text, Text> arg2, Reporter arg3)
        throws IOException {
    }

}
