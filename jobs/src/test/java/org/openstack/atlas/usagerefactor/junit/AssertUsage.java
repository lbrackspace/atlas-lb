package org.openstack.atlas.usagerefactor.junit;

import org.junit.Assert;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.util.common.CalendarUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AssertUsage {

    public static void hasValues(Integer id, Integer accountId, Integer lbId, Long incomingTransfer,
                                 Long incomingTransferSsl, Long outgoingTransfer, Long outgoingTransferSsl,
                                 Double avgCcs, Double avgCcsSsl, String startTime, String endTime,
                                 Integer numPolls, Integer numVips, Integer tags, String eventType,
                                 Integer entryVersion, boolean needsPushed, String uuid, Usage actualUsage) throws ParseException {
        Assert.assertEquals(id, actualUsage.getId());
        Assert.assertEquals(accountId, actualUsage.getAccountId());
        Assert.assertEquals(lbId, actualUsage.getLoadbalancer().getId());
        Assert.assertEquals(incomingTransfer, actualUsage.getIncomingTransfer());
        Assert.assertEquals(incomingTransferSsl, actualUsage.getIncomingTransferSsl());
        Assert.assertEquals(outgoingTransfer, actualUsage.getOutgoingTransfer());
        Assert.assertEquals(outgoingTransferSsl, actualUsage.getOutgoingTransferSsl());
        Assert.assertEquals(avgCcs, actualUsage.getAverageConcurrentConnections(), .001);
        Assert.assertEquals(avgCcsSsl, actualUsage.getAverageConcurrentConnectionsSsl(), .001);
        Assert.assertEquals(CalendarUtils.stringToCalendar(startTime), actualUsage.getStartTime());
        Assert.assertEquals(CalendarUtils.stringToCalendar(endTime), actualUsage.getEndTime());
        Assert.assertEquals(numPolls, actualUsage.getNumberOfPolls());
        Assert.assertEquals(numVips, actualUsage.getNumVips());
        Assert.assertEquals(tags, actualUsage.getTags());
        Assert.assertEquals(eventType, actualUsage.getEventType());
        Assert.assertEquals(entryVersion, actualUsage.getEntryVersion());
        Assert.assertEquals(needsPushed, actualUsage.isNeedsPushed());
        Assert.assertEquals(uuid, actualUsage.getUuid());
    }
}
