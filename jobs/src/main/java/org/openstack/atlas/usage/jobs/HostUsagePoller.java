package org.openstack.atlas.usage.jobs;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.service.ReverseProxyLoadBalancerAdapter;
import org.openstack.atlas.jobs.AbstractJob;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.JobName;
import org.openstack.atlas.service.domain.entities.JobStateVal;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;
import org.openstack.atlas.service.domain.usage.repository.HostUsageRepository;
import org.openstack.atlas.usage.helpers.HostConfigHelper;
import org.openstack.atlas.util.crypto.exception.DecryptException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.ConnectException;
import java.util.Calendar;
import java.util.List;

@Component
public class HostUsagePoller extends AbstractJob {
    private final Log LOG = LogFactory.getLog(HostUsagePoller.class);

    @Autowired
    private ReverseProxyLoadBalancerAdapter reverseProxyLoadBalancerAdapter;
    @Autowired
    private HostRepository hostRepository;
    @Autowired
    private HostUsageRepository hostUsageRepository;

    @Override
    public Log getLogger() {
        return LOG;
    }

    @Override
    public JobName getJobName() {
        return JobName.HOST_USAGE_POLLER;
    }

    @Override
    public void setup(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    }

    @Override
    public void run() throws Exception {
        List<Host> hosts = hostRepository.getAll();

        for (final Host host : hosts) {
            try {
                final LoadBalancerEndpointConfiguration config = HostConfigHelper.getConfig(host, hostRepository);
                Calendar pollTime = Calendar.getInstance();

                LOG.debug(String.format("Retrieving host bytes in from '%s' (%s)...", host.getName(), host.getEndpoint()));
                long hostBytesIn = reverseProxyLoadBalancerAdapter.getHostBytesIn(config);
                LOG.debug(String.format("Retrieving host bytes out from '%s' (%s)...", host.getName(), host.getEndpoint()));
                long hostBytesOut = reverseProxyLoadBalancerAdapter.getHostBytesOut(config);

                LOG.info(String.format("Host Name: '%s', Bandwidth In: %d, Bandwidth Out: %d", host.getName(), hostBytesIn, hostBytesOut));

                LOG.debug(String.format("Saving usage snapshot for '%s' (%s)...", host.getName(), host.getEndpoint()));
                addRecordForHost(host, hostBytesIn, hostBytesOut, pollTime);
                LOG.debug(String.format("Usage snapshot successfully saved."));
            } catch (DecryptException de) {
                LOG.error(String.format("Error decrypting configuration for '%s' (%s)", host.getName(), host.getEndpoint()), de);
            } catch (AxisFault af) {
                if (af.getCause() instanceof ConnectException) {
                    LOG.error(String.format("Error connecting to '%s' (%s). Skipping...", host.getName(), host.getEndpoint()));
                } else {
                    LOG.error("Axis Fault Exception caught", af);
                    af.printStackTrace();
                }
            } catch (Exception e) {
                LOG.error("Exception caught", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cleanup() {
    }

    private void addRecordForHost(Host host, long hostBytesIn, long hostBytesOut, Calendar pollTime) {
        HostUsage hostUsage = new HostUsage();
        hostUsage.setHostId(host.getId());
        hostUsage.setBandwidthBytesIn(hostBytesIn);
        hostUsage.setBandwidthBytesOut(hostBytesOut);
        hostUsage.setSnapshotTime(pollTime);
        hostUsageRepository.save(hostUsage);
    }

}
