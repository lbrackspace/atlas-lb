package org.openstack.atlas.rax.jobs.usage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.jobs.helper.HostConfigHelper;
import org.openstack.atlas.jobs.usage.*;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openstack.atlas.jobs.batch.BatchExecutor.executeInBatches;

@Primary
@Component
public class RaxLoadBalancerUsagePollerThread extends LoadBalancerUsagePollerThread {
    private final Log LOG = LogFactory.getLog(RaxLoadBalancerUsagePollerThread.class);

    @Override
    public void run() {
        try {
            final LoadBalancerEndpointConfiguration config = HostConfigHelper.getConfig(host, hostRepository);
            List<LoadBalancer> loadBalancersForHost = hostRepository.getUsageLoadBalancersWithStatus(host.getId(), CoreLoadBalancerStatus.ACTIVE);

            RaxUsageCollector usageCollector = new RaxUsageCollector(config, usageAdapter);
            executeInBatches(loadBalancersForHost, BATCH_SIZE, usageCollector);

            RaxUsageProcessor usageProcessor = new RaxUsageProcessor(usageRepository, usageCollector.getBytesInMap(), usageCollector.getBytesOutMap(), usageCollector.getCurrentConnectionMap());
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
}
