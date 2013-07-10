package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoadBalancerDeletionJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerDeletionJob.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private SslTerminationRepository sslTerminationRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.LB_DELETION_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        try {
            List<LoadBalancer> elbs = loadBalancerRepository.getExpiredLbs();
            LOG.info(String.format("There are '%s' expired load balancers...", elbs.size()));
            if (!elbs.isEmpty()) {
                for (LoadBalancer deleteLb : elbs) {
                    LOG.info(String.format("Attempting to remove load balancer with id..'%s' from the database... ", deleteLb.getId()));
                    //TODO: for legacy bug, remove user_page to prevent manual intervention. once caught up the following line(1) can be removed
                    loadBalancerRepository.removeErrorPage(deleteLb.getId(), deleteLb.getAccountId());
                    try {
                        sslTerminationRepository.removeSslTermination(deleteLb.getId(), deleteLb.getAccountId());
                    } catch (Exception e) {
                        LOG.debug("SSL Termination is not found for load balancer: " + deleteLb.getId());
                    }
                    loadBalancerRepository.removeExpiredLb(deleteLb.getId());
                }
            }
        } catch (Exception e) {
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            throw e;
        }
    }

    @Override
    public void cleanup() {
    }

}
