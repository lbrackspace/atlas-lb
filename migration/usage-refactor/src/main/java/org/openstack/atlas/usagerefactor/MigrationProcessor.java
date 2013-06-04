package org.openstack.atlas.usagerefactor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsage;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MigrationProcessor {

    private static LoadBalancingConfig loadbalancingConfig = new LoadBalancingConfig();
    private static Configuration hibernateConfig = new Configuration();

    public MigrationProcessor() {
        loadHibernateConfigs();
        loadEntities();
    }

    private static void loadHibernateConfigs() {
        final Iterator keys = loadbalancingConfig.getKeys();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = loadbalancingConfig.getString(key);
            hibernateConfig.setProperty(key, value);
        }
    }

    private static void loadEntities() {
        hibernateConfig.addAnnotatedClass(Entity.class);
        hibernateConfig.addAnnotatedClass(AccessList.class);
        hibernateConfig.addAnnotatedClass(Account.class);
        hibernateConfig.addAnnotatedClass(AccountGroup.class);
        hibernateConfig.addAnnotatedClass(AccountLimit.class);
        hibernateConfig.addAnnotatedClass(AccountUsage.class);
        hibernateConfig.addAnnotatedClass(AllowedDomain.class);
        hibernateConfig.addAnnotatedClass(Backup.class);
        hibernateConfig.addAnnotatedClass(BlacklistItem.class);
        hibernateConfig.addAnnotatedClass(Cluster.class);
        hibernateConfig.addAnnotatedClass(ConnectionLimit.class);
        hibernateConfig.addAnnotatedClass(Defaults.class);
        hibernateConfig.addAnnotatedClass(GroupRateLimit.class);
        hibernateConfig.addAnnotatedClass(HealthMonitor.class);
        hibernateConfig.addAnnotatedClass(Host.class);
        hibernateConfig.addAnnotatedClass(JobState.class);
        hibernateConfig.addAnnotatedClass(LimitType.class);
        hibernateConfig.addAnnotatedClass(LoadBalancer.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerAlgorithmObject.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerId.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerJoinVip.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerJoinVip6.class);
        hibernateConfig.addAnnotatedClass(LoadbalancerMeta.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerProtocolObject.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerStatusHistory.class);
        hibernateConfig.addAnnotatedClass(Node.class);
        hibernateConfig.addAnnotatedClass(NodeMeta.class);
        hibernateConfig.addAnnotatedClass(RateLimit.class);
        hibernateConfig.addAnnotatedClass(SessionPersistenceObject.class);
        hibernateConfig.addAnnotatedClass(SslTermination.class);
        hibernateConfig.addAnnotatedClass(Suspension.class);
        hibernateConfig.addAnnotatedClass(Ticket.class);
        hibernateConfig.addAnnotatedClass(TrafficScripts.class);
        hibernateConfig.addAnnotatedClass(Usage.class);
        hibernateConfig.addAnnotatedClass(UserPages.class);
//        hibernateConfig.addAnnotatedClass(Version.class);
        hibernateConfig.addAnnotatedClass(VirtualIp.class);
        hibernateConfig.addAnnotatedClass(VirtualIpv6.class);

        hibernateConfig.addAnnotatedClass(AccessListType.class);
        hibernateConfig.addAnnotatedClass(AccountLimitType.class);
        hibernateConfig.addAnnotatedClass(BlacklistType.class);
        hibernateConfig.addAnnotatedClass(ClusterStatus.class);
        hibernateConfig.addAnnotatedClass(DataCenter.class);
        hibernateConfig.addAnnotatedClass(HealthMonitorType.class);
        hibernateConfig.addAnnotatedClass(HostStatus.class);
        hibernateConfig.addAnnotatedClass(IpVersion.class);
        hibernateConfig.addAnnotatedClass(JobName.class);
        hibernateConfig.addAnnotatedClass(JobStateVal.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerAlgorithm.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerProtocol.class);
        hibernateConfig.addAnnotatedClass(LoadBalancerStatus.class);
        hibernateConfig.addAnnotatedClass(NodeCondition.class);
        hibernateConfig.addAnnotatedClass(NodeStatus.class);
        hibernateConfig.addAnnotatedClass(NodeType.class);
        hibernateConfig.addAnnotatedClass(SessionPersistence.class);
        hibernateConfig.addAnnotatedClass(VirtualIpType.class);
        hibernateConfig.addAnnotatedClass(Zone.class);
    }

    public List<LoadBalancerMergedHostUsage> process(List<LoadBalancerUsage> loadBalancerUsages, List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerHostUsage> loadBalancerHostUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        loadBalancerUsageEvents = removeDuplicateEvents(loadBalancerUsageEvents, loadBalancerHostUsages);

        List<LoadBalancerMergedHostUsage> lbUsageRecords = convertLoadBalancerUsageRecords(loadBalancerUsages);
        loadBalancerMergedHostUsages.addAll(lbUsageRecords);

        List<LoadBalancerMergedHostUsage> eventRecords = convertLoadBalancerUsageEvents(loadBalancerUsageEvents, loadBalancerUsages);
        loadBalancerMergedHostUsages.addAll(eventRecords);

        return loadBalancerMergedHostUsages;
    }

    private List<LoadBalancerMergedHostUsage> convertLoadBalancerUsageRecords(List<LoadBalancerUsage> loadBalancerUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        for (LoadBalancerUsage loadBalancerUsage : loadBalancerUsages) {
            loadBalancerMergedHostUsages.addAll(convertLoadBalancerUsage(loadBalancerUsage));
        }

        return loadBalancerMergedHostUsages;
    }

    // TODO: Test
    protected List<LoadBalancerMergedHostUsage> convertLoadBalancerUsage(LoadBalancerUsage loadBalancerUsage) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        String eventType = loadBalancerUsage.getEventType();

        if (eventType != null) {
            LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();

            loadBalancerMergedHostUsage.setAccountId(loadBalancerUsage.getAccountId());
            loadBalancerMergedHostUsage.setLoadbalancerId(loadBalancerUsage.getLoadbalancerId());
            loadBalancerMergedHostUsage.setNumVips(loadBalancerUsage.getNumVips());
            loadBalancerMergedHostUsage.setPollTime(loadBalancerUsage.getStartTime());
            loadBalancerMergedHostUsage.setTagsBitmask(loadBalancerUsage.getTags());
            loadBalancerMergedHostUsage.setEventType(UsageEvent.valueOf(eventType));
            loadBalancerMergedHostUsage.setIncomingTransfer(0);
            loadBalancerMergedHostUsage.setIncomingTransferSsl(0);
            loadBalancerMergedHostUsage.setOutgoingTransfer(0);
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(0);
            loadBalancerMergedHostUsage.setConcurrentConnections(0);
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(0);

            loadBalancerMergedHostUsages.add(loadBalancerMergedHostUsage);
        }

        if (loadBalancerUsage.getEndTime().after(loadBalancerUsage.getStartTime())) {
            Calendar endTime = loadBalancerUsage.getEndTime();
            endTime.add(Calendar.SECOND, -1);

            LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();
            loadBalancerMergedHostUsage.setAccountId(loadBalancerUsage.getAccountId());
            loadBalancerMergedHostUsage.setLoadbalancerId(loadBalancerUsage.getLoadbalancerId());
            loadBalancerMergedHostUsage.setNumVips(loadBalancerUsage.getNumVips());
            loadBalancerMergedHostUsage.setPollTime(endTime);
            loadBalancerMergedHostUsage.setTagsBitmask(loadBalancerUsage.getTags());
            loadBalancerMergedHostUsage.setEventType(null);
            loadBalancerMergedHostUsage.setIncomingTransfer(loadBalancerUsage.getCumulativeBandwidthBytesIn());
            loadBalancerMergedHostUsage.setIncomingTransferSsl(loadBalancerUsage.getCumulativeBandwidthBytesInSsl());
            loadBalancerMergedHostUsage.setOutgoingTransfer(loadBalancerUsage.getCumulativeBandwidthBytesOut());
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(loadBalancerUsage.getCumulativeBandwidthBytesOutSsl());
            loadBalancerMergedHostUsage.setConcurrentConnections(loadBalancerUsage.getAverageConcurrentConnections().longValue());
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(loadBalancerUsage.getAverageConcurrentConnectionsSsl().longValue());

            loadBalancerMergedHostUsages.add(loadBalancerMergedHostUsage);
        }

        return loadBalancerMergedHostUsages;
    }

    private List<LoadBalancerMergedHostUsage> convertLoadBalancerUsageEvents(List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerUsage> loadBalancerUsages) {
        List<LoadBalancerMergedHostUsage> loadBalancerMergedHostUsages = new ArrayList<LoadBalancerMergedHostUsage>();

        for (LoadBalancerUsageEvent loadBalancerUsageEvent : loadBalancerUsageEvents) {
            LoadBalancerUsage mostRecentLbUsage = getMostRecentLbUsage(loadBalancerUsageEvent.getLoadbalancerId(), loadBalancerUsages);
            LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = convertLoadBalancerUsageEvent(loadBalancerUsageEvent, mostRecentLbUsage);
            loadBalancerMergedHostUsages.add(loadBalancerMergedHostUsage);
        }

        return loadBalancerMergedHostUsages;
    }

    /*
     * Assumes usage is in order
     */
    // TODO: Test
    protected LoadBalancerUsage getMostRecentLbUsage(Integer loadbalancerId, List<LoadBalancerUsage> loadBalancerUsages) {
        LoadBalancerUsage mostRecentLbUsage = null;

        for (LoadBalancerUsage loadBalancerUsage : loadBalancerUsages) {
            if (loadBalancerUsage.getLoadbalancerId().equals(loadbalancerId)) {
                mostRecentLbUsage = loadBalancerUsage;
            }
        }

        if (mostRecentLbUsage == null) {
            Usage usage = getMostRecentLbUsageFromMainLbUsageTable(loadbalancerId);
            System.out.println(usage);
        }

        return mostRecentLbUsage;
    }

    private Usage getMostRecentLbUsageFromMainLbUsageTable(Integer loadbalancerId) {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<Usage> loadBalancerUsageList = new ArrayList<Usage>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving items from lb_usage from 'loadbalancing' db..."));
            loadBalancerUsageList = session.createQuery("SELECT u FROM Usage u WHERE u.loadbalancer.id = :loadbalancerId ORDER BY u.startTime DESC")
                    .setParameter("loadbalancerId", loadbalancerId)
                    .list();
            System.out.println(String.format("Number of items retrieved from lb_usage from 'loadbalancing' db: %d", loadBalancerUsageList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        if (loadBalancerUsageList.isEmpty()) {
            return null;
        }

        return loadBalancerUsageList.get(0);
    }

    // TODO: Test
    protected LoadBalancerMergedHostUsage convertLoadBalancerUsageEvent(LoadBalancerUsageEvent loadBalancerUsageEvent, LoadBalancerUsage mostRecentLbUsage) {
        UsageEvent eventType = UsageEvent.valueOf(loadBalancerUsageEvent.getEventType());
        LoadBalancerMergedHostUsage loadBalancerMergedHostUsage = new LoadBalancerMergedHostUsage();

        loadBalancerMergedHostUsage.setAccountId(loadBalancerUsageEvent.getAccountId());
        loadBalancerMergedHostUsage.setLoadbalancerId(loadBalancerUsageEvent.getLoadbalancerId());
        loadBalancerMergedHostUsage.setNumVips(loadBalancerUsageEvent.getNumVips());
        loadBalancerMergedHostUsage.setPollTime(loadBalancerUsageEvent.getStartTime());
        loadBalancerMergedHostUsage.setEventType(eventType);

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && mostRecentLbUsage == null) {
            throw new RuntimeException(String.format("Load balancer '%d' does not have any recent usage!", loadBalancerUsageEvent.getLoadbalancerId()));
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastBandwidthBytesIn() != null && loadBalancerUsageEvent.getLastBandwidthBytesIn() > mostRecentLbUsage.getLastBandwidthBytesIn()) {
            loadBalancerMergedHostUsage.setIncomingTransfer(loadBalancerUsageEvent.getLastBandwidthBytesIn() - mostRecentLbUsage.getLastBandwidthBytesIn());
        } else {
            loadBalancerMergedHostUsage.setIncomingTransfer(0);
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastBandwidthBytesInSsl() != null && loadBalancerUsageEvent.getLastBandwidthBytesInSsl() > mostRecentLbUsage.getLastBandwidthBytesInSsl()) {
            loadBalancerMergedHostUsage.setIncomingTransferSsl(loadBalancerUsageEvent.getLastBandwidthBytesInSsl() - mostRecentLbUsage.getLastBandwidthBytesInSsl());
        } else {
            loadBalancerMergedHostUsage.setIncomingTransferSsl(0);
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastBandwidthBytesOut() != null && loadBalancerUsageEvent.getLastBandwidthBytesOut() > mostRecentLbUsage.getLastBandwidthBytesOut()) {
            loadBalancerMergedHostUsage.setOutgoingTransfer(loadBalancerUsageEvent.getLastBandwidthBytesOut() - mostRecentLbUsage.getLastBandwidthBytesOut());
        } else {
            loadBalancerMergedHostUsage.setOutgoingTransfer(0);
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastBandwidthBytesOutSsl() != null && loadBalancerUsageEvent.getLastBandwidthBytesOutSsl() > mostRecentLbUsage.getLastBandwidthBytesOutSsl()) {
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(loadBalancerUsageEvent.getLastBandwidthBytesOutSsl() - mostRecentLbUsage.getLastBandwidthBytesOutSsl());
        } else {
            loadBalancerMergedHostUsage.setOutgoingTransferSsl(0);
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastConcurrentConnections() != null) {
            loadBalancerMergedHostUsage.setConcurrentConnections(loadBalancerUsageEvent.getLastConcurrentConnections());
        } else {
            loadBalancerMergedHostUsage.setConcurrentConnections(0);
        }

        if (!eventType.equals(UsageEvent.CREATE_LOADBALANCER) && loadBalancerUsageEvent.getLastConcurrentConnectionsSsl() != null) {
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(loadBalancerUsageEvent.getLastConcurrentConnectionsSsl());
        } else {
            loadBalancerMergedHostUsage.setConcurrentConnectionsSsl(0);
        }

        if (eventType.equals(UsageEvent.CREATE_LOADBALANCER)) {
            // TODO: Figure out how to determine if servicenet or not.
        } else {
            BitTags bitTags = new BitTags(mostRecentLbUsage.getTags());
            bitTags.applyEvent(eventType);
            loadBalancerMergedHostUsage.setTagsBitmask(bitTags.toInt());
        }

        return loadBalancerMergedHostUsage;
    }

    // TODO: Test
    protected List<LoadBalancerUsageEvent> removeDuplicateEvents(List<LoadBalancerUsageEvent> loadBalancerUsageEvents, List<LoadBalancerHostUsage> loadBalancerHostUsages) {
        for (LoadBalancerHostUsage loadBalancerHostUsage : loadBalancerHostUsages) {
            for (LoadBalancerUsageEvent loadBalancerUsageEvent : loadBalancerUsageEvents) {
                if (loadBalancerHostUsage.getLoadbalancerId() == loadBalancerUsageEvent.getAccountId()
                        && loadBalancerHostUsage.getPollTime().equals(loadBalancerUsageEvent.getStartTime())
                        && loadBalancerHostUsage.getEventType().name().equals(loadBalancerUsageEvent.getEventType())) {
                    System.out.println(String.format("Removing duplicate usage event for loadbalancer %d...", loadBalancerUsageEvent.getLoadbalancerId()));
                    loadBalancerUsageEvents.remove(loadBalancerUsageEvent);
                    break;
                }
            }
        }

        return loadBalancerUsageEvents;
    }
}