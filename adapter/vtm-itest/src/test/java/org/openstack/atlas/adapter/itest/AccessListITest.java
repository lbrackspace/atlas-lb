package org.openstack.atlas.adapter.itest;

import org.junit.*;
import org.openstack.atlas.adapter.helpers.VTMNameBuilder;
import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.AccessListType;
import org.openstack.atlas.service.domain.entities.ConnectionLimit;
import org.openstack.atlas.service.domain.entities.SslTermination;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import org.rackspace.vtm.client.VTMRestClient;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.vtm.client.protection.Protection;

import java.util.*;

public class AccessListITest extends VTMTestBase {

    private String name;
    Set<AccessList> list;
    AccessList item1;
    AccessList item2;

    @BeforeClass
    public static void clientInit() {
        vtmClient = new VTMRestClient();
    }

    @Before
    public void setUp() {
        list = new HashSet<AccessList>();

        item1 = new AccessList();
        item1.setId(101);
        item1.setIpAddress("10.0.0.1");
        item1.setUserName("anonymous");
        item1.setLoadbalancer(lb);
        item1.setType(AccessListType.ALLOW);
        list.add(item1);

        item2 = new AccessList();
        item2.setId(102);
        item2.setIpAddress("10.0.0.2");
        item2.setUserName("stillAnonymous");
        item2.setLoadbalancer(lb);
        item2.setType(AccessListType.DENY);
        list.add(item2);

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
            setupIvars();
            createSimpleLoadBalancer();
            name = VTMNameBuilder.genVSName(lb);
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
            vtmAdapter.updateAccessList(config, lb);
        } catch (Exception e) {
            Assert.fail(String.format("Error updating Access List on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
        }
    }

    public void verifyAccessList() {
        Protection protection;
        try {
            protection = vtmClient.getProtection(name);
        } catch (Exception e) {
            Assert.fail(String.format("Error retrieving Protection on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
            return;
        }
        if (lb.getConnectionLimit() != null) {
            Assert.assertNotNull(protection);
        }
        Set<String> allowed = protection.getProperties().getAccessRestriction().getAllowed();
        Set<String> banned = protection.getProperties().getAccessRestriction().getBanned();
        Assert.assertTrue(allowed.contains(item1.getIpAddress()));
        Assert.assertTrue(banned.contains(item2.getIpAddress()));
    }

    public void verifyAccessListSingleDeletion() {
        Protection protection;
        try {
            protection = vtmClient.getProtection(name);
        } catch (Exception e) {
            Assert.fail(String.format("Error retrieving Protection on '%s'.\n%s", name,
                    Arrays.toString(e.getStackTrace())));
            return;
        }
        if (lb.getConnectionLimit() != null) {
            Assert.assertNotNull(protection);
        }
        Set<String> allowed = protection.getProperties().getAccessRestriction().getAllowed();
        Set<String> banned = protection.getProperties().getAccessRestriction().getBanned();
        Assert.assertTrue(allowed.contains(item1.getIpAddress()));
        Assert.assertTrue(banned.isEmpty());
    }

    public void verifyAccessListDeletion() {
        try {
            vtmClient.getProtection(name);
        } catch (VTMRestClientObjectNotFoundException notFoundException) {
            Assert.assertTrue("Protection object removed successfully", true);
        } catch (VTMRestClientException e) {
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
            vtmAdapter.updateConnectionThrottle(config, lb);
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
            vtmAdapter.updateSslTermination(config, lb, sslTermination);
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
            vtmAdapter.updateSslTermination(config, lb, sslTermination);
        } catch (Exception e) {
            Assert.fail(String.format("Error updating SSL termination on '%s'.", name));
        }
    }

    public SslTermination setupSsl(Boolean SslOnly) {
        SslTermination termination = new SslTermination();
        termination.setCertificate(VTMTestConstants.SSL_CERT);
        termination.setPrivatekey(VTMTestConstants.SSL_KEY);
        termination.setSecurePort(LB_SECURE_PORT);
        termination.setSecureTrafficOnly(SslOnly);
        return termination;
    }

    public void deleteAccessListItem() {
        List<Integer> deletionItems = new ArrayList<Integer>();
        deletionItems.add(item2.getId());
        try {
            vtmAdapter.deleteAccessList(config, lb, deletionItems);
        } catch(Exception e) {
            Assert.fail(String.format("Error deleting SSL termination on '%s'.", name));
        }
    }

    public void deleteAccessList() {
        List<Integer> deletionItems = new ArrayList<Integer>();
        deletionItems.add(item1.getId());
        deletionItems.add(item2.getId());
        try {
            vtmAdapter.deleteAccessList(config, lb, deletionItems);
        } catch(Exception e) {
            Assert.fail(String.format("Error deleting SSL termination on '%s'.", name));
        }
    }

    @After
    public void resetAfter() {
        removeLoadBalancer();
    }

    @AfterClass
    public static void tearDown() {
        teardownEverything();
    }
}