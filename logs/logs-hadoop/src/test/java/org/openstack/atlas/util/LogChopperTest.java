package org.openstack.atlas.util;

import org.openstack.atlas.logs.hadoop.util.LogChopper;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import org.openstack.atlas.io.LbLogsWritable;

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

    @Test
    public void testHTTPWithIP() throws Exception {
        String testLineWithIP = "406271_37533 50.56.48.198 46.137.248.139 - - [21/Nov/2012:03:51:45 +0000] \"GET /phpMyAdmin/translators.html HTTP/1.1\" 404 544 \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.01 [en]\" 10.1.1.1:80";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIP);
        Assert.assertEquals(testLineWithIP, logsWritableWithip.getLogline());
    }

    @Test
    public void testHTTPWithIPSimple() throws Exception {
        String testLineWithIP = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\" 10.1.1.1:8080";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIP);
        Assert.assertEquals(testLineWithIP, logsWritableWithip.getLogline());
    }

    @Test
    public void testHTTPWithIPV6() throws Exception {
        String testLineWithIv6P = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\" 2607:f0d0:1002:0051:0000:0000:0000:0004";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIv6P);
        Assert.assertEquals(testLineWithIv6P, logsWritableWithip.getLogline());
    }

    @Test
    public void testHTTPWithIPV6Compression() throws Exception {
        String testLineWithIv6P = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\" 2607:f0d0:1002:51::4";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIv6P);
        Assert.assertEquals(testLineWithIv6P, logsWritableWithip.getLogline());
    }

    @Test
    public void testHTTPWithIPV6Brackets() throws Exception {
        String testLineWithIv6P = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\" [2607:f0d0:1002:0051:0000:0000:0000:0004]:80";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIv6P);
        Assert.assertEquals(testLineWithIv6P, logsWritableWithip.getLogline());
    }

    @Test
    public void testHTTPWithIPV6CompressionBrackets() throws Exception {
        String testLineWithIv6P = "554867_15815 toolbar.lavasoft.com 213.184.198.10 - - [27/Jan/2012:07:41:37 +0000] \"GET /malwaresitelist/data/120126162223-l.zip HTTP/1.0\" 200 1136 \"-\" \"SimpleGet\" [2607:f0d0:1002:51::4]:9090";
        LbLogsWritable logsWritableWithip = LogChopper.getLbLogStats(testLineWithIv6P);
        Assert.assertEquals(testLineWithIv6P, logsWritableWithip.getLogline());
    }
}