package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.IpVersion;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.protection.Protection;

import java.util.*;

public class AccessListITest extends STMTestBase {

    private String name;
    Set<AccessList> list;
    AccessList item1;
    AccessList item2;

    @Before
    public void setUp() {
        list = new HashSet<AccessList>();

        item1 = new AccessList();
        item1.setIpAddress("10.0.0.1");
        item1.setIpVersion(IpVersion.IPV4);
        item1.setUserName("anonymous");
        item1.setLoadbalancer(lb);
        item1.setType(AccessListType.ALLOW);
        list.add(item1);

        item2 = new AccessList();
        item2.setIpAddress("10.0.0.2");
        item2.setIpVersion(IpVersion.IPV4);
        item2.setUserName("stillAnonymous");
        item2.setLoadbalancer(lb);
        item2.setType(AccessListType.DENY);
        list.add(item2);

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            createSimpleLoadBalancer();
            name = ZxtmNameBuilder.genVSName(lb);
        } catch(Exception e) {
            Assert.fail(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    @Test
    public void testAccessListCreationOnBasicLoadBalancer() {
        createAccessList();
        verifyAccessList();
    }

    @Test
    public void testAccessListCreationWithConnectionThrottleEnabled() {
        enableConnectionThrottle();
        createAccessList();
        verifyAccessList();
    }

    @Test
    public void testAccessListCreationOnSslLoadBalancer() {
        enableSsl();
        createAccessList();
        verifyAccessList();
    }

    @Test
    public void testAccessListCreationOnSslOnlyLoadBalancer() {
        enableSslOnly();
        createAccessList();
        verifyAccessList();
    }

    @Test
    public void testAccessListRemovalOfOneItem() {
        createAccessList();
        verifyAccessList();
        deleteAccessListItem();
        verifyAccessListSingleDeletion();
    }

    @Test
    public void testAccessListDeletion() {
        createAccessList();
        verifyAccessList();
        deleteAccessList();
        verifyAccessListDeletion();
    }

    public void createAccessList() {
        lb.setAccessLists(list);
        try {
            stmAdapter.updateAccessList(config, lb);
        } catch (Exception e) {
            Assert.fail(String.format("Error updating Access List on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
        }
    }

    public void verifyAccessList() {
        Protection protection;
        try {
            protection = stmClient.getProtection(name);
        } catch (Exception e) {
            Assert.fail(String.format("Error retrieving Protection on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
            return;
        }
        if (lb.getConnectionLimit() != null) {
            Assert.assertNotNull(protection);
        }
        Set<String> allowed = protection.getProperties().getAccess_restriction().getAllowed();
        Set<String> banned = protection.getProperties().getAccess_restriction().getBanned();
        Assert.assertTrue(allowed.contains(item1.getIpAddress()));
        Assert.assertTrue(banned.contains(item2.getIpAddress()));
    }

    public void verifyAccessListSingleDeletion() {
        Protection protection;
        try {
            protection = stmClient.getProtection(name);
        } catch (Exception e) {
            Assert.fail(String.format("Error retrieving Protection on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
            return;
        }
        if (lb.getConnectionLimit() != null) {
            Assert.assertNotNull(protection);
        }
        Set<String> allowed = protection.getProperties().getAccess_restriction().getAllowed();
        Set<String> banned = protection.getProperties().getAccess_restriction().getBanned();
        Assert.assertTrue(allowed.contains(item1.getIpAddress()));
        Assert.assertTrue(banned.isEmpty());
    }

    public void verifyAccessListDeletion() {
        try {
            stmClient.getProtection(name);
        } catch (StingrayRestClientObjectNotFoundException notFoundException) {
            Assert.assertTrue("Protection object removed successfully", true);
        } catch (StingrayRestClientException e) {
            Assert.fail(String.format("Error retrieving Protection on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
            return;
        }
    }

    public void enableConnectionThrottle() {
        ConnectionLimit limit = new ConnectionLimit();
        limit.setMaxConnectionRate(MAX_CONECT_RATE);
        limit.setMaxConnections(MAX_CONNECTIONS);
        limit.setMinConnections(MIN_CONNECTIONS);
        limit.setRateInterval(RATE_INTERVAL);
        lb.setConnectionLimit(limit);
        try {
            stmAdapter.updateConnectionThrottle(config, lb);
        } catch(Exception e) {
            Assert.fail(String.format("Error updating Connection Throttle on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
        }
    }

    public void enableSsl() {
        ZeusSslTermination sslTermination = new ZeusSslTermination();
        SslTermination termination = setupSsl(false);
        sslTermination.setSslTermination(termination);
        lb.setSslTermination(termination);
        try {
            stmAdapter.updateSslTermination(config, lb, sslTermination);
        } catch (Exception e) {
            Assert.fail(String.format("Error updating SSL termination on '%s'.", name));
        }
    }

    public void enableSslOnly() {
        ZeusSslTermination sslTermination = new ZeusSslTermination();
        SslTermination termination = setupSsl(true);
        sslTermination.setSslTermination(termination);
        lb.setSslTermination(termination);
        try {
            stmAdapter.updateSslTermination(config, lb, sslTermination);
        } catch (Exception e) {
            Assert.fail(String.format("Error updating SSL termination on '%s'.", name));
        }
    }

    public SslTermination setupSsl(Boolean SslOnly) {
        SslTermination termination = new SslTermination();
        termination.setCertificate(StmTestConstants.SSL_CERT);
        termination.setPrivatekey(StmTestConstants.SSL_KEY);
        termination.setSecurePort(LB_SECURE_PORT);
        termination.setSecureTrafficOnly(SslOnly);
        return termination;
    }

    public void deleteAccessListItem() {
        List<Integer> deletionItems = new ArrayList<Integer>();
        deletionItems.add(item2.getId());
        try {
            stmAdapter.deleteAccessList(config, lb, deletionItems);
        } catch(Exception e) {
            Assert.fail(String.format("Error deleting SSL termination on '%s'.", name));
        }
    }

    public void deleteAccessList() {
        List<Integer> deletionItems = new ArrayList<Integer>();
        deletionItems.add(item1.getId());
        deletionItems.add(item2.getId());
        try {
            stmAdapter.deleteAccessList(config, lb, deletionItems);
        } catch(Exception e) {
            Assert.fail(String.format("Error deleting SSL termination on '%s'.", name));
        }
    }

    @After
    public void tearDown() {
        teardownEverything();
    }
}