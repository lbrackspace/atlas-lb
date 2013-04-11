package org.openstack.atlas.usagerefactor.junit;

import org.junit.Assert;
import org.openstack.atlas.service.domain.entities.Usage;

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
        Assert.assertEquals(avgCcs, actualUsage.getAverageConcurrentConnections());
        Assert.assertEquals(avgCcsSsl, actualUsage.getAverageConcurrentConnectionsSsl());
        Assert.assertEquals(stringToCalendar(startTime), actualUsage.getStartTime());
        Assert.assertEquals(stringToCalendar(endTime), actualUsage.getEndTime());
        Assert.assertEquals(numPolls, actualUsage.getNumberOfPolls());
        Assert.assertEquals(numVips, actualUsage.getNumVips());
        Assert.assertEquals(tags, actualUsage.getTags());
        Assert.assertEquals(eventType, actualUsage.getEventType());
        Assert.assertEquals(entryVersion, actualUsage.getEntryVersion());
        Assert.assertEquals(needsPushed, actualUsage.isNeedsPushed());
        Assert.assertEquals(uuid, actualUsage.getUuid());
    }
    
    private static Calendar stringToCalendar(String calAsString) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTime(sdf.parse(calAsString));
        return cal;
    }
}
