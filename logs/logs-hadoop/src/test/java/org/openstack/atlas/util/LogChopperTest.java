package org.openstack.atlas.util;

import org.openstack.atlas.logs.hadoop.util.LogChopper;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import org.openstack.atlas.io.LbLogsWritable;
import org.openstack.atlas.logs.hadoop.writables.LogMapperOutputValue;

public class LogChopperTest extends TestCase {

    private LogMapperOutputValue value = new LogMapperOutputValue();

    @Test
    public void testNonHTTPWithSSL() throws Exception {
        String testLineWithSSL = "495819_14490_S [27/Jan/2012:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";
        String testLineWithOutSSL = "495819_14490 [27/Jan/2012:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";
        LogChopper.getLogLineValues(testLineWithSSL, value);
        String serverName = "495819_14490";
        Assert.assertEquals(serverName, value.getLoadbalancerName());
    }

    @Test
    public void testNonHTTPWithoutSSL() throws Exception {
        String testLineWithOutSSL = "495819_14490 [27/Jan/2013:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";
        String lbName = "495819_14490";
        LogChopper.getLogLineValues(testLineWithOutSSL, value);
        Assert.assertEquals(lbName, value.getLoadbalancerName());
    }

    @Test
    public void testHTTPWithSSL() throws Exception {
        String testLineWithSSL = "554867_15815_S toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/201:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\"";
        String nameWithOutSSL = "554867_15815";
        LogChopper.getLogLineValues(testLineWithSSL, value);
        Assert.assertEquals(nameWithOutSSL, value.getLoadbalancerName());

    }

    @Test
    public void testHTTPWithoutSSL() throws Exception {
        String testLineWithOutSSL = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\"";
        String lbName = "554867_15815";
        LogChopper.getLogLineValues(testLineWithOutSSL, value);
        Assert.assertEquals(lbName, value.getLoadbalancerName());
    }

    @Test
    public void testWithBorkedWhitespace() throws Exception {
        String testBorkedLine = "5909655_89317 - 184.106.32.84 - - [24/Apr/2014:19:22:31 +0000] \"GET HTTP/1.0 \" - 304 \"-\" \"-\" 50.56.235.33:80";
        LogChopper.getLogLineValues(testBorkedLine, value);
        Assert.assertEquals(testBorkedLine + "\n", value.getLogLine());
    }

    @Test
    public void testOddPlayerNames() throws Exception {
        String testBorked = "123456_123456 www.foo.org 102.61.23.23 - Player name [29/May/2014:11:49:44 +0000] \"GET /services/mmo/get.php?op=pets&cb=9531 HTTP/1.1\" 200 822 \"-\" \"-\" 10.208.13.24:80\n";
        String lbName = "123456_123456";
        LogChopper.getLogLineValues(testBorked, value);
        Assert.assertEquals(lbName, value.getLoadbalancerName());
    }

}
