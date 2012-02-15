package org.openstack.atlas.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogFileUtilTest extends TestCase {

    @Test
    public void testGetLogFileTime() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String logFileTime = LogFileUtil.getLogFileTime(absoluteFileName);
        Assert.assertEquals(logFileTime, "2012021005");
    }

    @Test
    public void testGetLoadBalancerId() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String loadBalancerId = LogFileUtil.getLoadBalancerId(absoluteFileName);
        Assert.assertEquals(loadBalancerId, "10");
    }

    @Test
    public void testGetAccountId() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String accountId = LogFileUtil.getAccountId(absoluteFileName);
        Assert.assertEquals(accountId, "1");
    }

    @Test
    public void testGetDateStringFromFileName() throws Exception {
        String absoluteFileName = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        String dateString = LogFileUtil.getDateStringFromFileName(absoluteFileName);
        Assert.assertEquals(dateString, "2011021513");
    }

    @Test
    public void testGetDateStringFromFileNameWhenInvalidFileName() {
        String absoluteFileName = "/var/log/zxtm/rotated/new-access_log.aggregated";
        boolean expectedException = false;
        try {
            LogFileUtil.getDateStringFromFileName(absoluteFileName);
        } catch(IllegalArgumentException e) {
            expectedException = true;
        }
        Assert.assertEquals(true, expectedException);
    }

    @Test
    public void testGetDateFromFileName() throws Exception {
        String absoluteFileName = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        Date date = LogFileUtil.getDateFromFileName(absoluteFileName);
        Assert.assertEquals(date, LogFileUtil.getDate("2011021513", LogFileUtil.filedf));
    }

    @Test
    public void testGetNewestFile() throws Exception {
        String absoluteFileName1 = "/var/log/zxtm/rotated/2011021512-access_log.aggregated";
        String absoluteFileName2 = "/var/log/zxtm/rotated/2011021513-access_log.aggregated";
        String absoluteFileName3 = "/var/log/zxtm/rotated/2011021511-access_log.aggregated";
        List<String> files = new ArrayList<String>();
        files.add(absoluteFileName1);
        files.add(absoluteFileName2);
        files.add(absoluteFileName3);

        String newestFile = LogFileUtil.getNewestFile(files);
        Assert.assertEquals(newestFile, absoluteFileName2);
    }

    @Test
    public void testGetMonthYearFromFileDate() throws Exception {
        String dateString = "2011021512";

        String monthYear = LogFileUtil.getMonthYearFromFileDate(dateString);
        Assert.assertEquals(monthYear, "Feb_2011");
    }
}