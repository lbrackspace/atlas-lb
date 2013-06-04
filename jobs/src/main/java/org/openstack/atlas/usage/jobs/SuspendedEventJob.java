package org.openstack.atlas.usage.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.repository.LoadBalancerMergedHostUsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

public class SuspendedEventJob extends AbstractJob {
    private final Log LOG = LogFactory.getLog(SuspendedEventJob.class);

    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private LoadBalancerService loadBalancerService;
    @Autowired
    private LoadBalancerMergedHostUsageRepository lbMergedHostUsageRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.SUSPENDED_LB_JOB;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        final Calendar now = Calendar.getInstance();
        List<LoadBalancer> suspendedLoadBalancers = loadBalancerRepository.getLoadBalancersWithStatus(LoadBalancerStatus.SUSPENDED);

        for (LoadBalancer suspendedLoadBalancer : suspendedLoadBalancers) {
            BitTags tags = loadBalancerService.getCurrentBitTags(suspendedLoadBalancer.getId());
            LoadBalancerMergedHostUsage newSuspendedEvent = new LoadBalancerMergedHostUsage(suspendedLoadBalancer.getAccountId(), suspendedLoadBalancer.getId(), 0l, 0l, 0l, 0l, 0, 0, suspendedLoadBalancer.getLoadBalancerJoinVipSet().size(), tags.toInt(), now, UsageEvent.SUSPENDED_LOADBALANCER);
            LOG.debug(String.format("Adding suspended usage event for load balancer '%d'...", suspendedLoadBalancer.getId()));
            lbMergedHostUsageRepository.create(newSuspendedEvent);
        }
    }

    @Override
    public void cleanup() {
    }
}
