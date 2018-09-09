package org.openstack.atlas.api.mgmt.helpers;

import org.openstack.atlas.service.domain.pojos.HostUsageRecord;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;
import org.openstack.atlas.api.helpers.CalendarHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class HostUsageProcessorTest {

    private List<HostUsage> rawHostUsage;

    @Before
    public void standUp() {
        rawHostUsage = new ArrayList<HostUsage>();
    }

    @Test
    public void shouldReturnNoUsageWhenPassingNull() {
        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(null);
        Assert.assertTrue(hostUsageRecords.isEmpty());
    }

    @Test
    public void shouldReturnNoUsageWhenThereIsNoRawData() {
        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(new ArrayList<HostUsage>());
        Assert.assertTrue(hostUsageRecords.isEmpty());
    }

    @Test
    public void shouldReturnOneUsageRecordWithZeroUsagePerHostWhenThereIsOneSnapshotRecordPerHost() {
        Calendar now = Calendar.getInstance();
        HostUsage usage1 = createHostUsage(1, 1, 100l, 100l, now);
        rawHostUsage.add(usage1);

        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawHostUsage);
        Assert.assertEquals(1, hostUsageRecords.size());

        final HostUsageRecord record = hostUsageRecords.get(0);
        Assert.assertEquals(new Long(0), record.getHostUsages().get(0).getBandwidthIn());
        Assert.assertEquals(new Long(0), record.getHostUsages().get(0).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(now), record.getHostUsages().get(0).getDay());
    }

    @Test
    public void shouldReturnProperUsageWhenThereAreValidRawUsageRecordsInOneDay() {
        Calendar startOfToday = CalendarHelper.zeroOutTime(Calendar.getInstance());
        Calendar fiveMinutesLater = startOfToday;
        fiveMinutesLater.add(Calendar.MINUTE, 5);
        
        HostUsage usage1 = createHostUsage(1, 1, 100l, 100l, startOfToday);
        HostUsage usage2 = createHostUsage(1, 1, 500l, 500l, fiveMinutesLater);
        rawHostUsage.add(usage1);
        rawHostUsage.add(usage2);

        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawHostUsage);
        Assert.assertEquals(1, hostUsageRecords.size());

        final HostUsageRecord record = hostUsageRecords.get(0);
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthIn());
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfToday), record.getHostUsages().get(0).getDay());
    }

    @Test
    public void shouldReturnProperUsageWhenThereAreValidRawUsageRecordsBelowThresholdInTwoDays() {
        Calendar startOfToday = CalendarHelper.zeroOutTime(Calendar.getInstance());
        Calendar fiveMinutesLater = startOfToday;
        fiveMinutesLater.add(Calendar.MINUTE, 5);
        Calendar tenMinutesLater = startOfToday;
        tenMinutesLater.add(Calendar.MINUTE, 10);
        Calendar startOfTomorrow = CalendarHelper.zeroOutTime(Calendar.getInstance());
        startOfTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar tomorrowFiveMinutesLater = startOfTomorrow;
        tomorrowFiveMinutesLater.add(Calendar.MINUTE, 5);

        HostUsage usage1 = createHostUsage(1, 1, 100l, 100l, startOfToday);
        HostUsage usage2 = createHostUsage(1, 1, 500l, 500l, fiveMinutesLater);
        HostUsage usage3 = createHostUsage(1, 1, 500l, 500l, tenMinutesLater);
        HostUsage usage4 = createHostUsage(1, 1, 900l, 900l, startOfTomorrow);
        HostUsage usage5 = createHostUsage(1, 1, 300l, 300l, tomorrowFiveMinutesLater); // This record should get ignored
        rawHostUsage.add(usage1);
        rawHostUsage.add(usage2);
        rawHostUsage.add(usage3);
        rawHostUsage.add(usage4);
        rawHostUsage.add(usage5);

        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawHostUsage);
        Assert.assertEquals(1, hostUsageRecords.size());

        final HostUsageRecord record = hostUsageRecords.get(0);
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthIn());
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfToday), record.getHostUsages().get(0).getDay());
        Assert.assertEquals(new Long(400), record.getHostUsages().get(1).getBandwidthIn());
        Assert.assertEquals(new Long(400), record.getHostUsages().get(1).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfTomorrow), record.getHostUsages().get(1).getDay());
    }

    @Test
    public void shouldReturnProperUsageWhenThereAreValidRawUsageRecordsAboveThresholdInTwoDays() {
        Calendar startOfToday = CalendarHelper.zeroOutTime(Calendar.getInstance());
        Calendar fiveMinutesLater = startOfToday;
        fiveMinutesLater.add(Calendar.MINUTE, 5);
        Calendar tenMinutesLater = startOfToday;
        tenMinutesLater.add(Calendar.MINUTE, 10);
        Calendar startOfTomorrow = CalendarHelper.zeroOutTime(Calendar.getInstance());
        startOfTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar tomorrowFiveMinutesLater = startOfTomorrow;
        tomorrowFiveMinutesLater.add(Calendar.MINUTE, 5);

        HostUsage usage1 = createHostUsage(1, 1, 10000000000l, 10000000000l, startOfToday);
        HostUsage usage2 = createHostUsage(1, 1, 50000000000l, 50000000000l, fiveMinutesLater);
        HostUsage usage3 = createHostUsage(1, 1, 50000000000l, 50000000000l, tenMinutesLater);
        HostUsage usage4 = createHostUsage(1, 1, 80000000000l, 80000000000l, startOfTomorrow);
        HostUsage usage5 = createHostUsage(1, 1, 30000000000l, 30000000000l, tomorrowFiveMinutesLater);
        rawHostUsage.add(usage1);
        rawHostUsage.add(usage2);
        rawHostUsage.add(usage3);
        rawHostUsage.add(usage4);
        rawHostUsage.add(usage5);

        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawHostUsage);
        Assert.assertEquals(1, hostUsageRecords.size());

        final HostUsageRecord record = hostUsageRecords.get(0);
        Assert.assertEquals(new Long(40000000000l), record.getHostUsages().get(0).getBandwidthIn());
        Assert.assertEquals(new Long(40000000000l), record.getHostUsages().get(0).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfToday), record.getHostUsages().get(0).getDay());
        Assert.assertEquals(new Long(60000000000l), record.getHostUsages().get(1).getBandwidthIn());
        Assert.assertEquals(new Long(60000000000l), record.getHostUsages().get(1).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfTomorrow), record.getHostUsages().get(1).getDay());
    }

    @Test
    public void shouldReturnProperUsageWhenThereAreValidRawUsageRecordsInTwoDaysAndZeusResets() {
        Calendar startOfToday = CalendarHelper.zeroOutTime(Calendar.getInstance());
        Calendar fiveMinutesLater = startOfToday;
        fiveMinutesLater.add(Calendar.MINUTE, 5);
        Calendar startOfTomorrow = CalendarHelper.zeroOutTime(Calendar.getInstance());
        startOfTomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar tomorrowFiveMinutesLater = startOfTomorrow;
        tomorrowFiveMinutesLater.add(Calendar.MINUTE, 5);

        HostUsage usage1 = createHostUsage(1, 1, 100l, 100l, startOfToday);
        HostUsage usage2 = createHostUsage(1, 1, 500l, 500l, fiveMinutesLater);
        HostUsage usage3 = createHostUsage(1, 1, 0l, 0l, startOfTomorrow);
        HostUsage usage4 = createHostUsage(1, 1, 300l, 300l, tomorrowFiveMinutesLater);
        rawHostUsage.add(usage1);
        rawHostUsage.add(usage2);
        rawHostUsage.add(usage3);
        rawHostUsage.add(usage4);

        List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawHostUsage);
        Assert.assertEquals(1, hostUsageRecords.size());

        final HostUsageRecord record = hostUsageRecords.get(0);
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthIn());
        Assert.assertEquals(new Long(400), record.getHostUsages().get(0).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfToday), record.getHostUsages().get(0).getDay());
        Assert.assertEquals(new Long(300), record.getHostUsages().get(1).getBandwidthIn());
        Assert.assertEquals(new Long(300), record.getHostUsages().get(1).getBandwidthOut());
        Assert.assertEquals(CalendarHelper.zeroOutTime(startOfTomorrow), record.getHostUsages().get(1).getDay());
    }

    private HostUsage createHostUsage(Integer id, Integer hostId, Long bandwidthInSnapshot, Long bandwidthOutSnapshot, Calendar snapshotTime) {
        HostUsage hostUsage = new HostUsage();
        hostUsage.setId(id);
        hostUsage.setHostId(hostId);
        hostUsage.setBandwidthBytesIn(bandwidthInSnapshot);
        hostUsage.setBandwidthBytesOut(bandwidthOutSnapshot);
        hostUsage.setSnapshotTime(snapshotTime);
        return hostUsage;
    }
}
