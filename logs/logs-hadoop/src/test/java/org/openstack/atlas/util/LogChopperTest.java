package org.openstack.atlas.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.io.LbLogsWritable;
import org.openstack.atlas.util.LogChopper;

public class LogChopperTest extends TestCase {

    @Test
    public void testNonHTTPWithSSL() throws Exception {
        String testLineWithSSL = "495819_14490_S [27/Jan/2012:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";
        String testLineWithOutSSL = "495819_14490 [27/Jan/2012:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";

        LbLogsWritable logsWritableWithoutSSL = LogChopper.getLbLogStats(testLineWithSSL);
        Assert.assertEquals(testLineWithOutSSL, logsWritableWithoutSSL.getLogline());
    }

    @Test
    public void testNonHTTPWithoutSSL() throws Exception {
        String testLineWithOutSSL = "495819_14490 [27/Jan/2012:07:41:46 +0000] 46.35.159.208 50.56.4.164:443 50.56.120.213:443 905 5778 0.006673";
        LbLogsWritable logsWritableWithSSL = LogChopper.getLbLogStats(testLineWithOutSSL);
        Assert.assertEquals(testLineWithOutSSL, logsWritableWithSSL.getLogline());
    }

    @Test
    public void testHTTPWithSSL() throws Exception {
        String testLineWithSSL = "554867_15815_S toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\"";
        String testLineWithOutSSL = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\"";

        LbLogsWritable logsWritableWithoutSSL = LogChopper.getLbLogStats(testLineWithSSL);
        Assert.assertEquals(testLineWithOutSSL, logsWritableWithoutSSL.getLogline());
    }

    @Test
    public void testHTTPWithoutSSL() throws Exception {
        String testLineWithOutSSL = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\"";
        LbLogsWritable logsWritableWithSSL = LogChopper.getLbLogStats(testLineWithOutSSL);
        Assert.assertEquals(testLineWithOutSSL, logsWritableWithSSL.getLogline());
    }
}