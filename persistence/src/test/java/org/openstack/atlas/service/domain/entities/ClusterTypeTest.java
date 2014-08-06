/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.service.domain.entities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClusterTypeTest {

    public ClusterTypeTest() {
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
    public void testValueOf() {
        ClusterType clusterType;
        assertEquals(ClusterType.valueOf("INTERNAL"), ClusterType.INTERNAL);
        assertEquals(ClusterType.valueOf("STANDARD"), ClusterType.STANDARD);
        assertEquals(ClusterType.valueOf("SMOKE"), ClusterType.SMOKE);

        try {
            // Should trigger Illegal Argument Exceptio
            clusterType = ClusterType.valueOf("NOT_A_CLUSTER_TYPE");
        } catch (Exception ex) {
            return;
        }
        fail("NOT_A_CLUSTER_TYPE should not have been in ClusterType");
    }
}
