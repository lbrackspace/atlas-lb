package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.jobs.JobInterface;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class LoadBalancerUsagePoller implements JobInterface {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePoller.class);

    @Autowired
    LoadBalancerUsagePollerThread hostThread;
    @Autowired
    UsageAdapter usageAdapter;
    @Autowired
    HostRepository hostRepository;
    @Autowired
    UsageRepository usageRepository;

    @Override
    public void init(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.debug("INIT");
    }

    @Override
    public void execute() throws JobExecutionException {
        LOG.debug("EXECUTE");
        startUsagePoller();
    }

    @Override
    public void destroy() {
        LOG.debug("DESTROY");
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
            final LoadBalancerUsagePollerThread thread = createHostThread(host);
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

    protected LoadBalancerUsagePollerThread createHostThread(Host host) {
        try {
            final LoadBalancerUsagePollerThread thread = hostThread.getClass().newInstance();
            thread.setName(host.getName() + "-poller-thread");
            thread.setHost(host);
            thread.setHostRepository(hostRepository);
            thread.setUsageRepository(usageRepository);
            thread.setUsageAdapter(usageAdapter);
            return thread;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}