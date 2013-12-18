package org.openstack.atlas.usagerefactor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.entities.Entity;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.BitTag;
import org.openstack.atlas.service.domain.usage.BitTags;
import org.openstack.atlas.service.domain.usage.entities.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public class UsageEventProcessor {
    private static LoadBalancingConfig loadbalancingConfig = new LoadBalancingConfig();
    private static Configuration loadbalancingHibernateConfig = new Configuration();
    private static LoadbalancingUsageConfig loadbalancingUsageConfig = new LoadbalancingUsageConfig();
    private static Configuration loadbalancingUsageHibernateConfig = new Configuration();

    private List<LoadBalancerUsage> usagesToCreate;
    private List<LoadBalancerUsage> usagesToUpdate;
    private List<LoadBalancerUsageEvent> inOrderUsageEventEntries;

    public UsageEventProcessor(List<LoadBalancerUsageEvent> inOrderUsageEventEntries) {
        loadLoadbalancingHibernateConfigs();
        loadLoadbalancingEntities();
        loadLoadbalancingUsageHibernateConfigs();
        loadLoadbalancingUsageEntities();

        this.inOrderUsageEventEntries = inOrderUsageEventEntries;
        this.usagesToCreate = new ArrayList<LoadBalancerUsage>();
        this.usagesToUpdate = new ArrayList<LoadBalancerUsage>();
    }

    public List<LoadBalancerUsage> getUsagesToCreate() {
        return this.usagesToCreate;
    }

    public List<LoadBalancerUsage> getUsagesToUpdate() {
        return this.usagesToUpdate;
    }

    private static void loadLoadbalancingHibernateConfigs() {
        final Iterator keys = loadbalancingConfig.getKeys();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = loadbalancingConfig.getString(key);
            loadbalancingHibernateConfig.setProperty(key, value);
        }
    }

    private static void loadLoadbalancingEntities() {
        loadbalancingHibernateConfig.addAnnotatedClass(Entity.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AccessList.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Account.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AccountGroup.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AccountLimit.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AccountUsage.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AllowedDomain.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Backup.class);
        loadbalancingHibernateConfig.addAnnotatedClass(BlacklistItem.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Cluster.class);
        loadbalancingHibernateConfig.addAnnotatedClass(ConnectionLimit.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Defaults.class);
        loadbalancingHibernateConfig.addAnnotatedClass(GroupRateLimit.class);
        loadbalancingHibernateConfig.addAnnotatedClass(HealthMonitor.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Host.class);
        loadbalancingHibernateConfig.addAnnotatedClass(JobState.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LimitType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancer.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerAlgorithmObject.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerId.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerJoinVip.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerJoinVip6.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadbalancerMeta.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerProtocolObject.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerStatusHistory.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Node.class);
        loadbalancingHibernateConfig.addAnnotatedClass(NodeMeta.class);
        loadbalancingHibernateConfig.addAnnotatedClass(RateLimit.class);
        loadbalancingHibernateConfig.addAnnotatedClass(SessionPersistenceObject.class);
        loadbalancingHibernateConfig.addAnnotatedClass(SslTermination.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Suspension.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Ticket.class);
        loadbalancingHibernateConfig.addAnnotatedClass(TrafficScripts.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Usage.class);
        loadbalancingHibernateConfig.addAnnotatedClass(UserPages.class);
//        loadbalancingHibernateConfig.addAnnotatedClass(Version.class);
        loadbalancingHibernateConfig.addAnnotatedClass(VirtualIp.class);
        loadbalancingHibernateConfig.addAnnotatedClass(VirtualIpv6.class);

        loadbalancingHibernateConfig.addAnnotatedClass(AccessListType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(AccountLimitType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(BlacklistType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(ClusterStatus.class);
        loadbalancingHibernateConfig.addAnnotatedClass(DataCenter.class);
        loadbalancingHibernateConfig.addAnnotatedClass(HealthMonitorType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(HostStatus.class);
        loadbalancingHibernateConfig.addAnnotatedClass(IpVersion.class);
        loadbalancingHibernateConfig.addAnnotatedClass(JobName.class);
        loadbalancingHibernateConfig.addAnnotatedClass(JobStateVal.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerAlgorithm.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerProtocol.class);
        loadbalancingHibernateConfig.addAnnotatedClass(LoadBalancerStatus.class);
        loadbalancingHibernateConfig.addAnnotatedClass(NodeCondition.class);
        loadbalancingHibernateConfig.addAnnotatedClass(NodeStatus.class);
        loadbalancingHibernateConfig.addAnnotatedClass(NodeType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(SessionPersistence.class);
        loadbalancingHibernateConfig.addAnnotatedClass(VirtualIpType.class);
        loadbalancingHibernateConfig.addAnnotatedClass(Zone.class);
    }

    private static void loadLoadbalancingUsageHibernateConfigs() {
        final Iterator keys = loadbalancingUsageConfig.getKeys();

        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = loadbalancingUsageConfig.getString(key);
            loadbalancingUsageHibernateConfig.setProperty(key, value);
        }
    }

    private static void loadLoadbalancingUsageEntities() {
        loadbalancingUsageHibernateConfig.addAnnotatedClass(org.openstack.atlas.service.domain.usage.entities.Entity.class);
        loadbalancingUsageHibernateConfig.addAnnotatedClass(HostUsage.class);
        loadbalancingUsageHibernateConfig.addAnnotatedClass(LoadBalancerHostUsage.class);
        loadbalancingUsageHibernateConfig.addAnnotatedClass(LoadBalancerMergedHostUsage.class);
        loadbalancingUsageHibernateConfig.addAnnotatedClass(LoadBalancerUsage.class);
        loadbalancingUsageHibernateConfig.addAnnotatedClass(LoadBalancerUsageEvent.class);
    }

    public UsageEventProcessor process() {
        System.out.println(String.format("Processing %d usage events...", inOrderUsageEventEntries.size()));

        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = createEventUsageMap();
        Map<Integer, LoadBalancerUsage> recentUsageMap = createRecentUsageMap(newEventUsageMap.keySet());

        updateTimestampsAndTagsForUsages(newEventUsageMap, recentUsageMap);

        System.out.println(String.format("%d usage events processed.", inOrderUsageEventEntries.size()));
        return this;
    }

    private void updateTimestampsAndTagsForUsages(Map<Integer, List<LoadBalancerUsage>> newEventUsageMap, Map<Integer, LoadBalancerUsage> recentUsageMap) {
        for (Integer lbId : newEventUsageMap.keySet()) {
            List<LoadBalancerUsage> loadBalancerUsages = newEventUsageMap.get(lbId);

            if (recentUsageMap.containsKey(lbId)) {
                final LoadBalancerUsage recentUsage = recentUsageMap.get(lbId);
                final LoadBalancerUsage firstNewUsage = loadBalancerUsages.get(0);

                // Update recent usage end time
                Calendar newEndTimeForRecentUsage = calculateEndTime(recentUsage.getEndTime(), firstNewUsage.getStartTime());
                if (!recentUsage.getEndTime().equals(newEndTimeForRecentUsage)) {
                    recentUsage.setEndTime(newEndTimeForRecentUsage);
                    usagesToUpdate.add(recentUsage);
                }

                // New records may be needed if we are near the hour mark or if poller goes down.
                List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(recentUsage, firstNewUsage);
                mutateCumulativeFields(recentUsage, bufferRecords, firstNewUsage);
                if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);

                // Update the tags to the proper tags.
                UsageEvent usageEvent = UsageEvent.valueOf(firstNewUsage.getEventType());
                int updatedTags = calculateTags(recentUsage.getAccountId(), lbId, usageEvent, recentUsage);
                firstNewUsage.setTags(updatedTags);
            } else {
                final Usage recentUsage = getMostRecentLbUsageFromMainLbUsageTable(lbId);
                if (recentUsage != null) {
                    final LoadBalancerUsage firstNewUsage = loadBalancerUsages.get(0);

                    // Update the tags to the proper tags.
                    UsageEvent usageEvent = UsageEvent.valueOf(firstNewUsage.getEventType());
                    int updatedTags = calculateTags(recentUsage.getAccountId(), lbId, usageEvent, recentUsage);
                    firstNewUsage.setTags(updatedTags);
                }
            }

            for (int i = 0; i < loadBalancerUsages.size(); i++) {
                if (i < loadBalancerUsages.size() - 1) {
                    LoadBalancerUsage firstUsage = loadBalancerUsages.get(i);
                    LoadBalancerUsage secondUsage = loadBalancerUsages.get(i + 1);

                    // Update firstUsage end time add it to the create list
                    UsageEvent firstUsageEvent = UsageEvent.valueOf(firstUsage.getEventType());
                    int firstUsageTags = calculateTags(firstUsage.getAccountId(), lbId, firstUsageEvent, firstUsage);
                    firstUsage.setTags(firstUsageTags);
                    Calendar newEndTimeForRecentUsage = calculateEndTime(firstUsage.getEndTime(), secondUsage.getStartTime());
                    firstUsage.setEndTime(newEndTimeForRecentUsage);
                    usagesToCreate.add(firstUsage);

                    // New records may be needed if we are near the hour mark or if poller goes down.
                    List<LoadBalancerUsage> bufferRecords = createBufferRecordsIfNeeded(firstUsage, secondUsage);
                    mutateCumulativeFields(firstUsage, bufferRecords, secondUsage);
                    if (!bufferRecords.isEmpty()) usagesToCreate.addAll(bufferRecords);

                    // Update the tags to the proper tags.
                    UsageEvent usageEvent = UsageEvent.valueOf(secondUsage.getEventType());
                    int updatedTags = calculateTags(firstUsage.getAccountId(), lbId, usageEvent, firstUsage);
                    secondUsage.setTags(updatedTags);
                } else {
                    LoadBalancerUsage usage = loadBalancerUsages.get(i);
                    UsageEvent usageEvent = UsageEvent.valueOf(usage.getEventType());
                    int updatedUsageTags = calculateTags(usage.getAccountId(), lbId, usageEvent, usage);
                    usage.setTags(updatedUsageTags);

                    // Add last record whose timestamps are the same.
                    usagesToCreate.add(usage);
                }
            }
        }
    }

    public static void mutateCumulativeFields(LoadBalancerUsage previousUsage, List<LoadBalancerUsage> bufferRecords, LoadBalancerUsage nextUsage) {
        final int MILLISECONDS_PER_HOUR = 60000;
        // Update cumulative fields if the previousUsage and nextUsage are less than an hour apart or if there are no buffer records.
        // This prevents usage records from having a ton of usage if we weren't polling for an extended period of time, but ensures
        // that we properly calculate cumulative fields.
        if (bufferRecords.isEmpty() || Math.abs(nextUsage.getStartTime().getTimeInMillis() - previousUsage.getEndTime().getTimeInMillis()) < MILLISECONDS_PER_HOUR) {
            if (previousUsage.getLastBandwidthBytesIn() != null && nextUsage.getLastBandwidthBytesIn() != null) {
                final Long updatedCumBandwidthBytesIn = UsageCalculator.calculateCumBandwidthBytesIn(previousUsage, nextUsage.getLastBandwidthBytesIn());
                previousUsage.setCumulativeBandwidthBytesIn(updatedCumBandwidthBytesIn);
            }

            if (previousUsage.getLastBandwidthBytesInSsl() != null && nextUsage.getLastBandwidthBytesInSsl() != null) {
                final Long updatedCumBandwidthBytesInSsl = UsageCalculator.calculateCumBandwidthBytesInSsl(previousUsage, nextUsage.getLastBandwidthBytesInSsl());
                previousUsage.setCumulativeBandwidthBytesInSsl(updatedCumBandwidthBytesInSsl);
            }

            if (previousUsage.getLastBandwidthBytesOut() != null && nextUsage.getLastBandwidthBytesOut() != null) {
                final Long updatedCumBandwidthBytesOut = UsageCalculator.calculateCumBandwidthBytesOut(previousUsage, nextUsage.getLastBandwidthBytesOut());
                previousUsage.setCumulativeBandwidthBytesOut(updatedCumBandwidthBytesOut);
            }

            if (previousUsage.getLastBandwidthBytesOutSsl() != null && nextUsage.getLastBandwidthBytesOutSsl() != null) {
                final Long updatedCumBandwidthBytesOutSsl = UsageCalculator.calculateCumBandwidthBytesOutSsl(previousUsage, nextUsage.getLastBandwidthBytesOutSsl());
                previousUsage.setCumulativeBandwidthBytesOutSsl(updatedCumBandwidthBytesOutSsl);
            }
        }
    }

    public static List<LoadBalancerUsage> createBufferRecordsIfNeeded(LoadBalancerUsage previousUsage, LoadBalancerUsage nextUsage) {
        if (nextUsage.getStartTime().before(previousUsage.getEndTime())) {
            System.err.println(String.format("Usages are out of order! Usage id: %d, Usage endTime: %s, Next Usage id: %d, Next usage startTime: %s,", previousUsage.getId(), previousUsage.getEndTime().getTime(), nextUsage.getId(), nextUsage.getStartTime().getTime()));
// throw new RuntimeException("cd!");
        }

        List<LoadBalancerUsage> bufferRecords = new ArrayList<LoadBalancerUsage>();

        Calendar previousRecordsEndTime = (Calendar) previousUsage.getEndTime().clone();
        Calendar nextUsagesStartTime = (Calendar) nextUsage.getStartTime().clone();

        while (previousRecordsEndTime.before(nextUsagesStartTime)) {
            if (isEndOfHour(previousRecordsEndTime)) {
                if (previousRecordsEndTime.before(nextUsagesStartTime)
                        && previousRecordsEndTime.get(Calendar.HOUR_OF_DAY) == nextUsagesStartTime.get(Calendar.HOUR_OF_DAY)
                        && previousRecordsEndTime.get(Calendar.DAY_OF_MONTH) == nextUsagesStartTime.get(Calendar.DAY_OF_MONTH)
                        && previousRecordsEndTime.get(Calendar.YEAR) == nextUsagesStartTime.get(Calendar.YEAR)
                        ) {
                    // We need a buffer record for the beginning of the hour.
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, nextUsagesStartTime);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) nextUsagesStartTime.clone();
                } else if (previousRecordsEndTime.getTimeInMillis() != nextUsagesStartTime.getTimeInMillis()) {
                    // We need a buffer record for the whole hour.
                    Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                    LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                    bufferRecords.add(newBufferRecord);
                    previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
                }
            } else {
                // We need a buffer record for the end of the hour.
                Calendar newEndTimeForBufferRecord = calculateEndTime(previousRecordsEndTime, nextUsagesStartTime);
                LoadBalancerUsage newBufferRecord = instantiateAndPopulateBufferRecord(previousUsage, previousRecordsEndTime, newEndTimeForBufferRecord);
                bufferRecords.add(newBufferRecord);
                previousRecordsEndTime = (Calendar) newEndTimeForBufferRecord.clone();
            }
        }

        return bufferRecords;
    }

    private static LoadBalancerUsage instantiateAndPopulateBufferRecord(LoadBalancerUsage recentUsage, Calendar previousRecordsEndTime, Calendar newEndTimeForBufferRecord) {
        LoadBalancerUsage newBufferRecord = new LoadBalancerUsage();
        newBufferRecord.setAccountId(recentUsage.getAccountId());
        newBufferRecord.setLoadbalancerId(recentUsage.getLoadbalancerId());
        newBufferRecord.setTags(recentUsage.getTags());
        newBufferRecord.setNumVips(recentUsage.getNumVips());
        newBufferRecord.setStartTime((Calendar) previousRecordsEndTime.clone());
        newBufferRecord.setEndTime((Calendar) newEndTimeForBufferRecord.clone());
        if (UsageEvent.SUSPEND_LOADBALANCER.name().equals(recentUsage.getEventType()) || UsageEvent.SUSPENDED_LOADBALANCER.name().equals(recentUsage.getEventType())) {
            newBufferRecord.setEventType(UsageEvent.SUSPENDED_LOADBALANCER.name());
        }
        return newBufferRecord;
    }

    public static boolean isEndOfHour(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0;
    }

    public static Calendar calculateEndTime(Calendar recentUsageEndTime, Calendar nextUsageStartTime) {
        if (nextUsageStartTime.before(recentUsageEndTime)) {
            System.err.println(String.format("Usages are out of order! nextUsageStartTime: %s, recentUsageEndTime: %s,", nextUsageStartTime.getTime(), recentUsageEndTime.getTime()));
// throw new RuntimeException("Usages are not in order!");
        }

        if (recentUsageEndTime.get(Calendar.HOUR_OF_DAY) == nextUsageStartTime.get(Calendar.HOUR_OF_DAY)
                && recentUsageEndTime.get(Calendar.DAY_OF_MONTH) == nextUsageStartTime.get(Calendar.DAY_OF_MONTH)
                && recentUsageEndTime.get(Calendar.MONTH) == nextUsageStartTime.get(Calendar.MONTH)
                && recentUsageEndTime.get(Calendar.YEAR) == nextUsageStartTime.get(Calendar.YEAR)
                && (recentUsageEndTime.get(Calendar.MINUTE) != 0
                || recentUsageEndTime.get(Calendar.SECOND) != 0
                || recentUsageEndTime.get(Calendar.MILLISECOND) != 0)) {
            return nextUsageStartTime;
        }

        // Return a new end time that reaches the very end of the hour
        Calendar newEndTime = Calendar.getInstance();
        newEndTime.setTime(recentUsageEndTime.getTime());
        if ((recentUsageEndTime.get(Calendar.HOUR_OF_DAY) != nextUsageStartTime.get(Calendar.HOUR_OF_DAY)
                || recentUsageEndTime.get(Calendar.DAY_OF_MONTH) != nextUsageStartTime.get(Calendar.DAY_OF_MONTH)
                || recentUsageEndTime.get(Calendar.MONTH) != nextUsageStartTime.get(Calendar.MONTH)
                || recentUsageEndTime.get(Calendar.YEAR) != nextUsageStartTime.get(Calendar.YEAR))
                || (newEndTime.get(Calendar.MINUTE) != 0
                || newEndTime.get(Calendar.SECOND) != 0
                || newEndTime.get(Calendar.MILLISECOND) != 0)) {
            newEndTime.set(Calendar.MINUTE, 59);
            newEndTime.set(Calendar.SECOND, 59);
            newEndTime.set(Calendar.MILLISECOND, 999);
            newEndTime.add(Calendar.MILLISECOND, 1);
        }
        return newEndTime;
    }

    private Map<Integer, List<LoadBalancerUsage>> createEventUsageMap() {
        Map<Integer, List<LoadBalancerUsage>> newEventUsageMap = new HashMap<Integer, List<LoadBalancerUsage>>();

        for (LoadBalancerUsageEvent inOrderUsageEventEntry : inOrderUsageEventEntries) {
            Integer key = inOrderUsageEventEntry.getLoadbalancerId();
            if (newEventUsageMap.containsKey(key)) {
                LoadBalancerUsage newUsage = createNewUsageFromEvent(inOrderUsageEventEntry);
                newEventUsageMap.get(key).add(newUsage);
            } else {
                List<LoadBalancerUsage> usages = new ArrayList<LoadBalancerUsage>();
                LoadBalancerUsage newUsage = createNewUsageFromEvent(inOrderUsageEventEntry);
                usages.add(newUsage);
                newEventUsageMap.put(key, usages);
            }
        }

        return newEventUsageMap;
    }

    public LoadBalancerUsage createNewUsageFromEvent(LoadBalancerUsageEvent inOrderUsageEventEntry) {
        LoadBalancerUsage newUsage = new LoadBalancerUsage();

        newUsage.setAccountId(inOrderUsageEventEntry.getAccountId());
        newUsage.setLoadbalancerId(inOrderUsageEventEntry.getLoadbalancerId());
        newUsage.setNumVips(inOrderUsageEventEntry.getNumVips());
        newUsage.setStartTime(inOrderUsageEventEntry.getStartTime());
        newUsage.setEndTime(inOrderUsageEventEntry.getStartTime());
        if (inOrderUsageEventEntry.getEventType().equals(UsageEvent.SUSPEND_LOADBALANCER.name()) ||
                inOrderUsageEventEntry.getEventType().equals(UsageEvent.SUSPENDED_LOADBALANCER.name())) {
            newUsage.getEndTime().add(Calendar.SECOND, 1);
        }
        newUsage.setNumberOfPolls(0);
        newUsage.setTags(0); // Will most likely change in 2nd pass
        newUsage.setEventType(inOrderUsageEventEntry.getEventType());
        newUsage.setLastBandwidthBytesIn(inOrderUsageEventEntry.getLastBandwidthBytesIn());
        newUsage.setLastBandwidthBytesInSsl(inOrderUsageEventEntry.getLastBandwidthBytesInSsl());
        newUsage.setLastBandwidthBytesOut(inOrderUsageEventEntry.getLastBandwidthBytesOut());
        newUsage.setLastBandwidthBytesOutSsl(inOrderUsageEventEntry.getLastBandwidthBytesOutSsl());

        return newUsage;
    }

    public Map<Integer, LoadBalancerUsage> createRecentUsageMap(Set<Integer> loadBalancerIds) {
        Map<Integer, LoadBalancerUsage> recentUsageMap = new HashMap<Integer, LoadBalancerUsage>();

        for (Integer loadBalancerId : loadBalancerIds) {
            LoadBalancerUsage mostRecentUsageForLoadBalancer = getMostRecentLbUsageFromHourlyLbUsageTable(loadBalancerId);
            if (mostRecentUsageForLoadBalancer != null)
                recentUsageMap.put(loadBalancerId, mostRecentUsageForLoadBalancer);
        }

        return recentUsageMap;
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, Usage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        return calculateTags(accountId, lbId, usageEvent, tags);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, LoadBalancerUsage recentUsage) {
        BitTags tags;

        if (recentUsage != null) {
            tags = new BitTags(recentUsage.getTags());
        } else {
            tags = new BitTags();
        }

        return calculateTags(accountId, lbId, usageEvent, tags);
    }

    public int calculateTags(Integer accountId, Integer lbId, UsageEvent usageEvent, BitTags bitTags) {
        BitTags tags = new BitTags(bitTags.toInt());

        switch (usageEvent) {
            case CREATE_LOADBALANCER:
                tags.flipAllTagsOff();
                break;
            case DELETE_LOADBALANCER:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_OFF:
                tags.flipTagOff(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_ONLY_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOff(BitTag.SSL_MIXED_MODE);
                break;
            case SSL_MIXED_ON:
                tags.flipTagOn(BitTag.SSL);
                tags.flipTagOn(BitTag.SSL_MIXED_MODE);
                break;
            default:
        }

        if (isServiceNetLoadBalancer(lbId)) {
            tags.flipTagOn(BitTag.SERVICENET_LB);
        }

        return tags.toInt();
    }

    public boolean isServiceNetLoadBalancer(Integer lbId) {
        final List<VirtualIp> vipsByAccountIdLoadBalancerId = getVipsByLbId(lbId);

        if (vipsByAccountIdLoadBalancerId.isEmpty()) return false;

        for (VirtualIp virtualIp : vipsByAccountIdLoadBalancerId) {
            if (virtualIp.getVipType().equals(VirtualIpType.SERVICENET)) return true;
        }

        return false;
    }

    private Usage getMostRecentLbUsageFromMainLbUsageTable(Integer loadbalancerId) {
        final SessionFactory sessionFactory = loadbalancingHibernateConfig.buildSessionFactory();
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

    private LoadBalancerUsage getMostRecentLbUsageFromHourlyLbUsageTable(Integer loadbalancerId) {
        final SessionFactory sessionFactory = loadbalancingUsageHibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<Object[]> resultList = new ArrayList<Object[]>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving most recent lb_usage from 'loadbalancing_usage' db for loadbalancer '%d'...", loadbalancerId));
            resultList = session.createSQLQuery("SELECT u.id, u.account_id, u.loadbalancer_id, u.avg_concurrent_conns, u.cum_bandwidth_bytes_in, u.cum_bandwidth_bytes_out, u.last_bandwidth_bytes_in, u.last_bandwidth_bytes_out, u.avg_concurrent_conns_ssl, u.cum_bandwidth_bytes_in_ssl, u.cum_bandwidth_bytes_out_ssl, u.last_bandwidth_bytes_in_ssl, u.last_bandwidth_bytes_out_ssl, u.start_time, u.end_time, u.num_polls, u.num_vips, u.tags_bitmask, u.event_type FROM lb_usage u WHERE u.loadbalancer_id = :loadbalancerId ORDER BY u.start_time DESC LIMIT 1")
                    .setParameter("loadbalancerId", loadbalancerId)
                    .list();
            System.out.println(String.format("Number of items retrieved from lb_usage from 'loadbalancing_usage' db for loadbalancer '%d': %d", loadbalancerId, resultList.size()));
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
        return rowToLoadBalancerUsage(row);
    }

    private LoadBalancerUsage rowToLoadBalancerUsage(Object[] row) {
        Long startTimeMillis = ((Timestamp) row[13]).getTime();
        Long endTimeMillis = ((Timestamp) row[14]).getTime();
        Calendar startTimeCal = new GregorianCalendar();
        Calendar endTimeCal = new GregorianCalendar();
        startTimeCal.setTimeInMillis(startTimeMillis);
        endTimeCal.setTimeInMillis(endTimeMillis);

        LoadBalancerUsage usageItem = new LoadBalancerUsage();
        usageItem.setId((Integer) row[0]);
        usageItem.setAccountId((Integer) row[1]);
        usageItem.setLoadbalancerId((Integer) row[2]);
        usageItem.setAverageConcurrentConnections((Double) row[3]);
        usageItem.setCumulativeBandwidthBytesIn(((BigInteger) row[4]).longValue());
        usageItem.setCumulativeBandwidthBytesOut(((BigInteger) row[5]).longValue());
        usageItem.setLastBandwidthBytesIn(((BigInteger) row[6]).longValue());
        usageItem.setLastBandwidthBytesOut(((BigInteger) row[7]).longValue());
        usageItem.setAverageConcurrentConnectionsSsl((Double) row[8]);
        usageItem.setCumulativeBandwidthBytesInSsl(((BigInteger) row[9]).longValue());
        usageItem.setCumulativeBandwidthBytesOutSsl(((BigInteger) row[10]).longValue());
        usageItem.setLastBandwidthBytesInSsl(((BigInteger) row[11]).longValue());
        usageItem.setLastBandwidthBytesOutSsl(((BigInteger) row[12]).longValue());
        usageItem.setStartTime(startTimeCal);
        usageItem.setEndTime(endTimeCal);
        usageItem.setNumberOfPolls((Integer) row[15]);
        usageItem.setNumVips((Integer) row[16]);
        usageItem.setTags((Integer) row[17]);
        usageItem.setEventType((String) row[18]);
        return usageItem;
    }

    private List<VirtualIp> getVipsByLbId(Integer loadbalancerId) {
        final SessionFactory sessionFactory = loadbalancingHibernateConfig.buildSessionFactory();
        final Session session = sessionFactory.openSession();
        List<VirtualIp> virtualIps = new ArrayList<VirtualIp>();
        List<Object[]> resultList = new ArrayList<Object[]>();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            System.out.println(String.format("Retrieving vips from loadbalancing db for lb '%d'...", loadbalancerId));
            resultList = session.createSQLQuery("SELECT v.id, v.ip_address, v.last_allocation, v.last_deallocation, v.type, v.cluster_id, v.is_allocated from loadbalancer_virtualip j JOIN virtual_ip_ipv4 v on j.virtualip_id = v.id WHERE j.loadbalancer_id = :lbId").setParameter("lbId", loadbalancerId).list();
            System.out.println(String.format("Number of vips retrieved from loadbalancing db for lb '%d': %d", loadbalancerId, resultList.size()));
            tx.commit();
        } catch (Exception e) {
            System.err.print(e);
            if (tx != null) tx.rollback();
        } finally {
            session.close();
            sessionFactory.close();
        }

        for (Object[] object : resultList) {
            virtualIps.add(rowToVirtualIp(object));
        }

        return virtualIps;
    }

    private VirtualIp rowToVirtualIp(Object[] row) {
        Long lastAllocationMillis = ((Timestamp) row[2]).getTime();
        Long lastDeallocationMillis = ((Timestamp) row[3]).getTime();
        Calendar lastAllocationCal = new GregorianCalendar();
        Calendar lastDeallocationCal = new GregorianCalendar();
        lastAllocationCal.setTimeInMillis(lastAllocationMillis);
        lastDeallocationCal.setTimeInMillis(lastDeallocationMillis);

        VirtualIp vip = new VirtualIp();

        vip.setId((Integer) row[0]);
        vip.setIpAddress((String) row[1]);
        vip.setLastAllocation(lastAllocationCal);
        vip.setLastDeallocation(lastDeallocationCal);
        vip.setVipType(VirtualIpType.valueOf((String) row[4]));
        Cluster cluster = new Cluster();
        cluster.setId((Integer) row[5]);
        vip.setCluster(cluster);
        vip.setAllocated((Boolean) row[6]);

        return vip;
    }
}