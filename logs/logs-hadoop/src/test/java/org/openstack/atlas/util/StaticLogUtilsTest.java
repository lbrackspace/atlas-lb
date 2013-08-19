package org.openstack.atlas.util;

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.logs.hadoop.util.StaticLogUtils;
import static org.junit.Assert.*;


public class StaticLogUtilsTest {

    public StaticLogUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    @Test
    public void testGetLoadBalancerId() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String loadBalancerId = StaticLogUtils.getLoadBalancerId(absoluteFileName);
        Assert.assertEquals(loadBalancerId, "10");
    }

    @Test
    public void testGetAccountId() throws Exception {
        String absoluteFileName = "/var/log/zxtm/hadoop/cache/2012021005/1/access_log_10_2012021005.zip";
        String accountId = StaticLogUtils.getAccountId(absoluteFileName);
        Assert.assertEquals(accountId, "1");
    }


}