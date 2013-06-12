package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;

public class EventsDeletionJob extends Job {
    private final Log LOG = LogFactory.getLog(EventsDeletionJob.class);
    LoadBalancerEventRepository loadBalancerEventRepository;

    @Required
    public void setLoadBalancerEventRepository(LoadBalancerEventRepository loadBalancerEventRepository) {
        this.loadBalancerEventRepository = loadBalancerEventRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Atom events deletion job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.ATOM_EVENTS_DELETION_JOB, JobStateVal.IN_PROGRESS);

        try {
            //LoadBalancerEvent
            LOG.info("Loadbalancer events deletion job started...");
            LOG.info(String.format("Attempting to remove loadbalancer events from the database... "));
            loadBalancerEventRepository.removeLoadBalancerEventEntries();
            LOG.info("LoadBalancer events deletion job completed.");

            //LoadBalancerServiceEvent
            LOG.info("Loadbalancer service events deletion job started...");
            LOG.info(String.format("Attempting to remove loadbalancer service events from the database... "));
            loadBalancerEventRepository.removeLoadBalancerServiceEventEntries();
            LOG.info("LoadBalancer service events deletion job completed.");

            //NodeEvent
            LOG.info("Node events deletion job started...");
            LOG.info(String.format("Attempting to remove node events from the database... "));
            loadBalancerEventRepository.removeNodeEventEntries();
            LOG.info("Node events deletion job completed.");

            //NodeServiceEvent
            LOG.info("Node service events deletion job started...");
            LOG.info(String.format("Attempting to remove old node service events from the database... "));
            loadBalancerEventRepository.removeNodeServiceEventEntries();
            LOG.info("Node service events deletion job completed.");

            //VirtualIpEvent
            LOG.info("Virtual ip events deletion job started...");
            LOG.info(String.format("Attempting to remove virtual ip events from the database... "));
            loadBalancerEventRepository.removeVirtualIpEventEntries();
            LOG.info("Virtual ip events deletion job completed.");

            //ConnectionLimitEvent
            LOG.info("Connection limit events deletion job started...");
            LOG.info(String.format("Attempting to remove connection limit events from the database... "));
            loadBalancerEventRepository.removeConnectionLimitEventEntries();
            LOG.info("Connection limit events deletion job completed.");

            //SessionPersistenceEvent
            LOG.info("Session persistence events deletion job started...");
            LOG.info(String.format("Attempting to remove session persistence events from the database... "));
            loadBalancerEventRepository.removeSessionPersistenceEventEntries();
            LOG.info("Session persistence events deletion job completed.");

            //AccessListEvent
            LOG.info("Access list events deletion job started...");
            LOG.info(String.format("Attempting to remove access list events from the database... "));
            loadBalancerEventRepository.removeAccessListEventEntries();
            LOG.info("Access list events deletion job completed.");

            //HealthMonitorEvent
            LOG.info("Health monitor events deletion job started...");
            LOG.info(String.format("Attempting to remove health monitor events from the database... "));
            loadBalancerEventRepository.removeHealthMonitorEventEntries();
            LOG.info("Health monitor events deletion job completed.");
        } catch (Exception e) {
            jobStateService.updateJobState(JobName.ATOM_EVENTS_DELETION_JOB, JobStateVal.FAILED);
            LOG.error(String.format("Failed while removing one of the event entries: %s", e.getMessage()));
            Alert alert = AlertHelper.createAlert(1, 1, e, AlertType.API_FAILURE.name(), "Failed removing an event entry...");
            alertRepository.save(alert);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.ATOM_EVENTS_DELETION_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Atom events deletion job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
