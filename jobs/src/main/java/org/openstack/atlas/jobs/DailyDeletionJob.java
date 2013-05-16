package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.services.helpers.AlertHelper;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Component
public class DailyDeletionJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(DailyDeletionJob.class);

    @Autowired
    private LoadBalancerUsageRepository hourlyUsageRepository;
    @Autowired
    private HostUsageRepository hostUsageRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.DAILY_DELETION_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        try {
            deleteLoadBalancerUsageRecords();
            deleteHostUsageRecords();
        } catch (Exception e) {
            Alert alert = AlertHelper.createAlert(null, null, e, AlertType.API_FAILURE.name(), e.getMessage());
            alertRepository.save(alert);
            throw e;
        }
    }

    @Override
    public void cleanup() {
    }

    private void deleteLoadBalancerUsageRecords() {
        LOG.info("Deleting old loadbalancer usage records...");
        hourlyUsageRepository.deleteOldRecords();
        LOG.info("Completed deleting old loadbalancer usage records.");
    }

    private void deleteHostUsageRecords() {
        LOG.info("Deleting old host usage records...");
        hostUsageRepository.deleteOldRecords();
        LOG.info("Completed deleting old host usage records.");
    }

}
