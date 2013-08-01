package org.openstack.atlas.logs.itest;

import com.hadoop.compression.lzo.LzopCodec;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class TestLzoCompressionMain {

    public static final int BUFFSIZE = 256 * 1024;

    public static String usage() {
        StringBuilder sb = new StringBuilder();
        String progName = Debug.getProgName(TestLzoCompressionMain.class);
        String javaLibPath = System.getProperty("java.library.path");
        sb = sb.append("Usage is ").append(progName).append("<fileName>\n").
                append("\n").
                append("    Tests if Lzo compression works\n").
                append("    If compression fails then install the low level gplcompression\n").
                append("    in a directory somewhere on the path below\n").
                append("JAVA_LIBRARY_PATH=\"").append(javaLibPath).append("\"\n");

        return sb.toString();
    }

    public static void testLzoCompress(InputStream inStream, OutputStream lzoStream, OutputStream idxStream, int buffSize, PrintStream ps) throws IOException {
        Configuration codecConf = new Configuration();
        codecConf.set("io.compression.codecs", "org.apache.hadoop.io.compress.GzipCodec,org.apache.hadoop.io.compress.DefaultCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.BZip2Codec");
        codecConf.set("io.compression.codec.lzo.class", "com.hadoop.compression.lzo.LzoCodec");
        LzopCodec codec = new LzopCodec();
        codec.setConf(codecConf);
        CompressionOutputStream cos = codec.createIndexedOutputStream(lzoStream, new DataOutputStream(idxStream));
        StaticFileUtils.copyStreams(inStream, cos, ps, buffSize);
        cos.close();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length <= 0) {
            System.out.printf("%s", usage());
            return;
        }
        String filePath = StaticFileUtils.expandUser(args[0]);
        String lzoPath = filePath + ".lzo";
        String idxPath = lzoPath + ".index";
        System.out.printf("Compression input  file: %s\n", filePath);
        System.out.printf("Compression output file: %s\n", lzoPath);
        System.out.printf("Compression index  file: %s\n", idxPath);

        FileInputStream inStream = new FileInputStream(filePath);
        FileOutputStream lzoStream = new FileOutputStream(lzoPath);
        FileOutputStream idxStream = new FileOutputStream(idxPath);

        testLzoCompress(inStream, lzoStream, idxStream, BUFFSIZE, System.out);

        inStream.close();
        lzoStream.close();
        idxStream.close();
        return;
    }
}
