package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
@Ignore
public class NodeResourceTest {

    public static class WhenGettingLoadBalancerNode {

        private AsyncService esbService;
        private NodeResource nodeResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            nodeResource = new NodeResource();
            esbService = mock(AsyncService.class);
            nodeResource.setAsyncService(esbService);
            nodeResource.setId(12);
            nodeResource.setAccountId(31337);
            nodeResource.setLoadBalancerId(32);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);            
            Response resp = nodeResource.retrieveNode(null);
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn200WhenEsbIsNormal() throws Exception {            
            Response resp = nodeResource.retrieveNode(null);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {            
            Response resp = nodeResource.retrieveNode(null);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenDeletingALoadBalancerNode {

        private AsyncService esbService;
        private NodeResource nodeResource;
        private OperationResponse operationResponse;

        @Before
        public void setUp() {
            nodeResource = new NodeResource();
            esbService = mock(AsyncService.class);
            nodeResource.setAsyncService(esbService);
            nodeResource.setId(12);
            nodeResource.setAccountId(31337);
            nodeResource.setLoadBalancerId(32);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            nodeResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturn500OnEsbException() throws Exception {            
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500OnEsbReturningNull() throws Exception {            
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);            
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn202WhenEsbIsNormal() throws Exception {            
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(202, resp.getStatus());
        }
    }

    public static class WhenUpdatingLoadBalancerNodes {

        private AsyncService esbService;
        private NodeResource nodeResource;
        private OperationResponse operationResponse;
        private Node gnode;
        private Node bnode;

        @Before
        public void setUp() {
            nodeResource = new NodeResource();
            esbService = mock(AsyncService.class);
            nodeResource.setAsyncService(esbService);
            gnode = new Node();
            gnode.setAddress("10.6.60.173");
            bnode = new Node();
            bnode.setId(32); // Can't set id shame shame
            nodeResource.setLoadBalancerId(12);
            nodeResource.setAccountId(12345);
            operationResponse = new OperationResponse();
            operationResponse.setExecutedOkay(true);
            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            nodeResource.setDozerMapper(new DozerBeanMapper(mappingFiles));
        }

        @Test
        public void shouldReturn202OnGoodNode() throws Exception {
            operationResponse.setExecutedOkay(true);
            gnode.setAddress(null);
            gnode.setPort(null);
            gnode.setCondition(NodeCondition.DRAINING);            
            Response resp = nodeResource.updateNode(gnode);
            Assert.assertEquals((String) resp.getEntity(), 202, resp.getStatus());
        }

        @Test
        public void shouldReturn400OnInvalidNode() throws Exception {
            operationResponse.setExecutedOkay(true);            
            Response resp = nodeResource.updateNode(bnode);
            Assert.assertEquals(400, resp.getStatus());
        }

        @Test
        public void shouldReturn500WhenisExecutedOkayisFalse() throws Exception {
            operationResponse.setExecutedOkay(false);            
            gnode.setAddress(null);
            gnode.setCondition(NodeCondition.ENABLED);
            Response resp = nodeResource.updateNode(gnode);
            Assert.assertEquals(500, resp.getStatus());
        }

        //Really need to get resource tests working... testing manually for now for node secondary update to primary when user not supplying type issue.
        @Test
        public void shouldReturnDBValueIfTypeNull() throws Exception {
            //Test it when i look back into why resource tests are broken at some point
        }
    }
}
