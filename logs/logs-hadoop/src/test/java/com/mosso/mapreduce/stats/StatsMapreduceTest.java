//package com.mosso.mapreduce.stats;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//
//import com.mosso.mapreduce.HadoopTool.RUN_STATES;
//
//import org.openstack.atlas.util.FileSystemUtils;
//import org.openstack.atlas.io.StatsWritable;
//import org.openstack.atlas.test.BaseHadoopTest;
//import org.openstack.atlas.test.TestHelper;
//
//import org.apache.commons.configuration.CompositeConfiguration;
//import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.SequenceFile;
//import org.apache.hadoop.io.Text;
//
//public class StatsMapreduceTest extends BaseHadoopTest {
//    private static final Log LOG = LogFactory.getLog(StatsMapreduceTest.class);
//
//    private StatsTool tool;
//
//    private FileSystemUtils utils;
//
//    public StatsMapreduceTest() throws IOException {
//        super();
//    }
//
//    public void testCCRun() throws Exception {
//        String localFile = "/tmp/testfile_statsmapred.txt";
//        String inputDir = tool.getInputDirectory();
//        String outputDir = tool.getOutputDirectory();
//
//        // create a file to run mapred on.
//        File folderF = new File(inputDir);
//        folderF.mkdirs();
//
//        BufferedWriter w = new BufferedWriter(new FileWriter(new File(localFile)));
//        w.write(TestHelper.generateLogLine("www.foo.com", "21/Jul/2009:22:18:46"));
//        w.write(TestHelper.generateLogLine("www.foo.com", "29/Jul/2009:22:44:40"));
//        w.close();
//        utils.removeFileFromDFS(getFileSystem(), outputDir, true);
//        utils.placeFileOnDFS(getFileSystem(), localFile, inputDir + "/testfile.txt");
//        RUN_STATES run = tool.executeHadoopRun();
//
//        LOG.info(run);
//
//        // sequence file read
//        String seqFilename = outputDir + "/part-00000";
//        SequenceFile.Reader r = new SequenceFile.Reader(getFileSystem(), new Path(seqFilename), tool
//                .getConfiguration().getConfiguration());
//
//        Text key = new Text();
//        StatsWritable val = new StatsWritable();
//        r.next(key, val);
//        LOG.info(key + ":" + val);
//        r.next(key, val);
//        LOG.info(key + ":" + val);
//        Runtime.getRuntime().exec("rm -fr " + localFile);
//
//    }
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        CompositeConfiguration conf = new CompositeConfiguration();
//        conf.addConfiguration(new PropertiesConfiguration("application.properties"));
//        utils = new FileSystemUtils();
//
//        tool = new StatsTool();
//        tool.setConf(conf);
//        tool.setFileSystemUtils(utils);
//        tool.setupHadoopRun("test_stats", null);
//    }
//}
