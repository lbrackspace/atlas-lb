package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.HostStatus;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class LoadBalancerUsagePoller extends Job implements StatefulJob {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);
    private final int BATCH_SIZE = 100;

    @Autowired
    private UsageAdapter usageAdapter;
    @Autowired
    private LoadBalancerRepository loadBalancerRepository;
    @Autowired
    private HostRepository hostRepository;
    @Autowired
    private UsageRepository usageRepository;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        startUsagePoller();
    }

    private void startUsagePoller() {
        Calendar startTime = Calendar.getInstance();
        LOG.info(String.format("Load balancer usage poller job started at %s (Timezone: %s)", startTime.getTime(), startTime.getTimeZone().getDisplayName()));

        List<Host> hosts;
        List<LoadBalancerUsagePollerThread> threads = new ArrayList<LoadBalancerUsagePollerThread>();

        try {
            hosts = hostRepository.getActiveHosts();
        } catch (Exception ex) {
            LOG.error(ex.getCause(), ex);
            return;
        }

        for (final Host host : hosts) {
            LoadBalancerUsagePollerThread thread = new LoadBalancerUsagePollerThread(loadBalancerRepository, host.getName() + "-poller-thread", host, usageAdapter, hostRepository, usageRepository);
            threads.add(thread);
            thread.start();
        }

        for (LoadBalancerUsagePollerThread thread : threads) {
            try {
                thread.join();
                LOG.debug(String.format("Load balancer usage poller thread '%s' completed.", thread.getName()));
            } catch (InterruptedException e) {
                LOG.error(String.format("Load balancer usage poller thread interrupted for thread '%s'", thread.getName()), e);
                e.printStackTrace();
            }
        }

        Calendar endTime = Calendar.getInstance();
        Double elapsedMins = ((endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0) / 60.0;
        LOG.info(String.format("Usage poller job completed at '%s' (Total Time: %f mins)", endTime.getTime(), elapsedMins));
    }

}