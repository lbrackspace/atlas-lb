package org.openstack.atlas.scheduler.execution.threads;

import java.util.List;
import org.openstack.atlas.cloudfiles.CloudFilesConfig;
import org.openstack.atlas.cloudfiles.CloudFilesUtils;
import org.openstack.atlas.cloudfiles.SegmentMd5Thread;
import org.openstack.atlas.cloudfiles.objs.ResponseContainer;
import org.openstack.atlas.service.domain.entities.CloudFilesLzo;
import org.openstack.atlas.service.domain.entities.HdfsLzo;
import org.openstack.atlas.service.domain.services.LzoService;

import org.openstack.atlas.util.common.CloudFilesSegmentContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticFileUtils;

public class CloudFilesLzoUploadThread extends Thread {

    public CloudFilesLzoUploadThread(LzoService lzoService, CloudFilesUtils cfu, int hourKey, String expandedPath, String cntName) {
        this.lzoService = lzoService;
        this.cfu = cfu;
        this.hourKey = hourKey;
        this.expandedPath = expandedPath;
        this.cntName = cntName;
    }
    private LzoService lzoService;
    private CloudFilesUtils cfu;
    private int hourKey;
    private String expandedPath;
    private String cntName;
    private static final Log LOG = LogFactory.getLog(CloudFilesLzoUploadThread.class);
    private static int totalRuns = 0;
    private static int nRunning = 0;
    private static final Object mutex = new Object();

    public static void incCounts() {
        synchronized (mutex) {
            totalRuns++;
            nRunning++;
        }
    }

    public static int getTotalRuns() {
        int count;
        synchronized (mutex) {
            count = totalRuns;
        }
        return count;
    }

    public static int getNRunning() {
        int count;
        synchronized (mutex) {
            count = nRunning;
        }
        return count;
    }

    public static void decCounts() {
        synchronized (mutex) {
            nRunning--;
        }
    }

    @Override
    public void run() {
        incCounts();
        String excMsg;
        String lzoName = StaticFileUtils.stripDirectoryFromFileName(expandedPath);
        try {
            // Start uploading this new LZO
            LOG.info(String.format("new LZO %s found spinning up MD5 threads\n", expandedPath));
            CloudFilesSegmentContainer sc = SegmentMd5Thread.threadedSegmentFile(expandedPath, CloudFilesConfig.getSegmentSize());
            sc.toString();
            LOG.info(String.format("Computed lzo for file set %s\n", sc.toString()));
            lzoService.setStateFlagsFalse(hourKey, HdfsLzo.NEEDS_MD5);
            List<CloudFilesLzo> lzos = lzoService.newCloudFilesLzo(hourKey, sc);
            LOG.info(String.format("md5 completed for %d %s: %s", hourKey, expandedPath, sc.toString()));
            // Start upload
            cfu.getAuthToken();
            cfu.emptyContainer(cntName);
            cfu.deleteContainer(cntName);
            cfu.createContainer(cntName);
            List<ResponseContainer<Boolean>> uploadResp = cfu.writeSegmentContainer(cntName, sc);
            for (ResponseContainer<Boolean> resp : uploadResp) {
                System.out.printf("upload resp for %d = %s\n", hourKey, resp.toString());
                if (resp == null || resp.getEntity() == null || !resp.getEntity()) {
                    String error = String.format("unable to upload LZO for hour %d scheduling a resend", hourKey);
                    throw new Exception(error);
                }
            }
            lzoService.finishCloudFilesLzo(hourKey, sc);
            // Mark that CloudFiles have been uploaded
            lzoService.setStateFlagsFalse(hourKey, HdfsLzo.NEEDS_CF);
            decCounts();
        } catch (Exception ex) {
            excMsg = Debug.getExtendedStackTrace(ex);
            LOG.error(String.format("Error checking hour %d for lzo %s due to Exception %s", hourKey, expandedPath, excMsg), ex);
            lzoService.setStateFlagsTrue(hourKey, HdfsLzo.NEEDS_REUPLOAD | HdfsLzo.NEEDS_MD5 | HdfsLzo.NEEDS_CF);
            decCounts();
        }
    }
}
