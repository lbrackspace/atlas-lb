package org.openstack.atlas.api.mgmt.helpers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;

import static org.junit.Assert.assertTrue;
import org.junit.Assert;

public class StubFactoryTest {

    public StubFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testrndInt() {
        int ri = StubFactory.rndInt(10, 20);
        assertTrue("out of range found", (ri <= 20 && ri >= 10));
    }

    @Test
    public void testrndDouble() {
        double rd = StubFactory.rndDouble(10.0, 20.0);
        assertTrue("Out of range double found", (rd <= 20.0 && rd >= 10.0));
    }

    @Test
    public void testgetClustersDetails() {
        Object obj = StubFactory.getClustersDetails();
        assertTrue("Expected Clusters object", obj instanceof Clusters);

    }

    @Test
    public void testGetClusterVirtualIpsDetails() {
        Object obj = StubFactory.getVirtualIpsDetails();
        assertTrue("Expected VirtualIps", obj instanceof VirtualIps);
    }

    @Test
    public void testgetLoadBalancerSuspsensionDetails() {
        Object obj = StubFactory.getLoadBalancerSuspensionDetails("test", 55, "SomeUser");
        assertTrue("Expected Suspension", obj instanceof Suspension);
    }

    @Test
    public void testrndBackup() {
        Object obj = StubFactory.rndBackup();
        assertTrue("Expected Backup", obj instanceof Backup);
    }

    @Test
    public void testrndBackups() {
        Object obj = StubFactory.rndBackups();
        assertTrue("Expected Backup", obj instanceof Backups);
    }

    @Ignore
    @Test
    public void testrndCustomer() {
//        Object obj = StubFactory.rndCustomer();
//        assertTrue("Expected Customeer", obj instanceof Customer);
    }

    @Ignore
    @Test
    public void testrndCustomerList() {
//        Object obj = StubFactory.rndCustomerList(5);
//        assertTrue("Expected CustomerList", obj instanceof CustomerList);
    }

    @Test
    public void testrndHostCapacityReport() {
        Object obj = StubFactory.rndHostCapacityReport();
        assertTrue("Expected HostCapacityReport", obj instanceof HostCapacityReport);
    }

    @Test
    public void testrndHostCapacityReports() {
        Object obj = StubFactory.rndHostCapacityReports(5);
        assertTrue("Expected HostCapacityReports", obj instanceof HostCapacityReports);
    }

    @Test
    public void testrndHostPut() {
        Object obj = StubFactory.rndHostPut();
        assertTrue("Expected HostCapacityReports", obj instanceof Host);
    }

    @Test
    public void testrndHostPost() {
        Object obj = StubFactory.rndHostPost();
        assertTrue("Expected HostCapacityReports", obj instanceof Host);
    }

    @Test
    public void testrndConnectionThrottle() {
        Object obj = StubFactory.rndConnectionThrottle();
        assertTrue("Expected ConnectionThrottle",obj instanceof ConnectionThrottle);
    }

    @Test
    public void testrndCurrentUsage() {
        Object obj = StubFactory.rndCurrentUsage();
        assertTrue("Excepted Usage",obj instanceof LoadBalancerUsageRecord);
    }

    @Test
    public void testrndHosts() {
        Object obj = StubFactory.rndHosts(5);
        assertTrue("Expected Hosts",obj instanceof Hosts);
    }

    @Test
    public void testrndLoadBalancer() {
        Object obj = StubFactory.rndLoadBalancer();
        assertTrue("Expected LoadBalancer",obj instanceof LoadBalancer);
    }

    @Test
    public void testrndNode() {
        Object obj = StubFactory.rndNode();
        assertTrue("Expected Node",obj instanceof Node);
    }

    @Test
    public void testrndNodes() {
        Object obj = StubFactory.rndNodes(5);
        assertTrue("Expected Nodes",obj instanceof Nodes);
    }

    @Test
    public void testrndRateLimit() {
        Object obj = StubFactory.rndRateLimit();
        assertTrue("Expected RateLimit",obj instanceof RateLimit);
    }

    @Test
   public void testSessionPersistence() {
        Object obj = StubFactory.rndSessionPersistance();
        assertTrue("Expected SessionPersistence",obj instanceof SessionPersistence);
    }

    @Test
    public void testNullIntegerStringFormatterShouldentThrowException(){
        Integer val = null;
        // Null val should not break a String formatter.

        String test;
        test = String.format("%d",val);
        Assert.assertEquals("null",test);
        val = 500;
        test = String.format("%d",val);
        Assert.assertEquals("500", test);
        Assert.assertFalse(test.equals("300"));
    }
}
