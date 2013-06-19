package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.ZxtmRollBackException;
import org.openstack.atlas.adapter.helpers.ResourceTranslator;
import org.openstack.atlas.adapter.helpers.ReverseResourceTranslator;
import org.openstack.atlas.adapter.stm.StmAdapterImpl;
import org.openstack.atlas.service.domain.entities.*;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.AccessListType.ALLOW;
import static org.openstack.atlas.service.domain.entities.AccessListType.DENY;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.HTTP_COOKIE;

public class FullConfigIntegrationTest {
    //TODO: this is not proper... quick tests...

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        //STUFF
    }

    @AfterClass
    public static void tearDownClass() {
        //RAWR
    }

    @Test
    public void createFullyConfiguredLoadBalancer() throws ZxtmRollBackException, InsufficientRequestException, RemoteException {
        LoadBalancer lb = new LoadBalancer();
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
        lb.setSessionPersistence(HTTP_COOKIE);
        lb.setTimeout(99);
        lb.setId(98765);
        lb.setAccountId(56789);

        Set<Node> nodes = new HashSet<Node>();
        Node node = new Node();
        node.setIpAddress("10.1.49.5");
        nodes.add(node);
        lb.setNodes(nodes);

        HealthMonitor monitor = new HealthMonitor();
        monitor.setType(HealthMonitorType.CONNECT);
        monitor.setDelay(10);
        monitor.setTimeout(20);
        monitor.setAttemptsBeforeDeactivation(3);
        lb.setHealthMonitor(monitor);

        ConnectionLimit limit = new ConnectionLimit();
        limit.setMaxConnections(50);
        limit.setRateInterval(10);
        limit.setMaxConnectionRate(10);
        limit.setMinConnections(1);
        lb.setConnectionLimit(limit);

        lb.setConnectionLogging(true);

        Set<AccessList> networkItems = new HashSet<AccessList>();
        AccessList item1 = new AccessList();
        AccessList item2 = new AccessList();
        item1.setIpAddress("0.0.0.0/0");
        item2.setIpAddress("127.0.0.1");
        item1.setType(DENY);
        item2.setType(ALLOW);
        networkItems.add(item1);
        networkItems.add(item2);

        lb.setAccessLists(networkItems);

        try {
            StmAdapterImpl adapter = new StmAdapterImpl();
            adapter.createLoadBalancer(null, lb);
//            removeLoadBalancer();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void setErrorFileTest() throws RemoteException {
        StmAdapterImpl adapter = new StmAdapterImpl();

        //adapter.setErrorFile(null,"386085_324", "hrodjger");
        LoadBalancer lb = null;
        UserPages up = new UserPages();
        try {
            ResourceTranslator rt = new ResourceTranslator();
            lb = ReverseResourceTranslator.getLoadBalancer(362,406271);
            //up.setErrorpage(lb.getName());
            //lb.setUserPages(up);
            //adapter.setErrorFile(null, lb, "some error text");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
