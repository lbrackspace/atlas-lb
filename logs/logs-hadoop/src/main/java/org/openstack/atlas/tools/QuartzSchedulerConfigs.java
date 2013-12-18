package org.openstack.atlas.tools;

import java.util.ArrayList;
import org.openstack.atlas.logs.hadoop.util.Constants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openstack.atlas.config.CloudFilesZipInfo;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

public class QuartzSchedulerConfigs {

    private String runTime;
    private String rawlogsFileTime;
    private String inputString;
    private String fileMoveInput;
    private List<String> inputForMultiPathJobs;
    private List<CloudFilesZipInfo> cloudFilesZipInfoList;
    private String jobJarPath;
    private boolean lzoInput;

    @Override
    public String toString() {
        return "QuartzSchedulerConfigs{runTime=" + runTime
                + ", rawlogsFileTime=" + rawlogsFileTime
                + ", inputString=" + inputString
                + ", fileMoveInput=" + fileMoveInput
                + ", inputForMultiPathJobs=" + StaticStringUtils.collectionToString(inputForMultiPathJobs, ",")
                + ", jobJarPath=" + jobJarPath
                + ", lzoInput=" + lzoInput
                + ", cloudFilesZipList ={" + cloudFilesZipFileListToString() + "}"
                + "}";
    }

    private String cloudFilesZipFileListToString() {
        if (cloudFilesZipInfoList == null) {
            return "null";
        }

        long uncompressedSize = 0;
        long nLines = 0;
        int nFiles = 0;
        for (CloudFilesZipInfo zipInfo : cloudFilesZipInfoList) {
            uncompressedSize += zipInfo.getUncompressedSize();
            nFiles += 1;
            nLines += zipInfo.getnLines();
        }

        return "nLines=" + nLines
                + ", nFiles=" + nFiles
                + ", uncompressedSize=" + uncompressedSize;
    }

    private static List<String> createInputForMultiPathJobs(Map values) {
        Object val = values.get(Constants.COPY_ALL_FILES);
        if (val instanceof List) {
            return (List<String>) val;
        } else if (val instanceof Object[]) {
            List returnVals = new LinkedList();
            for (Object o : (Object[]) val) {
                if (o instanceof String) {
                    returnVals.add((String) o);
                } else {
                    returnVals.add(o.toString());
                }
                return returnVals;
            }
        }
        return null;
    }

    public Map createMapOutputOfValues() {

        Map map = new HashMap();
        map.put(Constants.FORMATTED_RUNTIME, getRunTime());
        map.put(Constants.INPUT_DIR, getInputString());
        map.put(Constants.FILEDATE, getRawlogsFileTime());
        map.put(Constants.COPY_ALL_FILES, getInputForMultiPathJobs());
        map.put(Constants.JOBJAR_PATH, getJobJarPath());
        map.put(Constants.CLOUDFILES_ZIP_INFO, getCloudFilesZipInfoList());
        map.put(Constants.INPUT_TYPE, isLzoInput());

        return map;
    }

    public static QuartzSchedulerConfigs createSchedulerConfigsFromMap(Map values) {
        QuartzSchedulerConfigs schedulerConfigs = new QuartzSchedulerConfigs();
        schedulerConfigs.setRunTime((String) values.get(Constants.FORMATTED_RUNTIME));
        schedulerConfigs.setInputString((String) values.get(Constants.INPUT_DIR));
        schedulerConfigs.setRawlogsFileTime((String) values.get(Constants.FILEDATE));
        schedulerConfigs.setCloudFilesZipInfoList((List<CloudFilesZipInfo>) values.get(Constants.CLOUDFILES_ZIP_INFO));
        schedulerConfigs.setInputForMultiPathJobs(createInputForMultiPathJobs(values));
        schedulerConfigs.setJobJarPath((String) values.get(Constants.JOBJAR_PATH));

        if (values.get(Constants.INPUT_TYPE) == null) {
            schedulerConfigs.setLzoInput(false);
        } else {
            schedulerConfigs.setLzoInput((Boolean) values.get(Constants.INPUT_TYPE));
        }

        return schedulerConfigs;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getRawlogsFileTime() {
        return rawlogsFileTime;
    }

    public void setRawlogsFileTime(String rawlogsFileTime) {
        this.rawlogsFileTime = rawlogsFileTime;
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public String getFileMoveInput() {
        return fileMoveInput;
    }

    public void setFileMoveInput(String fileMoveInput) {
        this.fileMoveInput = fileMoveInput;
    }

    public List<String> getInputForMultiPathJobs() {
        return inputForMultiPathJobs;
    }

    public void setInputForMultiPathJobs(List<String> inputForMultiPathJobs) {
        this.inputForMultiPathJobs = inputForMultiPathJobs;
    }

    public String getJobJarPath() {
        return jobJarPath;
    }

    public void setJobJarPath(String jobJarPath) {
        this.jobJarPath = jobJarPath;
    }

    public boolean isLzoInput() {
        return lzoInput;
    }

    public void setLzoInput(boolean lzoInput) {
        this.lzoInput = lzoInput;
    }

    public List<CloudFilesZipInfo> getCloudFilesZipInfoList() {
        if (cloudFilesZipInfoList == null) {
            cloudFilesZipInfoList = new ArrayList<CloudFilesZipInfo>();
        }
        return cloudFilesZipInfoList;
    }

    public void setCloudFilesZipInfoList(List<CloudFilesZipInfo> cloudFilesZipInfoList) {
        this.cloudFilesZipInfoList = cloudFilesZipInfoList;
    }
}
