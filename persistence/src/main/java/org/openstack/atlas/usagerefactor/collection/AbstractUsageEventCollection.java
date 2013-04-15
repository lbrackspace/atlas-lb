package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractUsageEventCollection {
    private final Log LOG = LogFactory.getLog(AbstractUsageEventCollection.class);
    private List<Usage> usages;
    private List<Host> hosts;
    ExecutorService executorService;
    private HostRepository hostRepository;

    public AbstractUsageEventCollection() {
    }

    @Required
    public void setHostRepository(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    public abstract void collectUsageRecords(List<Host> hosts, LoadBalancer lb, UsageEvent event);

    public AbstractUsageEventCollection(List<Usage> usages) {
        this.usages = usages;
    }

    public void processUsageRecord(List<Host> hosts, LoadBalancer lb, UsageEvent event) {
        //LOGGING AND STUFF HERE
        if (!hosts.isEmpty()) {
            this.hosts = hostRepository.getAllHosts();
        } else {
            this.hosts = hosts;
        }
        this.executorService = Executors.newFixedThreadPool(hosts.size());
        collectUsageRecords(hosts, lb, event);
        //LOGGING AND STUFF HERE
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
        processUsageRecord(null, null, null);
    }
}
