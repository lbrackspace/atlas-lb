package org.openstack.atlas.util;

import org.openstack.atlas.logs.hadoop.util.LogFileNameBuilder;
import junit.framework.Assert;
import org.junit.Test;

public class LogFileNameBuilderTest {
    @Test
    public void testGetFormattedName() throws Exception {
        String name = "lb /name 10 2012 10 12 13:00.zip";
        String formattedName = LogFileNameBuilder.getFormattedName(name);
        Assert.assertEquals(formattedName, "lb__name_10_2012_10_12_13:00.zip");
    }

    @Test
    public void testGetFormattedFileDate() throws Exception {
        String fileDate = "2012021213";
        String formattedFileDate = LogFileNameBuilder.getFormattedFileDate(fileDate);
        Assert.assertEquals(formattedFileDate, "2012-02-12-13:00");
    }

    @Test
    public void testGetContainerName() throws Exception {
        String containerName = LogFileNameBuilder.getContainerName("10", "my /lb-name", "2012021213");
        Assert.assertEquals(containerName, "lb_10_my__lb-name_Feb_2012");
    }

    @Test
    public void testGetRemoteFileName() throws Exception {
        String containerName = LogFileNameBuilder.getRemoteFileName("10", "my /lb-name", "2012021213");
        Assert.assertEquals(containerName, "lb_10_my__lb-name_2012-02-12-13:00.zip");
    }
}
