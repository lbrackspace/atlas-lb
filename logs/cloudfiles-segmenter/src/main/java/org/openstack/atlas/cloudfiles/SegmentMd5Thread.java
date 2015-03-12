package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.util.common.CloudFilesSegment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.util.common.CloudFilesSegmentContainer;

public class SegmentMd5Thread extends Thread {
    private CloudFilesSegment seg;
    private Exception exception = null;

    @Override
    public void run() {
        try {
            this.seg.computeMd5sum();
        } catch (FileNotFoundException ex) {
            exception = ex;
        } catch (IOException ex) {
            exception = ex;
        } catch (NoSuchAlgorithmException ex) {
            exception = ex;
        }
    }

    public SegmentMd5Thread(CloudFilesSegment seg) {
        this.seg = seg;
    }

    public CloudFilesSegment getSeg() {
        return seg;
    }

    public void setSeg(CloudFilesSegment seg) {
        this.seg = seg;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

        public static CloudFilesSegmentContainer threadedSegmentFile(String filePath, int segmentSize) throws FileNotFoundException {
        CloudFilesSegmentContainer segs = CloudFilesSegmentContainer.newSegmentContainer(filePath, segmentSize);
        List<SegmentMd5Thread> threads = new ArrayList<SegmentMd5Thread>();
        for (CloudFilesSegment seg : segs.getSegments()) {
            SegmentMd5Thread md5Thread = new SegmentMd5Thread(seg);
            threads.add(md5Thread);
            md5Thread.start();
        }
        List<Exception> ex = new ArrayList<Exception>();
        for (SegmentMd5Thread md5Thread : threads) {
            try {
                md5Thread.join();
            } catch (InterruptedException ex1) {
                Logger.getLogger(CloudFilesUtils.class.getName()).log(Level.SEVERE, null, ex1);
                continue;
            }
        }
        return segs;
    }
}
