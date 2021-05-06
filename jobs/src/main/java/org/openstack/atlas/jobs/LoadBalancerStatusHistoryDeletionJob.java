package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SslTerminationRepository;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.services.impl.LoadBalancerStatusHistoryServiceImpl;
import org.openstack.atlas.service.domain.util.Constants;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoadBalancerStatusHistoryDeletionJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerStatusHistoryDeletionJob.class);

    @Autowired
    private LoadBalancerStatusHistoryServiceImpl loadBalancerStatusHistoryService;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.LB_STATUS_HIST_DELETION;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {

            try {
                LOG.info("Loadbalancer status history deletion job started...");
                LOG.info(String.format("Attempting to remove loadbalancer status history from the database... "));
                loadBalancerStatusHistoryService.deleteLBStatusHistoryOlderThanSixMonths();
                LOG.info("LoadBalancer status history deletion job completed.");

                } catch (Exception e) {

                Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), "LoadBalancerStatusHistoryDeletionJob Failure, check Alerts for more information... ");
                alertRepository.save(alert);
                throw  e;
            }

    }

    @Override
    public void cleanup() {
    }

}
