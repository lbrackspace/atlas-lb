package org.openstack.atlas.jobs;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.events.repository.AlertRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;

public class LoadBalancerDeletionJob extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(LoadBalancerDeletionJob.class);
    private LoadBalancerRepository loadBalancerRepository;
    private AlertRepository alertRepository;

    @Required
    public void setAlertRepository(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Required
    public void setLoadBalancerRepository(LoadBalancerRepository loadBalancerRepository) {
        this.loadBalancerRepository = loadBalancerRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Expired load balancer deletion job started...");

        List<LoadBalancer> elbs;
        try {
            elbs = loadBalancerRepository.getExpiredLbs();
            LOG.info(String.format("There are '%s' expired load balancers...", elbs.size()));
            if (!elbs.isEmpty()) {
                for (LoadBalancer deleteLb : elbs) {
                    try {
                        LOG.info(String.format("Attempting to remove load balancer with id..'%s' from the database... ", deleteLb.getId()));
                        loadBalancerRepository.removeExpiredLb(deleteLb.getId());
                        LOG.info(String.format("Successfully removed load balancer with id..'%s' from the database... ", deleteLb.getId()));
                    } catch (Exception e) {
                        Alert alert = AlertHelper.createAlert(deleteLb.getAccountId(), deleteLb.getId(), e, AlertType.DATABASE_FAILURE.name(), e.getMessage());
                        alertRepository.save(alert);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("There was a problem deleting a load balancer...", e);
        }
        LOG.info("Expired load balancer deletion job completed.");
    }
}
