package org.openstack.atlas.adapter.itest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.Node;

import java.util.HashSet;
import java.util.Set;

import static org.openstack.atlas.service.domain.entities.NodeCondition.*;

public class SetNodesITest extends STMTestBase {
    String vsName;

    @BeforeClass
    public static void setupClass() throws InterruptedException {
        Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass() {

        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testNodeOperations() throws Exception {
        vsName = ZxtmNameBuilder.genVSName(lb);
        lb.setAlgorithm(LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS);
        stmAdapter.updateLoadBalancer(config, lb, lb);
        setupNodes();


    }

    private void setupNodes() throws Exception {
        final int defaultNodeWeight = 1;
        int node3Port = 81;
        int node4Port = 82;
        int node5Port = 83;
        int node3Weight = 15;
        int node4Weight = 20;
        int node5Weight = 1;
        String node3Ip = "127.0.0.3";
        String node4Ip = "127.0.0.4";
        String node5Ip = "127.0.0.5";
        Node node3 = new Node();
        Node node4 = new Node();
        Node node5 = new Node();
        node3.setIpAddress(node3Ip);
        node4.setIpAddress(node4Ip);
        node5.setIpAddress(node5Ip);
        node3.setPort(node3Port);
        node4.setPort(node4Port);
        node5.setPort(node5Port);
        node3.setCondition(ENABLED);
        node4.setCondition(DISABLED);
        node5.setCondition(DRAINING);
        node3.setWeight(node3Weight);
        node4.setWeight(node4Weight);
        node5.setWeight(node5Weight);
        Set<Node> createdNodes = new HashSet<Node>();
        createdNodes.add(node3);
        createdNodes.add(node4);
        createdNodes.add(node5);
        lb.setNodes(createdNodes);

        String node3StrRep = node3Ip + ":" + Integer.toString(node3Port);
        String node4StrRep = node4Ip + ":" + Integer.toString(node4Port);
        String node5StrRep = node5Ip + ":" + Integer.toString(node5Port);

        stmAdapter.setNodes(config, lb);
        int expectedDisabledNodes = 1;
        int expectedDrainingNodes = 1;
        int expectedEnabledNodes = 2;
        Set<String> setOfDisabledNodes = stmClient.getPool(vsName).getProperties().getBasic().getDisabled();
        Set<String> setOfDrainingNodes = stmClient.getPool(vsName).getProperties().getBasic().getDraining();
        Set<String> setOfEnabledNodes = stmClient.getPool(vsName).getProperties().getBasic().getNodes();
        Assert.assertEquals(expectedEnabledNodes, setOfEnabledNodes.size());
        Assert.assertEquals(expectedDisabledNodes, setOfDisabledNodes.size());
        Assert.assertEquals(expectedDrainingNodes, setOfDrainingNodes.size());
        Assert.assertTrue(setOfEnabledNodes.contains(node3StrRep));
        Assert.assertTrue(setOfDisabledNodes.contains(node4StrRep));
        Assert.assertTrue(setOfDrainingNodes.contains(node5StrRep));


    }


}
