package org.openstack.atlas.tools;

import org.openstack.atlas.constants.Constants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HadoopRunner {

    private String inputDumpString;

    private String inputForJoinJob;

    private String inputString;

    private String jobJarPath;
    private String cachedConstant;
    private String apacheConstant;
    private String dynamicConstant;
    private String cacheServers;

    private String oldestDate;
    private String inputOrderJob;

    private List<String> inputForMultiPathJobs;
    private String cacheIPs;
    private String ccAnalyzerFqdn;
    private String rawlogsBatchJobInput;
    private String fileMoveInput;
    private String container;
    private String rawlogsFileDate;
    private String urchinFile;
    private String urchinFqdn;
    private boolean lzoInput;
    private String ccAnalyzerFile;

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getOldestDate() {
        return oldestDate;
    }

    public void setOldestDate(String oldestDate) {
        this.oldestDate = oldestDate;
    }

    public String getInputOrderJob() {
        return inputOrderJob;
    }

    public void setInputOrderJob(String inputOrderJob) {
        this.inputOrderJob = inputOrderJob;
    }

    public String getInputDumpString() {
        return inputDumpString;
    }

    public String getInputForJoinJob() {
        return inputForJoinJob;
    }

    public String getInputString() {
        return inputString;
    }

    public String getJobJarPath() {
        return jobJarPath;
    }

    public void setInputDumpString(String inputDumpString) {
        this.inputDumpString = inputDumpString;
    }

    public void setInputForJoinJob(String inputForJoinJob) {
        this.inputForJoinJob = inputForJoinJob;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    public void setJobJarPath(String jobJarPath) {
        this.jobJarPath = jobJarPath;
    }

    public List<String> getInputForMultiPathJobs() {
        return inputForMultiPathJobs;
    }

    public void setInputForMultiPathJobs(List<String> inputForMultiPathJobs) {
        this.inputForMultiPathJobs = inputForMultiPathJobs;
    }

    public void setFileMoveInput(String fileMoveInput) {
        this.fileMoveInput = fileMoveInput;
    }

    public String getFileMoveInput() {
        return fileMoveInput;
    }

    public static HadoopRunner createRunnerFromValues(Map values) {
        HadoopRunner runner = new HadoopRunner();
        runner.setInputString((String) values.get(Constants.BaseMapreduceJob.FORMATTED_RUNTIME));
        runner.setInputDumpString((String) values.get(Constants.BaseMapreduceJob.BASELINE_DUMP_JOIN_FILE));
        runner.setInputForJoinJob((String) values.get(Constants.BaseMapreduceJob.LOG_JOIN_FILE));
        runner.setInputOrderJob((String) values.get(Constants.BaseMapreduceJob.LOG_ORDER_FILE));
        runner.setOldestDate((String) values.get(Constants.BaseMapreduceJob.OLDEST_LOGDATE));
        runner.setRawlogsBatchJobInput((String) values.get(Constants.Rawlogs.INPUT_DIR));
        runner.setRawlogsFileDate((String) values.get(Constants.Rawlogs.FILEDATE));

        //        (Constants.BaseMapreduceJob.COPY_ALL_FILES)
        runner.setInputForMultiPathJobs(createInputForMultiPathJobs(values));
        runner.setFileMoveInput((String) values.get(Constants.BaseMapreduceJob.COPY_SINGLE_FILE));
        runner.setContainer((String) values.get(Constants.CloudFiles.CONTAINER_NAME));
        runner.setJobJarPath((String) values.get(Constants.JOBJAR_PATH));

        runner.setUrchinFile((String) values.get(Constants.Urchin.FILE_NAME));
        runner.setUrchinFqdn((String) values.get(Constants.Urchin.FQDN));

        if (values.get(Constants.BaseMapreduceJob.INPUT_TYPE) == null) {
            runner.setLzoInput(false);
        } else {
            runner.setLzoInput((Boolean) values.get(Constants.BaseMapreduceJob.INPUT_TYPE));
        }

        return runner;
    }

    private static List<String> createInputForMultiPathJobs(Map values) {
        Object val = values.get(Constants.BaseMapreduceJob.COPY_ALL_FILES);
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

        map.put(Constants.BaseMapreduceJob.FORMATTED_RUNTIME, getInputString());
        map.put(Constants.BaseMapreduceJob.BASELINE_DUMP_JOIN_FILE, getInputDumpString());
        map.put(Constants.BaseMapreduceJob.LOG_JOIN_FILE, getInputForJoinJob());
        map.put(Constants.BaseMapreduceJob.LOG_ORDER_FILE, getInputOrderJob());
        map.put(Constants.BaseMapreduceJob.OLDEST_LOGDATE, getOldestDate());
        map.put(Constants.Rawlogs.INPUT_DIR, getRawlogsBatchJobInput());
        map.put(Constants.Rawlogs.FILEDATE, getRawlogsFileDate());
        map.put(Constants.BaseMapreduceJob.COPY_ALL_FILES, getInputForMultiPathJobs());
        map.put(Constants.BaseMapreduceJob.COPY_SINGLE_FILE, getFileMoveInput());
        map.put(Constants.CloudFiles.CONTAINER_NAME, getContainer());
        map.put(Constants.JOBJAR_PATH, getJobJarPath());

        map.put(Constants.Urchin.FILE_NAME, getUrchinFile());
        map.put(Constants.Urchin.FQDN, getUrchinFqdn());

        map.put(Constants.BaseMapreduceJob.INPUT_TYPE, isLzoInput());

        return map;
    }

    private void setCcAnalyzerFile(String ccAnalyzerFile) {
        this.ccAnalyzerFile = ccAnalyzerFile;
    }

    public String getCcAnalyzerFile() {
        return ccAnalyzerFile;
    }

    public void setApacheConstant(String apacheConstant) {
        this.apacheConstant = apacheConstant;
    }

    public void setDynamicConstant(String dynamicConstant) {
        this.dynamicConstant = dynamicConstant;
    }

    public void setCachedConstant(String cachedConstant) {
        this.cachedConstant = cachedConstant;
    }

    public String getCachedConstant() {
        return cachedConstant;
    }

    public String getApacheConstant() {
        return apacheConstant;
    }

    public String getDynamicConstant() {
        return dynamicConstant;
    }

    public String getCacheServers() {
        return cacheServers;
    }

    public void setCacheServers(String cacheServers) {
        this.cacheServers = cacheServers;
    }

    public void setCacheIPs(String cacheIPs) {
        this.cacheIPs = cacheIPs;
    }

    public String getCacheIPs() {
        return cacheIPs;
    }

    public String getCcAnalyzerFqdn() {
        return ccAnalyzerFqdn;
    }

    public void setCcAnalyzerFqdn(String ccAnalyzerFqdn) {
        this.ccAnalyzerFqdn = ccAnalyzerFqdn;
    }

    public void setRawlogsBatchJobInput(String rawlogsBatchJobInput) {
        this.rawlogsBatchJobInput = rawlogsBatchJobInput;
    }

    public String getRawlogsBatchJobInput() {
        return rawlogsBatchJobInput;
    }

    public String getRawlogsFileDate() {
        return rawlogsFileDate;
    }

    public void setRawlogsFileDate(String rawlogsFileDate) {
        this.rawlogsFileDate = rawlogsFileDate;
    }

    public String getUrchinFile() {
        return urchinFile;
    }

    public void setUrchinFile(String urchinFile) {
        this.urchinFile = urchinFile;
    }

    public String getUrchinFqdn() {
        return urchinFqdn;
    }

    public void setUrchinFqdn(String urchinFqdn) {
        this.urchinFqdn = urchinFqdn;
    }

    public boolean isLzoInput() {
        return lzoInput;
    }

    public void setLzoInput(boolean lzoInput) {
        this.lzoInput = lzoInput;
    }
}


