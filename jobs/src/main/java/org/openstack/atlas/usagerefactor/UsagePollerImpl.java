package org.openstack.atlas.usagerefactor;

import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsagePollerImpl implements UsagePoller {

    final Log LOG = LogFactory.getLog(UsagePollerImpl.class);

    HostRepository hostRepository = new HostRepository();
    StingrayUsageClientImpl stingrayUsageClient = new StingrayUsageClientImpl();

    @Override
    public void processRecords() {

    }

    @Override
    public List<LoadBalancerHostUsage> getLoadBalancerHostUsageRecords() {
        List<LoadBalancerHostUsage> lbHostUsages = new ArrayList<LoadBalancerHostUsage>();
        return lbHostUsages;
    }

    @Override
    public Map<Integer, SnmpUsage> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, SnmpUsage> mergedHostsUsage = new HashMap<Integer, SnmpUsage>();
        List<Host> hostList = hostRepository.getAll();
        ExecutorService threadPool = Executors.newFixedThreadPool(hostList.size());
        for (final Host host : hostList) {
            threadPool.submit(new Runnable() {
                public void run() {
                    try {
                        stingrayUsageClient.getHostUsage(host);
                    } catch (Exception e) {
                        String retString = String.format("Request for host %s usage from SNMP server failed.", host.getName());
                        LOG.error(retString, e);
                    }
                }
            });
        }
        return mergedHostsUsage;
    }

    @Override
    public void deleteLoadBalancerHostUsageRecords(int markerId) {

    }

    @Override
    public void insertLoadBalancerUsagePerHost(List<LoadBalancerHostUsage> lbHostUsages) {

    }

    @Override
    public void insertMergedRecords(List<LoadBalancerMergedHostUsage> mergedRecords) {

    }
}
