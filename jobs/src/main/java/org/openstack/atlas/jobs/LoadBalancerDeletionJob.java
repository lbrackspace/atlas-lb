package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;
import java.util.List;

public class LoadBalancerDeletionJob extends Job {
    private final Log LOG = LogFactory.getLog(LoadBalancerDeletionJob.class);
    private LoadBalancerRepository loadBalancerRepository;
    private SslTerminationRepository sslTerminationRepository;

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Required
    public void setSslTerminationRepository(SslTerminationRepository sslTerminationRepository) {
        this.sslTerminationRepository = sslTerminationRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load balancer deletion job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));
        jobStateService.updateJobState(JobName.LB_DELETION_JOB, JobStateVal.IN_PROGRESS);

        List<LoadBalancer> elbs;
        try {
            elbs = loadBalancerRepository.getExpiredLbs();
            LOG.info(String.format("There are '%s' expired load balancers...", elbs.size()));
            if (!elbs.isEmpty()) {
                for (LoadBalancer deleteLb : elbs) {
                    try {
                        LOG.info(String.format("Attempting to remove load balancer with id..'%s' from the database... ", deleteLb.getId()));
                        //TODO: for legacy bug, remove user_page to prevent manual intervention. once caught up the following line(1) can be removed
                        loadBalancerRepository.removeErrorPage(deleteLb.getId(), deleteLb.getAccountId());
                        sslTerminationRepository.removeSslTermination(deleteLb.getId(), deleteLb.getAccountId());

                        loadBalancerRepository.removeExpiredLb(deleteLb.getId());
                        LOG.info(String.format("Successfully removed load balancer with id..'%s' from the database... ", deleteLb.getId()));
                    } catch (Exception e) {
                        LOG.debug("SSL Termination is not found for load balancer: " + deleteLb.getId());
                        //No need for alert here, causing logging clutter for no benefits... 11/07/12
//                        Alert alert = AlertHelper.createAlert(deleteLb.getAccountId(), deleteLb.getId(), e, AlertType.DATABASE_FAILURE.name(), e.getMessage());
//                        alertRepository.save(alert);
                    }
                }
            }
        } catch (Exception e) {
            jobStateService.updateJobState(JobName.LB_DELETION_JOB, JobStateVal.FAILED);
            LOG.error(String.format("Load balancer deletion job failed while removing load balancers: %s", e.getMessage()));
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            return;
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        jobStateService.updateJobState(JobName.LB_DELETION_JOB, JobStateVal.FINISHED);
        LOG.info(String.format("Load balancer deletion job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }
}
