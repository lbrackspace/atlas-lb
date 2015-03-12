package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.cloudfiles.objs.CloudFilesSegment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
}
