package org.openstack.atlas.util.common;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class CloudFilesSegmentContainerTest {

    public static final int BUFFSIZE = 64 * 1024;
    public static final int SEGMENT_SIZE = 1024 * 1024;

    public CloudFilesSegmentContainerTest() {
    }

    @Test
    public void testMd5sumOnContainer() throws IOException, FileNotFoundException, NoSuchAlgorithmException {
        int i;
        int nSegs;
        String tmpFileName = newTempFile();
        String expMd5Sums[] = {"011bc8869b584f0749ad000f8bcb8efd",
            "319a938d357c694b1e9649061e6400c9",
            "e97c9a9da933bb515edff347296dc5d3",
            "2b29746731090df27c5b8ded94b76381",
            "07ed76279ec6a0288c67bda292bf47d2",
            "6c8a0c1cdff3ce69f096d27c6258b0e8",
            "3483bdd7fa429c5c12f68aad92c70618",
            "2f3ebb5a5247deaceb69d8b364ab4dc5",
            "7c7742ffe0fda3a9604e449c97fc2051",
            "f10f29c074873211ea3944cae84f513c",
            "72a43d289d899e34a2f4f7e19814ac49",
            "ff8112fd7384eebaa30892b35d696d73",
            "337c033f60ecbbea89bbf30c72061aa8",
            "2d31d97199fb287d6fcb4f82ebd1b3f2"};
        CloudFilesSegmentContainer container = CloudFilesSegmentContainer.newSegmentContainer(tmpFileName, SEGMENT_SIZE);
        nSegs = container.getSegments().size();
        Assert.assertEquals(nSegs, expMd5Sums.length);
        for (i = 0; i < nSegs; i++) {
            container.getSegments().get(i).computeMd5sum();
        }
        for (i = 0; i < nSegs; i++) {
            String expMd5sum = expMd5Sums[i];
            String foundmd5sum = container.getSegments().get(i).getMd5sum();
            junit.framework.Assert.assertEquals(expMd5sum, foundmd5sum);
        }
        File tmpFile = new File(tmpFileName);
        tmpFile.delete();
    }

    public String newTempFile() throws IOException {
        int i;
        int l = 1024 * 1024 + 1;
        String text = "0123456789abc";
        byte[] rawBytes = text.getBytes("utf-8");

        File tmpFile = File.createTempFile("md5test_file", ".dat");
        String tmpFileName = tmpFile.getCanonicalPath();
        OutputStream os = StaticFileUtils.openOutputFile(tmpFileName, BUFFSIZE);
        for (i = 0; i < l; i++) {
            os.write(rawBytes);
        }
        os.close();
        return tmpFileName;
    }
}
