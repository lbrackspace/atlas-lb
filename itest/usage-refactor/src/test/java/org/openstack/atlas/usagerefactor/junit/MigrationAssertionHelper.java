package org.openstack.atlas.usagerefactor.junit;

import junit.framework.Assert;
import org.openstack.atlas.service.domain.events.UsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerMergedHostUsage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MigrationAssertionHelper {

    public static void assertLoadBalancerMergedHostUsage(int accountId, int loadbalancerId, long outgoingTransfer,
                                                         long incomingTransfer, long outgoingTransferSsl, long incomingTransferSsl,
                                                         long concurrentConnections, long concurrentConnectionsSsl, int numVips, int tagsBitmask,
                                                         String pollTime, UsageEvent eventType, LoadBalancerMergedHostUsage usage) {
        org.junit.Assert.assertEquals(accountId, usage.getAccountId());
        org.junit.Assert.assertEquals(loadbalancerId, usage.getLoadbalancerId());
        org.junit.Assert.assertEquals(incomingTransfer, usage.getIncomingTransfer());
        org.junit.Assert.assertEquals(incomingTransferSsl, usage.getIncomingTransferSsl());
        org.junit.Assert.assertEquals(outgoingTransfer, usage.getOutgoingTransfer());
        org.junit.Assert.assertEquals(outgoingTransferSsl, usage.getOutgoingTransferSsl());
        org.junit.Assert.assertEquals(concurrentConnections, usage.getConcurrentConnections());
        org.junit.Assert.assertEquals(concurrentConnectionsSsl, usage.getConcurrentConnectionsSsl());
        org.junit.Assert.assertEquals(numVips, usage.getNumVips());
        org.junit.Assert.assertEquals(tagsBitmask, usage.getTagsBitmask());
        org.junit.Assert.assertEquals(eventType, usage.getEventType());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String actualTimeStr = sdf.format(usage.getPollTime().getTime());
        org.junit.Assert.assertEquals(pollTime, actualTimeStr);
    }

    private static Calendar stringToCalendar(String calAsString) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTime(sdf.parse(calAsString));
        return cal;
    }
}
