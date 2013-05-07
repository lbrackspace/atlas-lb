package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public abstract class AbstractUsageEventCollection {
    private final Log LOG = LogFactory.getLog(AbstractUsageEventCollection.class);
    private List<Usage> usages;
    private List<Host> hosts;
    private ExecutorService executorService;
    private UsageEventProcessor usageEventProcessor;
    private HostRepository hostRepository;

    public AbstractUsageEventCollection() throws UsageEventCollectionException {
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Required
    public void setUsageEventProcessor(UsageEventProcessor usageEventProcessor) {
        this.usageEventProcessor = usageEventProcessor;
    }

    public abstract List<Future<SnmpUsage>> collectUsageRecords(ExecutorService executorService, UsageEventProcessor usageEventProcessor, List<Host> hosts, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException;

    public abstract void processFutures(List<Future<SnmpUsage>> futures, UsageEventProcessor usageEventProcessor, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException;

    public void processUsageRecord(List<Host> hosts, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        this.hosts = null;
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        gatherHostsData(hosts);

        if (this.hosts != null && !this.hosts.isEmpty()) {
            executorService = Executors.newFixedThreadPool(this.hosts.size());
            collectUsageRecords(executorService, usageEventProcessor, this.hosts, lb, event);
            processFutures(null, usageEventProcessor, lb, event);
            LOG.debug("Finished Processing Usage Event Record for load balancer: " + lb.getId());
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }

    }

    public void processSnmpUsage(SnmpUsage snmpUsage, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        processSnmpUsage(null, snmpUsage, lb, event);
    }

    public void processSnmpUsage(List<Host> hosts, SnmpUsage snmpUsage, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        gatherHostsData(hosts);

        List<SnmpUsage> snmpUsages = new ArrayList<SnmpUsage>();
        if (this.hosts != null && !this.hosts.isEmpty()) {
            for (Host h : this.hosts) {
                snmpUsage = new SnmpUsage();
                snmpUsage.setHostId(h.getId());
                snmpUsages.add(snmpUsage);
                usageEventProcessor.processUsageEvent(snmpUsages, lb, event);
            }
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }
    }

    private void gatherHostsData(List<Host> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            this.hosts = hostRepository.getAllHosts();
        } else {
            this.hosts = hosts;
        }
    }

    public void processUsageRecord(LoadBalancer lb) throws UsageEventCollectionException {
        processUsageRecord(null, lb, null);

    }

    public void processUsageRecord(LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        processUsageRecord(null, lb, event);

    }

    public void processUsageRecord(List<Host> hosts) throws UsageEventCollectionException {
        processUsageRecord(hosts, null, null);
    }


    public void processUsageRecord() {
        System.out.print("TEST PPROCESS");
    }

    public List<Host> getHosts() {
        return this.hosts;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

}
