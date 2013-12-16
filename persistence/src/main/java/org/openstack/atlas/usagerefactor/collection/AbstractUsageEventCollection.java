package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.UsageEventCollectionException;
import org.openstack.atlas.service.domain.repository.HostRepository;
import org.openstack.atlas.usagerefactor.SnmpUsage;
import org.openstack.atlas.usagerefactor.processor.UsageEventProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public abstract class AbstractUsageEventCollection {
    private final Log LOG = LogFactory.getLog(AbstractUsageEventCollection.class);
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

    public abstract List<Future<SnmpUsage>> collectUsageRecords(ExecutorService executorService, UsageEventProcessor usageEventProcessor, List<Host> hosts, LoadBalancer lb) throws UsageEventCollectionException;

    public abstract List<SnmpUsage> getUsagesFromFutures(List<Future<SnmpUsage>> futures) throws UsageEventCollectionException;

    /**
     * Used to process usage events
     *
     * @param lb
     * @param event
     * @param eventTime
     * @throws UsageEventCollectionException
     */
    public void collectUsageAndProcessUsageRecords(LoadBalancer lb, UsageEvent event, Calendar eventTime) throws UsageEventCollectionException {
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        List<SnmpUsage> usages = getUsage(lb);
        usageEventProcessor.processUsageEvent(usages, lb, event, eventTime);
    }

    public List<SnmpUsage> getUsage(LoadBalancer lb) throws UsageEventCollectionException {
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        List<Host> hosts = null;
        try {
            hosts = gatherHostsData(lb);
        } catch (EntityNotFoundException e) {
            LOG.error(String.format("On an event, load balancer %d was assigned to host %d and it does not exist.", lb.getId(), lb.getHost().getId()));
        }

        List<SnmpUsage> usages;
        if (hosts != null && !hosts.isEmpty()) {
            executorService = Executors.newFixedThreadPool(hosts.size());
            List<Future<SnmpUsage>> futures = collectUsageRecords(executorService, usageEventProcessor, hosts, lb);
            usages = getUsagesFromFutures(futures);
            LOG.debug("Finished getting snmp usage for: " + lb.getId());
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }

        return usages;
    }

    /**
     * Used to process events that will not have usage, such as Create event with specified hosts.
     *
     * @param lb
     * @param event
     * @param eventTime
     * @throws UsageEventCollectionException
     */
    public void processZeroUsageEvent(LoadBalancer lb, UsageEvent event, Calendar eventTime) throws UsageEventCollectionException {
        List<Host> hosts = null;
        try {
            hosts = gatherHostsData(lb);
        } catch (EntityNotFoundException e) {
            LOG.error(String.format("On %s event, load balancer %d was assigned to host %d and it does not exist.", event.name(), lb.getId(), lb.getHost().getId()));
        }

        if (hosts != null && !hosts.isEmpty()) {
            List<SnmpUsage> snmpUsages = new ArrayList<SnmpUsage>();
            for (Host h : hosts) {
                SnmpUsage snmpUsage = new SnmpUsage();
                snmpUsage.setHostId(h.getId());
                snmpUsage.setLoadbalancerId(lb.getId());
                snmpUsage.setBytesIn(0L);
                snmpUsage.setBytesInSsl(0L);
                snmpUsage.setBytesOutSsl(0L);
                snmpUsage.setBytesOut(0L);
                snmpUsages.add(snmpUsage);
            }
            usageEventProcessor.processUsageEvent(snmpUsages, lb, event, eventTime);
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }
    }

    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer lb, UsageEvent event, Calendar eventTime) {
        usageEventProcessor.processUsageEvent(usages, lb, event, eventTime);
    }

    private List<Host> gatherHostsData(LoadBalancer lb) throws EntityNotFoundException {
        return hostRepository.getOnlineHostsByLoadBalancerHostCluster(lb);
    }

}
