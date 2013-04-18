package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class UsageEventCollection extends AbstractUsageEventCollection {
    private final Log LOG = LogFactory.getLog(UsageEventCollection.class);
    private List<Future<SnmpUsage>> futures;

    public UsageEventCollection() {
    }

    @Override
    public void collectUsageRecords(ExecutorService executorService, UsageEventProcessor usageEventProcessor,
                                    List<Host> hosts, LoadBalancer lb, UsageEvent event) {

        LOG.debug("Collecting SNMP Usages for load balancer: " + lb.getId());

        List<Callable<SnmpUsage>> callables = new ArrayList<Callable<SnmpUsage>>();
        for (Host h : hosts) {
            callables.add(new SnmpVSCollector(h, lb));
        }
        List<Future<SnmpUsage>> futures = null;
        try {
            LOG.debug("Executing SNMP collection tasks for loadbalancer: " + lb.getId());
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            LOG.error("Im an error during invokeAll, handle me: " + e);

        }
        this.futures = futures;
        processFutures(usageEventProcessor, lb, event);
    }

    private void processFutures(UsageEventProcessor usageEventProcessor, LoadBalancer lb, UsageEvent event) {
        List<SnmpUsage> usages = new ArrayList<SnmpUsage>();
        for (Future<SnmpUsage> f : futures) {
            try {
                usages.add(f.get());
            } catch (InterruptedException e) {
                LOG.error("Im an error during futures.get, handle me: " + e);
            } catch (ExecutionException e) {
                LOG.error("Im an error during invokeAll, handle me: " + e);
            }
        }
        usageEventProcessor.processUsageEvent(usages, lb, event);
    }

    public List<Future<SnmpUsage>> getFutures() {
        return this.futures;
    }
}
