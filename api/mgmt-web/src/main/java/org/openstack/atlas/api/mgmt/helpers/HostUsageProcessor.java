package org.openstack.atlas.api.mgmt.helpers;

import org.openstack.atlas.api.helpers.CalendarHelper;
import org.openstack.atlas.service.domain.pojos.HostUsageRecord;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;

import java.util.*;

public final class HostUsageProcessor {
    /* This variable is used to determine if Zeus really got reset.*/
    public static final long ZEUS_RESET_THRESHOLD_BYTES = 1073741824; // 1024 * 1024 * 1024 = 1073741824 => 1GB

    /*
     * NOTE: This method assumes that the usage records are sorted by ascending snapshot time
     */
    public static List<HostUsageRecord> processRawHostUsageData(List<HostUsage> rawHostUsage) {
        List<HostUsageRecord> hostUsageRecords = new ArrayList<HostUsageRecord>();

        if (rawHostUsage == null) return hostUsageRecords;

        Map<Integer, Map<Calendar, List<HostUsage>>> dailyHostUsageMap = convertRawUsageToMap(rawHostUsage);
        for (Integer hostId : dailyHostUsageMap.keySet()) {
            HostUsageRecord hostUsageRecord = new HostUsageRecord();
            hostUsageRecord.setHostId(hostId);
            hostUsageRecord.setHostUsages(processDailyHostUsageMap(dailyHostUsageMap.get(hostId)));
            hostUsageRecords.add(hostUsageRecord);
        }

        return hostUsageRecords;
    }

    private static List<org.openstack.atlas.service.domain.pojos.HostUsage> processDailyHostUsageMap(Map<Calendar, List<HostUsage>> dailyHostUsageMap) {
        List<org.openstack.atlas.service.domain.pojos.HostUsage> dailyHostUsageList = new ArrayList<org.openstack.atlas.service.domain.pojos.HostUsage>();
        HostUsage lastUsageOfPreviousDay = null;

        if (dailyHostUsageMap == null) return dailyHostUsageList;

        for (Calendar day : dailyHostUsageMap.keySet()) {
            org.openstack.atlas.service.domain.pojos.HostUsage dailyHostUsage = new org.openstack.atlas.service.domain.pojos.HostUsage();
            final List<HostUsage> hostUsageListForDay = dailyHostUsageMap.get(day);
            dailyHostUsage.setDay(day);
            dailyHostUsage.setBandwidthIn(processBandwidthIn(hostUsageListForDay, lastUsageOfPreviousDay));
            dailyHostUsage.setBandwidthOut(processBandwidthOut(hostUsageListForDay, lastUsageOfPreviousDay));
            dailyHostUsageList.add(dailyHostUsage);
            lastUsageOfPreviousDay = hostUsageListForDay.get(hostUsageListForDay.size() - 1); // Need this record so we don't lose 5 mins of info
        }

        return dailyHostUsageList;
    }

    /*
     * NOTE: This method assumes that the usage records are sorted by ascending snapshot time
     */
    private static Long processBandwidthIn(List<HostUsage> hostUsages, HostUsage tagOnRecord) {
        Long cumulativeBytes = 0l;
        Long currentBytesSnapshot = null;
        Long lastBytesSnapshot = null;

        if (tagOnRecord != null) {
            lastBytesSnapshot = tagOnRecord.getBandwidthBytesIn();
        }

        for (HostUsage hostUsage : hostUsages) {
            currentBytesSnapshot = hostUsage.getBandwidthBytesIn();

            if (lastBytesSnapshot != null) {
                long delta = currentBytesSnapshot - lastBytesSnapshot;

                if (delta >= 0l) {
                    cumulativeBytes += delta;
                } else if (Math.abs(delta) >= ZEUS_RESET_THRESHOLD_BYTES) {
                    // Zeus's counter reset.
                    cumulativeBytes += currentBytesSnapshot;
                }
            }

            lastBytesSnapshot = currentBytesSnapshot;
        }

        return cumulativeBytes;
    }

    /*
     * NOTE: This method assumes that the usage records are sorted by ascending snapshot time
     */
    private static Long processBandwidthOut(List<HostUsage> hostUsages, HostUsage tagOnRecord) {
        Long cumulativeBytes = 0l;
        Long currentBytesSnapshot = null;
        Long lastBytesSnapshot = null;

        if (tagOnRecord != null) {
            lastBytesSnapshot = tagOnRecord.getBandwidthBytesOut();
        }

        for (HostUsage hostUsage : hostUsages) {
            currentBytesSnapshot = hostUsage.getBandwidthBytesOut();

            if (lastBytesSnapshot != null) {
                long delta = currentBytesSnapshot - lastBytesSnapshot;

                if (delta >= 0l) {
                    cumulativeBytes += delta;
                } else if (Math.abs(delta) >= ZEUS_RESET_THRESHOLD_BYTES) {
                    // Zeus's counter reset.
                    cumulativeBytes += currentBytesSnapshot;
                }
            }

            lastBytesSnapshot = currentBytesSnapshot;
        }

        return cumulativeBytes;
    }

    /*
     *  Returns a map with hostId ==> (day ==> List<HostUsage>). This method assumes that the usage
     *  records are sorted by ascending snapshot time
     */
    private static Map<Integer, Map<Calendar, List<HostUsage>>> convertRawUsageToMap(List<HostUsage> rawHostUsage) {
        Map<Integer, Map<Calendar, List<HostUsage>>> dailyHostUsageMap = new HashMap<Integer, Map<Calendar, List<HostUsage>>>();

        if (rawHostUsage == null) return dailyHostUsageMap;

        for (HostUsage hostUsage : rawHostUsage) {
            Integer hostKey = hostUsage.getHostId();

            if (!dailyHostUsageMap.containsKey(hostKey)) {
                Map<Calendar, List<HostUsage>> mapForHost = new TreeMap<Calendar, List<HostUsage>>();
                dailyHostUsageMap.put(hostKey, mapForHost);
            }

            Calendar calKey = CalendarHelper.zeroOutTime(hostUsage.getSnapshotTime());
            Map<Calendar, List<HostUsage>> mapForHost = dailyHostUsageMap.get(hostUsage.getHostId());

            if (!mapForHost.containsKey(calKey)) {
                List<HostUsage> dailyHostUsageList = new ArrayList<HostUsage>();
                mapForHost.put(calKey, dailyHostUsageList);
            }

            mapForHost.get(calKey).add(hostUsage);
        }

        return dailyHostUsageMap;
    }
}
