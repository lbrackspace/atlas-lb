package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public abstract class AbstractUsageEventCollection {
    private final Log LOG = LogFactory.getLog(AbstractUsageEventCollection.class);
    private List<Usage> usages;
    private List<Host> hosts;
    private ExecutorService executorService;
    private UsageEventProcessor usageEventProcessor;
    private HostRepository hostRepository;

    public AbstractUsageEventCollection() {
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    @Required
    public void setUsageEventProcessor(UsageEventProcessor usageEventProcessor) {
        this.usageEventProcessor = usageEventProcessor;
    }

    public abstract void collectUsageRecords(ExecutorService executorService, UsageEventProcessor usageEventProcessor, List<Host> hosts, LoadBalancer lb, UsageEvent event);

    public void processUsageRecord(List<Host> hosts, LoadBalancer lb, UsageEvent event) {
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        if (hosts == null || hosts.isEmpty()) {
            this.hosts = hostRepository.getAllHosts();
        } else {
            this.hosts = hosts;
        }
        executorService = Executors.newFixedThreadPool(hosts.size());
        collectUsageRecords(executorService, usageEventProcessor, hosts, lb, event);
        LOG.debug("Finished Processing Usage Records for load balancer: " + lb.getId());
    }

    public void processUsageRecord(LoadBalancer lb) {
        processUsageRecord(null, lb, null);

    }

    public void processUsageRecord(LoadBalancer lb, UsageEvent event) {
        processUsageRecord(null, lb, event);

    }

    public void processUsageRecord(List<Host> hosts) {
        processUsageRecord(hosts, null, null);
    }

    public void processUsageRecord() {
        LOG.info("Test Request");
//        processUsageRecord(null, null, null);
    }
}
