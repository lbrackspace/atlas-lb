package org.openstack.atlas.cloudfiles.objs;

import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.cloudfiles.CloudFilesUtils;
import org.openstack.atlas.cloudfiles.SegmentMd5Thread;
import org.openstack.atlas.cloudfiles.objs.comparators.CloudFilesSegmentComparator;

public class CloudFilesSegmentContainer {

    private static final CloudFilesSegmentComparator segComparator = new CloudFilesSegmentComparator();
    private String fileName;
    private List<CloudFilesSegment> segments;
    private int segmentSize;

    CloudFilesSegmentContainer() {
        segments = new ArrayList<CloudFilesSegment>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<CloudFilesSegment> getSegments() {
        if (segments == null) {
            segments = new ArrayList<CloudFilesSegment>();
        }
        return segments;
    }

    public void setSegments(List<CloudFilesSegment> segments) {
        this.segments = segments;
    }

    public static CloudFilesSegmentContainer newSegmentContainer(String fileName, int segmentSize) throws FileNotFoundException {
        File file = new File(StaticFileUtils.expandUser(fileName));
        CloudFilesSegmentContainer container = new CloudFilesSegmentContainer();
        List<CloudFilesSegment> segments = container.getSegments();
        container.setFileName(fileName);
        container.setSegmentSize(segmentSize);
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException(String.format("%s not found", fileName));
        }
        int fragNumber = 0;
        long offset = 0;
        long nBytesLeft = file.length();
        int nBytes = 0;
        while (nBytesLeft > 0) {
            nBytes = (nBytesLeft < segmentSize) ? (int) nBytesLeft : segmentSize;
            CloudFilesSegment segment = new CloudFilesSegment();
            segment.setFileName(fileName);
            segment.setOffset(offset);
            segment.setSize(nBytes);
            segment.setFragNumber(fragNumber);
            fragNumber++;
            offset += nBytes;
            nBytesLeft -= nBytes;
            segments.add(segment);
        }
        Collections.sort(segments, segComparator);
        return container;
    }

    @Override
    public String toString() {
        int i;

        StringBuilder sb = new StringBuilder();
        sb.append("CloudFilesSegmentContainer{fileName=").append(fileName).append(", segmentSize=").append(segmentSize).append(",segments=[");

        for (i = 0; i < segments.size(); i++) {
            sb.append(segments.get(i)).append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static CloudFilesSegmentContainer threadedSegmentFile(String filePath, int segmentSize) throws FileNotFoundException {
        CloudFilesSegmentContainer segs = CloudFilesSegmentContainer.newSegmentContainer(filePath, segmentSize);
        List<SegmentMd5Thread> threads = new ArrayList<SegmentMd5Thread>();
        for (CloudFilesSegment seg : segs.getSegments()) {
            SegmentMd5Thread md5Thread = new SegmentMd5Thread(seg);
            md5Thread.start();
            threads.add(md5Thread);
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

    public int getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(int segmentSize) {
        this.segmentSize = segmentSize;
    }
}
