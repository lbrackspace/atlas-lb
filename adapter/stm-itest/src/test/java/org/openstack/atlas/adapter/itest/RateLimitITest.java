package org.openstack.atlas.adapter.itest;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RateLimitITest extends STMTestBase {

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass()
    {

    }

    @Test
    public void testSimpleRateLimitOperations() {
        setRateLimit();
        updateRateLimit();
        getRateLimit();
        deleteRateLimit();
    }

    private void setRateLimit() {

    }

    private void updateRateLimit() {

    }

    private void getRateLimit() {

    }

    private void deleteRateLimit() {

    }











}
