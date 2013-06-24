package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class STMRestAdapterITest extends STMTestBase {

    @Before
    public void setUp() {
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {
        //clean up...
    }

    @Test
    public void updateVirtualServer() {
        System.out.print(lb);
    }
}
