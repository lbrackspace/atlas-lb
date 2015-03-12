package org.openstack.atlas.cloudfiles;

import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudFilesSegmentContainer {

    private String fileName;
    private List<CloudFilesSegment> segments;
    private long segmentSize;

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

    public long getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(long segmentSize) {
        this.segmentSize = segmentSize;
    }

    public static CloudFilesSegmentContainer newContainer(String fileName, long segmentSize) {
        File file = new File(StaticFileUtils.expandUser(fileName));
        CloudFilesSegmentContainer container = new CloudFilesSegmentContainer();
        List<CloudFilesSegment> segments = container.getSegments();
        container.setFileName(fileName);
        container.setSegmentSize(segmentSize);
        long offset = 0;
        long nBytesLeft = file.length();
        long nBytes = 0;
        while (nBytesLeft > 0) {
            nBytes = (nBytesLeft < segmentSize) ? nBytesLeft : segmentSize;
            CloudFilesSegment segment = new CloudFilesSegment();
            segment.setFileName(fileName);
            segment.setOffset(offset);
            segment.setSize(nBytes);
            offset += nBytes;
            nBytesLeft -= nBytes;
            segments.add(segment);
        }
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
}
