package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.rackspace.stingray.client.protection.Protection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccessListITest extends STMTestBase {

    private String name;
    private String secureName;
    Set<AccessList> list;
    AccessList item1;
    AccessList item2;

    @Before
    public void setUp() {
        list = new HashSet<AccessList>();

        item1 = new AccessList();
        item1.setIpAddress("10.0.0.1");
        item1.setIpVersion(IpVersion.IPV4);
        item1.setUserName("doesntMatter");
        item1.setLoadbalancer(lb);
        item1.setType(AccessListType.ALLOW);
        list.add(item1);

        item2 = new AccessList();
        item2.setIpAddress("10.0.0.2");
        item2.setIpVersion(IpVersion.IPV4);
        item2.setUserName("stillDoesntMatter");
        item2.setLoadbalancer(lb);
        item2.setType(AccessListType.DENY);
        list.add(item2);

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            createSimpleLoadBalancer();
            name = ZxtmNameBuilder.genVSName(lb);
            secureName = ZxtmNameBuilder.genSslVSName(lb);
        } catch(Exception e) {
            Assert.fail(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    public void testAccessListCreationOnBasicLoadBalancer() throws Exception {
        createAccessList();
        verifyAccessList();
    }

    public void createAccessList() throws Exception {
        lb.setAccessLists(list);
        stmAdapter.updateAccessList(config, lb);
    }

    public void verifyAccessList() throws Exception {
        Protection protection = stmClient.getProtection(name);
        Assert.assertNotNull(protection);
        Set<String> allowed = protection.getProperties().getAccess_restriction().getAllowed();
        Set<String> banned = protection.getProperties().getAccess_restriction().getBanned();
        Assert.assertTrue(allowed.contains(item1.getIpAddress()));
        Assert.assertTrue(banned.contains(item2.getIpAddress()));
    }

    @AfterClass
    public static void tearDown() {
        removeSimpleLoadBalancer();
    }

    public static void removeSimpleLoadBalancer() {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            System.err.println("Failure to delete load balancer:\n" + Arrays.toString(e.getStackTrace()));
        }
    }
}