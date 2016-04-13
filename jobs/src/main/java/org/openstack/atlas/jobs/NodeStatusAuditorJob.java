package org.openstack.atlas.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openstack.atlas.service.domain.entities.JobName;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobState;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.pojos.NodeStatusReport;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.snmp.SnmpNodeKey;
import org.openstack.atlas.util.snmp.SnmpNodeStatus;
import org.openstack.atlas.util.snmp.SnmpNodeStatusThread;
import org.openstack.atlas.util.snmp.StingraySnmpClient;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NodeStatusAuditorJob extends AbstractJob {

    private static final Integer MAX_UPDATES = null;
    private final Log LOG = LogFactory.getLog(NodeStatusAuditorJob.class);
    private static final String SNMP_PORT = "1161";
    private static final String SNMP_COMMUNITY = "public";
    private static final int NON_REPEATERS = 0;
    private static final int MAX_RETRIES = 13;
    private static final int MAX_REPITIONS = 1000;
    @Autowired
    protected NodeService nodeService;
    @Autowired
    protected HostRepository hostRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.NODE_STATUS_UPDATER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void execute() {
        try {
            run();
        } catch (Exception ex) {
            LOG.error("Error running NODE AUDITOR JOB", ex);
        }
    }

    public void runMe() {
        int nThreads = 0;
        int nThreadsJoined = 0;
        int mangles = 0;
        int identicles = 0;
        if (!jobStateService.isJobReadyToGo()) {
            LOG.info(String.format("THE_ONE_TO_RULE_THEM_ALL is turned off so not running nodeStatusAudit job. Turn it on if you want jobs to run"));
            return;
        }
        JobState state = new JobState();
        state.setJobName(JobName.NODE_STATUS_UPDATER);
        state.setStartTime(Calendar.getInstance());
        state.setState(JobStateVal.IN_PROGRESS);
        state.setStartTime(StaticDateTimeUtils.toCal(StaticDateTimeUtils.nowDateTime(true)));
        jobStateService.saveJobeStateEntry(state);
        NodeStatusReport nsr = nodeService.runNodeStatusAudit();
        int nRows = nsr.getTurnOffline().size() + nsr.getTurnOnline().size();
        String msg = String.format("updateing %d nodeStatus rows", nRows);
        int turnedOn = 0;
        int turnedOff = 0;
        try {
            turnedOn = nodeService.setNodeStatus(nsr.getTurnOnline(), true);
            turnedOff = nodeService.setNodeStatus(nsr.getTurnOffline(), false);
        } catch (Exception ex) {
            String fmt = "Error updating database for node status audit: %s";
            String excMsg = Debug.getExtendedStackTrace(ex);
            msg = String.format(fmt, excMsg);
            LOG.error(msg, ex);
            state.setEndTime(StaticDateTimeUtils.toCal(StaticDateTimeUtils.nowDateTime(true)));
            state.setState(JobStateVal.FAILED);
            jobStateService.saveJobeStateEntry(state);
        }
        nsr.setNodesUpdated(turnedOn + turnedOff);
        nsr.setTurnedOffline(turnedOff);
        nsr.setTurnedOnline(turnedOn);
        state.setInputPath(nsr.toString());
        state.setEndTime(StaticDateTimeUtils.toCal(StaticDateTimeUtils.nowDateTime(true)));
        state.setState(JobStateVal.FINISHED);
        jobStateService.saveJobeStateEntry(state);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void run() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
