package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class NodesResourceTest {

    public static class whenRetrievingNodeResource {

        NodeResource nodeResource;

        NodesResource nodesResource;

        @Before
        public void setUp() {
            nodeResource = new NodeResource();
            nodesResource = new NodesResource();
            nodesResource.setNodeResource(nodeResource);
        }

        @Test
        public void shouldReturnNodeResourceWithId() {
            nodeResource = nodesResource.getNodeResource(1);
            Assert.assertEquals(1, nodeResource.getId());

        }


    }



}
