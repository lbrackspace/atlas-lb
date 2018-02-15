package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.api.integration.AsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.service.domain.services.NodeService;

import javax.jms.JMSException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class NodeResourceTest {
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
    public static class WhenGettingLoadBalancerNode {

        @Mock
        AsyncService asyncService;
        @Mock
        NodeService nodeService;

        @Mock
        HttpHeaders requestHeaders;

        List<String> headers;

        @InjectMocks
        NodeResource nodeResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            nodeResource = new NodeResource();
            nodeResource.setAsyncService(asyncService);
            nodeResource.setNodeService(nodeService);
            nodeResource.setId(12);
            nodeResource.setAccountId(31337);
            nodeResource.setLoadBalancerId(32);
            nodeResource.setRequestHeaders(requestHeaders);
            nodeResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
            headers = new ArrayList<>();
            headers.add("APPLICATION_JSON");
            // Also test headers! ..
            when(requestHeaders.getRequestHeader(ArgumentMatchers.any())).thenReturn(headers);

        }

        @Test
        public void shouldReturn200WhenAsynIsNormal() throws Exception {
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(nodeResource.getAccountId(),
                    nodeResource.getLoadBalancerId(), nodeResource.getId())).thenReturn(
                            new org.openstack.atlas.service.domain.entities.Node());
            Response resp = nodeResource.retrieveNode(null);
            Assert.assertEquals(200, resp.getStatus());
        }

        @Test(expected = Exception.class)
        public void shouldReturn500OnServiceReturningNull() throws Exception {
            doThrow(Exception.class).when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(any(), any(), any()));
            Response resp = nodeResource.retrieveNode(null);
            Assert.assertEquals(500, resp.getStatus());
        }
    }

    public static class WhenDeletingALoadBalancerNode {

        @Mock
        AsyncService asyncService;
        @Mock
        NodeService nodeService;

        @InjectMocks
        NodeResource nodeResource;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            nodeResource = new NodeResource();
            nodeResource.setAsyncService(asyncService);
            nodeResource.setNodeService(nodeService);
            nodeResource.setId(12);
            nodeResource.setAccountId(31337);
            nodeResource.setLoadBalancerId(32);
            nodeResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturn500OnAsyncException() throws Exception {
            doThrow(JMSException.class).when(asyncService).callAsyncLoadBalancingOperation(
                    ArgumentMatchers.eq(Operation.DELETE_NODE), ArgumentMatchers.<LoadBalancer>any());
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(500, resp.getStatus());
        }

        @Test
        public void shouldReturn202WhenAsyncIsNormal() throws Exception {
            Response resp = nodeResource.deleteNode();
            Assert.assertEquals(202, resp.getStatus());
        }
    }

    public static class WhenUpdatingLoadBalancerNodes {

        @Mock
        AsyncService asyncService;
        @Mock
        NodeService nodeService;

        @InjectMocks
        NodeResource nodeResource;

        private Node gnode;
        private Node bnode;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            nodeResource = new NodeResource();
            nodeResource.setAsyncService(asyncService);
            nodeResource.setNodeService(nodeService);

            gnode = new Node();
            gnode.setAddress("10.6.60.173");
            gnode.setType(NodeType.PRIMARY);
            bnode = new Node();
            bnode.setId(32); // Can't set id shame shame
            nodeResource.setId(42);
            nodeResource.setLoadBalancerId(12);
            nodeResource.setAccountId(12345);
            nodeResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());
        }

        @Test
        public void shouldReturn202OnGoodNode() throws Exception {
            gnode.setAddress(null);
            gnode.setPort(null);
            gnode.setCondition(NodeCondition.DRAINING);            
            Response resp = nodeResource.updateNode(gnode);
            Assert.assertEquals((String) resp.getEntity(), 202, resp.getStatus());
        }

        @Test
        public void shouldReturn400OnInvalidNode() throws Exception {
            Response resp = nodeResource.updateNode(bnode);
            Assert.assertEquals(400, resp.getStatus());
        }

        // TODO: We need to update and get more tests going in general.
//        //Really need to get resource tests working... testing manually for now for node secondary update to primary when user not supplying type issue.
//        @Test
//        public void shouldReturnDBValueIfTypeNull() throws Exception {
//            //Test it when i look back into why resource tests are broken at some point
//        }
    }
}
