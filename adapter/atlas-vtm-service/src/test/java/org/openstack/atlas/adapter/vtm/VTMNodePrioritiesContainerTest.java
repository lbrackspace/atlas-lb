package org.openstack.atlas.adapter.vtm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.helpers.VTMNodePriorityContainer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;

import java.util.*;

public class VTMNodePrioritiesContainerTest {
    Collection<Node> nodes;
    Node n1;
    Node n2;

    @Before
    public void setUp() {
        List<Node> nlist = new ArrayList<Node>();
        n1 = new Node();
        n2 = new Node();
        n1.setIpAddress("10.1.1.1");
        n2.setIpAddress("10.1.1.2");
        n1.setPort(80);
        n2.setPort(80);
        n1.setType(NodeType.PRIMARY);
        n2.setType(NodeType.SECONDARY);
        nlist.add(n1);
        nlist.add(n2);
        nodes = nlist;

    }

    @Test
    public void shouldCreateMapForZNPC() throws InsufficientRequestException {
        VTMNodePriorityContainer znpc = new VTMNodePriorityContainer(nodes);
        Assert.assertNotNull(znpc);
        Assert.assertNotNull(znpc.getPriorityValuesMap());
    }

    @Test
    public void shouldCreateSetForZNPC() throws InsufficientRequestException {
        VTMNodePriorityContainer znpc = new VTMNodePriorityContainer(nodes);
        Assert.assertNotNull(znpc);
        Assert.assertNotNull(znpc.getPriorityValuesSet());
    }

    @Test
    public void shouldCreateValidMapOfNodes() throws InsufficientRequestException {
        VTMNodePriorityContainer znpc = new VTMNodePriorityContainer(nodes);
        Assert.assertNotNull(znpc);
        Assert.assertNotNull(znpc.getPriorityValuesMap());

        Assert.assertEquals(true, znpc.hasSecondary());
        Assert.assertEquals(2, (long)znpc.getPriorityValuesMap().get("10.1.1.1:80"));
        Assert.assertEquals(1, (long)znpc.getPriorityValuesMap().get("10.1.1.2:80"));
    }

    @Test
    public void shouldCreateValidSetOfNodes() throws InsufficientRequestException {
        VTMNodePriorityContainer znpc = new VTMNodePriorityContainer(nodes);
        Assert.assertNotNull(znpc);
        Assert.assertNotNull(znpc.getPriorityValuesSet());

        Assert.assertEquals(true, znpc.hasSecondary());
        Assert.assertTrue(znpc.getPriorityValuesSet().contains("10.1.1.1:80:2"));
        Assert.assertTrue(znpc.getPriorityValuesSet().contains("10.1.1.2:80:1"));
    }
}
