package org.openstack.atlas.tools;

import org.openstack.atlas.util.Constants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openstack.atlas.util.StaticStringUtils;

public class QuartzSchedulerConfigs {

    private String runTime;
    private String rawlogsFileTime;
    private String inputString;
    private String fileMoveInput;
    private List<String> inputForMultiPathJobs;
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
                + ", lzoInput=" + lzoInput + '}';
    }

    public static QuartzSchedulerConfigs createSchedulerConfigsFromMap(Map values) {
        QuartzSchedulerConfigs schedulerConfigs = new QuartzSchedulerConfigs();
        schedulerConfigs.setRunTime((String) values.get(Constants.FORMATTED_RUNTIME));
        schedulerConfigs.setInputString((String) values.get(Constants.INPUT_DIR));
        schedulerConfigs.setRawlogsFileTime((String) values.get(Constants.FILEDATE));

        schedulerConfigs.setInputForMultiPathJobs(createInputForMultiPathJobs(values));
        schedulerConfigs.setJobJarPath((String) values.get(Constants.JOBJAR_PATH));

        if (values.get(Constants.INPUT_TYPE) == null) {
            schedulerConfigs.setLzoInput(false);
        } else {
            schedulerConfigs.setLzoInput((Boolean) values.get(Constants.INPUT_TYPE));
        }

        return schedulerConfigs;
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

        map.put(Constants.INPUT_TYPE, isLzoInput());

        return map;
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
}
