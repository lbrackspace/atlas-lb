package org.openstack.atlas.usagerefactor.collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.UsageEvent;
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

    public abstract List<Future<SnmpUsage>> collectUsageRecords(ExecutorService executorService, UsageEventProcessor usageEventProcessor, List<Host> hosts, LoadBalancer lb) throws UsageEventCollectionException;

    public abstract void processFutures(List<Future<SnmpUsage>> futures, UsageEventProcessor usageEventProcessor, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException;

    public abstract List<SnmpUsage> getUsagesFromFutures(List<Future<SnmpUsage>> futures) throws UsageEventCollectionException;

    /**
     * Used to process usage events
     *
     * @param hosts
     * @param lb
     * @param event
     * @throws UsageEventCollectionException
     */
    public void processUsageRecord(List<Host> hosts, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        this.hosts = null;
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        gatherHostsData(hosts);

        if (this.hosts != null && !this.hosts.isEmpty()) {
            executorService = Executors.newFixedThreadPool(this.hosts.size());
            collectUsageRecords(executorService, usageEventProcessor, this.hosts, lb);
            processFutures(null, usageEventProcessor, lb, event);
            LOG.debug("Finished Processing Usage Event Record for load balancer: " + lb.getId());
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }

    }

    public List<SnmpUsage> getUsageRecords(List<Host> hosts, LoadBalancer lb) throws UsageEventCollectionException {
        this.hosts = null;
        LOG.debug("Processing Usage Records for load balancer: " + lb.getId());
        gatherHostsData(hosts);

        List<SnmpUsage> usages = null;
        if (this.hosts != null && !this.hosts.isEmpty()) {
            executorService = Executors.newFixedThreadPool(this.hosts.size());
            collectUsageRecords(executorService, usageEventProcessor, this.hosts, lb);
            usages = getUsagesFromFutures(null);
            LOG.debug("Finished getting snmp usage for: " + lb.getId());
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }

        return usages;

    }

    /**
     * Used to process usage event without specified hosts
     *
     * @param snmpUsage
     * @param lb
     * @param event
     * @throws UsageEventCollectionException
     */
    public void processSnmpUsage(SnmpUsage snmpUsage, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        processSnmpUsage(null, snmpUsage, lb, event);
    }

    /**
     * Used to process events that will not have usage, such as Create event with specified hosts.
     *
     * @param hosts
     * @param snmpUsage
     * @param lb
     * @param event
     * @throws UsageEventCollectionException
     */
    public void processSnmpUsage(List<Host> hosts, SnmpUsage snmpUsage, LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        gatherHostsData(hosts);

        Calendar pollTime = Calendar.getInstance();
        if (this.hosts != null && !this.hosts.isEmpty()) {
            for (Host h : this.hosts) {
                List<SnmpUsage> snmpUsages = new ArrayList<SnmpUsage>();
                snmpUsage = new SnmpUsage();
                snmpUsage.setHostId(h.getId());
                snmpUsages.add(snmpUsage);
                usageEventProcessor.processUsageEvent(snmpUsages, lb, event, pollTime);
            }
        } else {
            LOG.error("Hosts data invalid, this shouldn't happen... Verify DB for data and notify developer immediately. ");
            throw new UsageEventCollectionException("Hosts data invalid, please contact support.");
        }
    }

    public void processUsageEvent(List<SnmpUsage> usages, LoadBalancer lb, UsageEvent event) {
        usageEventProcessor.processUsageEvent(usages, lb, event, null);
    }

    /**
     * Used to gather hosts data.
     *
     * @param hosts
     */
    private void gatherHostsData(List<Host> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            this.hosts = hostRepository.getAll();
        } else {
            this.hosts = hosts;
        }
    }

    /**
     * Used to process usage events with only load balancer specified.
     *
     * @param lb
     * @throws UsageEventCollectionException
     */
    public void processUsageRecord(LoadBalancer lb) throws UsageEventCollectionException {
        processUsageRecord(null, lb, null);

    }

    /**
     * Used to process usage event without hosts specified.
     *
     * @param lb
     * @param event
     * @throws UsageEventCollectionException
     */
    public void processUsageRecord(LoadBalancer lb, UsageEvent event) throws UsageEventCollectionException {
        processUsageRecord(null, lb, event);

    }

    /**
     * Used to process usage event with only hosts data specified.
     *
     * @param hosts
     * @throws UsageEventCollectionException
     */
    public void processUsageRecord(List<Host> hosts) throws UsageEventCollectionException {
        processUsageRecord(hosts, null, null);
    }


    /**
     * Used for test purposes.
     *
     */
    public void processUsageRecord() {
        System.out.print("TEST PPROCESS");
    }

    /**
     * @return hosts
     *
     */
    public List<Host> getHosts() {
        return this.hosts;
    }

    /**
     * @return executor service
     *
     */
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

}
