package org.openstack.atlas.io.lzo;

import com.hadoop.compression.lzo.LzopCodec;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.openstack.atlas.util.StaticFileUtils;


public class LzoCompressorIndexerMain {

    private static final int BUFFSIZE = 128 * 1024;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 1) {
            System.out.printf("usage is <fileName> [buffsize]\n");
            System.out.printf("\n");
            System.out.printf("Compress and index the text file listed in the command line\n");
            System.out.printf("adding files with the .lzo and .lzo.index file extension\n");
            System.out.printf("You will need to send the index files to hdfs to make the lzo files\n");
            System.out.printf("splittable\n");
            System.out.printf("JAVA_LIBRARY_PATH=%s\n", System.getProperty("java.library.path"));
            return;
        }

        String txtFileName = StaticFileUtils.expandUser(args[0]);
        String lzoFileName = txtFileName + ".lzo";
        String idxFileName = txtFileName + ".lzo.index";
        long fileSize = (new File(txtFileName)).length();
        int buffsize = (args.length >= 2) ? Integer.parseInt(args[1]) : BUFFSIZE;
        Configuration conf = new Configuration();
        conf.set("io.compression.codecs","org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        conf.set("io.compression.codec.lzo.class","com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(conf);
        InputStream is = new FileInputStream(new File(txtFileName));
        OutputStream os = new FileOutputStream(new File(lzoFileName));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(idxFileName)), BUFFSIZE));

        String fmt = "Compressing file %s to %s with index file %s with buffSize of %d bytes\n";
        System.out.printf(fmt, txtFileName, lzoFileName, idxFileName, buffsize);
        CompressionOutputStream cos = codec.createIndexedOutputStream(os, dos);
        StaticFileUtils.copyStreams(is, cos, System.out, fileSize, buffsize);
        cos.close();
        os.close();
        is.close();
        dos.close();
        System.out.printf("Compressed all data\n");
        System.out.printf("Don't forget to upload the index file as well as the LZO file\n");
    }
}
