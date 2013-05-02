package org.openstack.atlas.usagerefactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.services.HostService;
import org.openstack.atlas.usagerefactor.helpers.HostIdUsageMap;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SnmpUsageCollectorImpl implements SnmpUsageCollector {
    final Log LOG = LogFactory.getLog(UsageProcessor.class);

    HostService hostService;

    @Required
    public void setHostService(HostService hostService) {
        this.hostService = hostService;
    }

    @Override
    public Map<Integer, Map<Integer, SnmpUsage>> getCurrentData() throws Exception {
        LOG.info("Collecting Stingray data from each host...");
        Map<Integer, Map<Integer, SnmpUsage>> mergedHostsUsage = new HashMap<Integer, Map<Integer, SnmpUsage>>();
        List<Host> hostList = hostService.getAllHosts();
        List<Callable<HostIdUsageMap>> callables = new ArrayList<Callable<HostIdUsageMap>>();

        ExecutorService executor = Executors.newFixedThreadPool(hostList.size());
        for (Host host : hostList) {
            callables.add(new HostThread(host));
        }

        List<Future<HostIdUsageMap>> futures = executor.invokeAll(callables);
        for (Future<HostIdUsageMap> future : futures) {
            mergedHostsUsage.put(future.get().getHostId(), future.get().getMap());
        }

        return mergedHostsUsage;
    }
}
