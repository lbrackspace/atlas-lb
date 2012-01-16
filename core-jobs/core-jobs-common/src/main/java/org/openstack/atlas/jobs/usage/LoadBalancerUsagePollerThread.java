package org.openstack.atlas.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.jobs.helper.HostConfigHelper;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openstack.atlas.jobs.batch.BatchExecutor.executeInBatches;

/*
 *  Thread per host machine
 */
@Component
public class LoadBalancerUsagePollerThread extends Thread {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePollerThread.class);
    protected final int BATCH_SIZE = 100; // TODO: Externalize

    protected UsageAdapter usageAdapter;
    protected UsageRepository usageRepository;
    protected HostRepository hostRepository;
    protected Host host;

    public LoadBalancerUsagePollerThread() {
        super();
    }

    public LoadBalancerUsagePollerThread(String threadName, Host host, UsageAdapter usageAdapter, HostRepository hostRepository, UsageRepository usageRepository) {
        super(threadName);
        this.host = host;
        this.usageAdapter = usageAdapter;
        this.usageRepository = usageRepository;
        this.hostRepository = hostRepository;
    }

    @Override
    public void run() {
        try {
            final LoadBalancerEndpointConfiguration config = HostConfigHelper.getConfig(host, hostRepository);
            List<LoadBalancer> loadBalancersForHost = hostRepository.getUsageLoadBalancersWithStatus(host.getId(), CoreLoadBalancerStatus.ACTIVE);

            UsageCollector usageCollector = new UsageCollector(config, usageAdapter);
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageCollector);

            UsageProcessor usageProcessor = new UsageProcessor(usageRepository, usageCollector.getBytesInMap(), usageCollector.getBytesOutMap());
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageProcessor);

            UsageInsert usageInsert = new UsageInsert(usageRepository);
            executeInBatches(usageProcessor.getRecordsToInsert(), BATCH_SIZE, usageInsert);

            UsageUpdate usageUpdate = new UsageUpdate(usageRepository);
            executeInBatches(usageProcessor.getRecordsToUpdate(), BATCH_SIZE, usageUpdate);

        } catch (DecryptException de) {
            LOG.error(String.format("Error decrypting configuration for '%s' (%s)", host.getName(), host.getEndpoint()), de);
        } catch (Exception e) {
            LOG.error("Exception caught", e);
            e.printStackTrace();
        }
    }

    public void setUsageAdapter(UsageAdapter usageAdapter) {
        this.usageAdapter = usageAdapter;
    }

    public void setUsageRepository(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public void setHost(Host host) {
        this.host = host;
    }
}
