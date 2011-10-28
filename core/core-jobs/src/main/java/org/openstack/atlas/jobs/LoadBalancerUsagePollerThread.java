package org.openstack.atlas.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.LoadBalancerEndpointConfiguration;
import org.openstack.atlas.adapter.UsageAdapter;
import org.openstack.atlas.adapter.exception.AdapterException;
import org.openstack.atlas.common.crypto.exception.DecryptException;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.jobs.logic.UsagesFromPoll;
import org.openstack.atlas.service.domain.entity.Host;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.repository.UsageRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadBalancerUsagePollerThread extends Thread {
    private final Log LOG = LogFactory.getLog(LoadBalancerUsagePollerThread.class);
    private final int BATCH_SIZE = 100; // TODO: Externalize

    private UsageAdapter usageAdapter;
    private UsageRepository usageRepository;
    private HostRepository hostRepository;
    private Host host;

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

            BatchAction<LoadBalancer> batchAction = new BatchAction<LoadBalancer>() {
                public void execute(List<LoadBalancer> loadBalancers) throws Exception {
                    try {
                        LOG.info(String.format("Retrieving transfer bytes in from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<Integer, Long> bytesInMap = usageAdapter.getTransferBytesIn(config, loadBalancers);

                        LOG.debug("Listing transfer bytes in...");
                        for (Integer loadBalancerId : bytesInMap.keySet()) {
                            LOG.debug(String.format("LB Id: '%d', Transfer Bytes In: %d", loadBalancerId, bytesInMap.get(loadBalancerId)));
                        }

                        LOG.info(String.format("Retrieving transfer bytes out from '%s' (%s)...", host.getName(), host.getEndpoint()));
                        Map<Integer, Long> bytesOutMap = usageAdapter.getTransferBytesOut(config, loadBalancers);

                        LOG.debug("Listing transfer bytes out...");
                        for (Integer loadBalancerId : bytesOutMap.keySet()) {
                            LOG.debug(String.format("LB Id: '%d', Transfer Bytes Out: %d", loadBalancerId, bytesOutMap.get(loadBalancerId)));
                        }

                        updateUsageRecords(loadBalancers, bytesInMap, bytesOutMap);
                    } catch (AdapterException e) {
                        // TODO: Discuss how to handle exceptions better
                        LOG.error("Adapter exception occurred. Load balancer id(s) removed from batch. Skipping batch...", e);
                        for (LoadBalancer ignoredLoadBalancer : loadBalancers) {
                            LOG.error(String.format("LB id in bad batch: '%s'", ignoredLoadBalancer.getId()));
                        }
                    }
                }
            };

            ExecutionUtilities.executeInBatches(loadBalancersForHost, BATCH_SIZE, batchAction);
        } catch (DecryptException de) {
            LOG.error(String.format("Error decrypting configuration for '%s' (%s)", host.getName(), host.getEndpoint()), de);
        } catch (Exception e) {
            LOG.error("Exception caught", e);
            e.printStackTrace();
        }
    }

    private void updateUsageRecords(List<LoadBalancer> loadBalancers, Map<Integer, Long> bytesInMap, Map<Integer, Long> bytesOutMap) {
        Calendar pollTime = Calendar.getInstance();
        Map<Integer, Integer> lbIdAccountIdMap = generateLbIdAccountIdMap(loadBalancers);
        List<UsageRecord> usages = usageRepository.getMostRecentUsageForLoadBalancers(lbIdAccountIdMap.keySet());
        Map<Integer, UsageRecord> usagesAsMap = convertUsagesToMap(usages);
        UsagesFromPoll usagesForDatabase = new UsagesFromPoll(loadBalancers, bytesInMap, bytesOutMap, pollTime, usagesAsMap).invoke();

        if (!usagesForDatabase.getRecordsToInsert().isEmpty())
            usageRepository.batchCreate(usagesForDatabase.getRecordsToInsert());
        if (!usagesForDatabase.getRecordsToUpdate().isEmpty())
            usageRepository.batchUpdate(usagesForDatabase.getRecordsToUpdate());
    }

    private Map<Integer, Integer> generateLbIdAccountIdMap(List<LoadBalancer> loadBalancers) {
        Map<Integer, Integer> lbIdAccountIdMap = new HashMap<Integer, Integer>();
        for (LoadBalancer loadBalancer : loadBalancers) {
            lbIdAccountIdMap.put(loadBalancer.getId(), loadBalancer.getAccountId());
        }
        return lbIdAccountIdMap;
    }

    private Map<Integer, UsageRecord> convertUsagesToMap(List<UsageRecord> usages) {
        Map<Integer, UsageRecord> usagesAsMap = new HashMap<Integer, UsageRecord>();
        for (UsageRecord usage : usages) {
            usagesAsMap.put(usage.getLoadBalancer().getId(), usage);
        }
        return usagesAsMap;
    }
}
