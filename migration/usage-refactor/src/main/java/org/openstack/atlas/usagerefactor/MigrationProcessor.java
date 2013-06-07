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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

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



        UsageEventProcessor usageEventProcessor = new UsageEventProcessor(loadBalancerUsageEvents, null, null, null);
        usageEventProcessor.process();

        List<LoadBalancerUsage> usagesToUpdate = usageEventProcessor.getUsagesToUpdate();

        for (LoadBalancerUsage updatedRecord : usagesToUpdate) {
            for (LoadBalancerUsage recordToUpdate : loadBalancerUsages) {
                if (recordToUpdate.getId().equals(updatedRecord.getId())) {
                    System.out.println(String.format("Updating lb_usage record '%d for lb '%d'...", recordToUpdate.getId(), recordToUpdate.getLoadbalancerId()));
                    updateRecord(recordToUpdate, updatedRecord);
                    System.out.println(String.format("Updated lb_usage record '%d for lb '%d'.", recordToUpdate.getId(), recordToUpdate.getLoadbalancerId()));
                    break;
                }
            }
        }

        List<LoadBalancerUsage> usagesToCreate = usageEventProcessor.getUsagesToCreate();
        loadBalancerUsages.addAll(usagesToCreate);

        List<LoadBalancerMergedHostUsage> lbUsageRecords = convertLoadBalancerUsageRecords(loadBalancerUsages);
        loadBalancerMergedHostUsages.addAll(lbUsageRecords);

        return loadBalancerMergedHostUsages;
    }

    // Contains side effects FYI
    private void updateRecord(LoadBalancerUsage recordToUpdate, LoadBalancerUsage updatedRecord) {
        recordToUpdate.setCumulativeBandwidthBytesIn(updatedRecord.getCumulativeBandwidthBytesIn());
        recordToUpdate.setCumulativeBandwidthBytesInSsl(updatedRecord.getCumulativeBandwidthBytesInSsl());
        recordToUpdate.setCumulativeBandwidthBytesOut(updatedRecord.getCumulativeBandwidthBytesOut());
        recordToUpdate.setCumulativeBandwidthBytesOutSsl(updatedRecord.getCumulativeBandwidthBytesOutSsl());
        recordToUpdate.setAverageConcurrentConnections(updatedRecord.getAverageConcurrentConnections());
        recordToUpdate.setAverageConcurrentConnectionsSsl(updatedRecord.getAverageConcurrentConnectionsSsl());
        recordToUpdate.setLastBandwidthBytesIn(updatedRecord.getLastBandwidthBytesIn());
        recordToUpdate.setLastBandwidthBytesInSsl(updatedRecord.getLastBandwidthBytesInSsl());
        recordToUpdate.setLastBandwidthBytesOut(updatedRecord.getLastBandwidthBytesOut());
        recordToUpdate.setLastBandwidthBytesOutSsl(updatedRecord.getLastBandwidthBytesOutSsl());
        recordToUpdate.setStartTime(updatedRecord.getStartTime());
        recordToUpdate.setEndTime(updatedRecord.getEndTime());
        recordToUpdate.setEventType(updatedRecord.getEventType());
        recordToUpdate.setNumberOfPolls(updatedRecord.getNumberOfPolls());
        recordToUpdate.setTags(updatedRecord.getTags());
        recordToUpdate.setNumVips(updatedRecord.getNumVips());
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

    private Usage getMostRecentLbUsageFromMainLbUsageTable(Integer loadbalancerId) {
        final SessionFactory sessionFactory = hibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<Object[]> resultList = new ArrayList<Object[]>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving most recent lb_usage from 'loadbalancing' db for loadbalancer '%d'...", loadbalancerId));
            resultList = session.createSQLQuery("SELECT u.id, u.loadbalancer_id, u.avg_concurrent_conns, u.bandwidth_in, u.bandwidth_out, u.avg_concurrent_conns_ssl, u.bandwidth_in_ssl, u.bandwidth_out_ssl, u.start_time, u.end_time, u.num_polls, u.num_vips, u.tags_bitmask, u.event_type, u.account_id FROM lb_usage u WHERE u.loadbalancer_id = :loadbalancerId ORDER BY u.start_time DESC LIMIT 1")
                    .setParameter("loadbalancerId", loadbalancerId)
                    .list();
            System.out.println(String.format("Number of items retrieved from lb_usage from 'loadbalancing' db for loadbalancer '%d': %d", loadbalancerId, resultList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        if (resultList.isEmpty()) {
            return null;
        }

        Object[] row = resultList.get(0);
        return rowToUsage(row);
    }

    private Usage rowToUsage(Object[] row) {
        Long startTimeMillis = ((Timestamp) row[8]).getTime();
        Long endTimeMillis = ((Timestamp) row[9]).getTime();
        Calendar startTimeCal = new GregorianCalendar();
        Calendar endTimeCal = new GregorianCalendar();
        startTimeCal.setTimeInMillis(startTimeMillis);
        endTimeCal.setTimeInMillis(endTimeMillis);

        Usage usageItem = new Usage();
        usageItem.setId((Integer) row[0]);
        LoadBalancer lb = new LoadBalancer();
        lb.setId((Integer) row[1]);
        usageItem.setLoadbalancer(lb);
        usageItem.setAverageConcurrentConnections((Double) row[2]);
        usageItem.setIncomingTransfer(((BigInteger) row[3]).longValue());
        usageItem.setOutgoingTransfer(((BigInteger) row[4]).longValue());
        usageItem.setAverageConcurrentConnectionsSsl((Double) row[5]);
        usageItem.setIncomingTransferSsl(((BigInteger) row[6]).longValue());
        usageItem.setOutgoingTransferSsl(((BigInteger) row[7]).longValue());
        usageItem.setStartTime(startTimeCal);
        usageItem.setEndTime(endTimeCal);
        usageItem.setNumberOfPolls((Integer) row[10]);
        usageItem.setNumVips((Integer) row[11]);
        usageItem.setTags((Integer) row[12]);
        usageItem.setEventType((String) row[13]);
        usageItem.setAccountId((Integer) row[14]);
        return usageItem;
    }

}