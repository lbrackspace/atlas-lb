package org.openstack.atlas.util.common;

import org.openstack.atlas.util.staticutils.StaticFileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.util.common.comparators.CloudFilesSegmentComparator;
import org.openstack.atlas.util.common.CloudFilesSegment;

public class CloudFilesSegmentContainer {

    private static final CloudFilesSegmentComparator segComparator = new CloudFilesSegmentComparator();
    private String fileName;
    private List<CloudFilesSegment> segments;
    private int segmentSize;

    private static Set<Integer> workingHourSet;

    public CloudFilesSegmentContainer() {
        segments = new ArrayList<CloudFilesSegment>();
        workingHourSet = new HashSet<Integer>();
    }

    public static boolean setWorkingHour(int hourKey) {
        boolean isAlreadyWorkingOnHour;
        synchronized (CloudFilesSegmentContainer.class) {
            isAlreadyWorkingOnHour = workingHourSet.contains(hourKey);
            if (isAlreadyWorkingOnHour) {
                return false;
            }
            workingHourSet.add(hourKey);
            return true;
        }
    }

    public static void clearWorkingHour(int hourKey) {
        synchronized (CloudFilesSegmentContainer.class) {
            workingHourSet.remove(hourKey);
        }
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



    public int getSegmentSize() {
        return segmentSize;
    }

    public void setSegmentSize(int segmentSize) {
        this.segmentSize = segmentSize;
    }
}
