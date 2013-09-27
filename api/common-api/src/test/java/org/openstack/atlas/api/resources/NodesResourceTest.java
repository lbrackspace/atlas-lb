package org.openstack.atlas.api.resources;

import org.junit.Ignore;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.api.integration.AsyncService;
import junit.framework.Assert;
import org.dozer.DozerBeanMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NodesResourceTest {

    public static class WhenGettingLoadBalancerNodes {

        private HttpHeaders requestHeaders;
        private AsyncService asyncService;
        private NodesResource nodesResource;
        private NodeService nodeService;

        @Before
        public void setUp() throws EntityNotFoundException, DeletedStatusException {
            requestHeaders = mock(HttpHeaders.class);
            asyncService = mock(AsyncService.class);
            nodeService = mock(NodeService.class);
            nodesResource = new NodesResource();
            nodesResource.setRequestHeaders(requestHeaders);
            nodesResource.setAsyncService(asyncService);
            nodesResource.setNodeService(nodeService);

            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            nodesResource.setDozerMapper(new DozerBeanMapper(mappingFiles));

            List<String> acceptHeaders = new ArrayList<String>();
            acceptHeaders.add(APPLICATION_XML);
            when(requestHeaders.getRequestHeader("Accept")).thenReturn(acceptHeaders);
            nodesResource.setLoadBalancerId(12);
            nodesResource.setAccountId(12345);
        }

        @Test
        public void shouldProduce200WhenSuccessful() throws Exception {
            Set<org.openstack.atlas.service.domain.entities.Node> domainNodes = new HashSet<org.openstack.atlas.service.domain.entities.Node>();
            final org.openstack.atlas.service.domain.entities.Node node = new org.openstack.atlas.service.domain.entities.Node();
            node.setStatus(NodeStatus.OFFLINE);
            domainNodes.add(node);

            when(nodeService.getNodesByAccountIdLoadBalancerId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(domainNodes);
            Response response = nodesResource.retrieveNodes(0, 4, 3, null);
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(1, ((Nodes) response.getEntity()).getNodes().size());
        }

        @Test
        public void shouldProduce404WhenEntityNotFoundExceptionThrown() throws Exception {
            doThrow(new EntityNotFoundException("Exception")).when(nodeService).getNodesByAccountIdLoadBalancerId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
            Response response = nodesResource.retrieveNodes(0, 4, 3, null);
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldProduce410WhenDeletedStatusExceptionThrown() throws Exception {
            doThrow(new DeletedStatusException("Exception")).when(nodeService).getNodesByAccountIdLoadBalancerId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
            Response response = nodesResource.retrieveNodes(0, 4, 3, null);
            Assert.assertEquals(410, response.getStatus());
        }
    }

    public static class WhenCreatingNodes {

        private NodesResource nodesResource;
        private AsyncService asyncService;
        private LoadBalancerService loadBalancerService;
        private NodeService nodeService;
        private Nodes nodes;
        private Node node1;
        private Node node2;
        private Set<org.openstack.atlas.service.domain.entities.Node> nodeSet;

        @Before
        public void setUp() throws EntityNotFoundException {
            nodesResource = new NodesResource();
            asyncService = mock(AsyncService.class);
            loadBalancerService = mock(LoadBalancerService.class);
            nodeService = mock(NodeService.class);
            nodesResource.setLoadBalancerService(loadBalancerService);
            nodesResource.setAsyncService(asyncService);
            nodesResource.setNodeService(nodeService);

            List<String> mappingFiles = new ArrayList<String>();
            mappingFiles.add("loadbalancing-dozer-mapping.xml");
            nodesResource.setDozerMapper(new DozerBeanMapper(mappingFiles));

            when(loadBalancerService.get(anyInt())).thenReturn(null);
        }

        @Before
        public void setUpValidNodesObject() {
            node1 = new Node();
            node2 = new Node();
            nodeSet = new HashSet<org.openstack.atlas.service.domain.entities.Node>();
            node1.setAddress("10.1.1.1");
            node2.setAddress("10.1.1.2");
            node1.setPort(80);
            node2.setPort(80);
            node1.setCondition(NodeCondition.ENABLED);
            node2.setCondition(NodeCondition.ENABLED);

            nodes = new Nodes();
            nodes.getNodes().add(node1);
            nodes.getNodes().add(node2);
        }

        @Test
        public void shouldProduce202WhenSuccessful() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            when(nodeService.createNodes(Matchers.<LoadBalancer>any())).thenReturn(nodeSet);
            doNothing().when(asyncService).callAsyncLoadBalancingOperation(Matchers.eq(Operation.CREATE_NODES), Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduce404WhenEntityNotFoundExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(new EntityNotFoundException("Exception")).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldProduce422WhenImmutableEntityExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(new ImmutableEntityException("Exception")).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldProduce422WhenUnprocessableEntityExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(new UnprocessableEntityException("Exception")).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldProduce400WhenBadRequestExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(new BadRequestException("Exception")).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce202WhenPassingInAnEmptyNodesObject() {
            Response response = nodesResource.createNodes(new Nodes());
            Assert.assertEquals(202, response.getStatus());
        }

        @Ignore("This test is not doing what it claims it is...")
        @Test
        public void shouldProduce400WhenPassingInAnInvalidNodeIpAddress() throws Exception {
            Set<Node> nodes1 = new HashSet<Node>();
            Node node1 = new Node();
            node1.setAddress("192.168.1.3");
            node1.setPort(80);
            node1.setCondition(NodeCondition.ENABLED);
            nodes1.add(node1);

            Response response = nodesResource.createNodes(new Nodes());
            Assert.assertEquals(400, response.getStatus());
        }
    }

    public static class WhenRetrievingResources {
        private NodesResource nodesResource;

        @Before
        public void setUp() {
            nodesResource = new NodesResource();
        }

        @Test
        public void shouldSetNodeIdForNodeResource() {
            NodeResource nodeResource = mock(NodeResource.class);
            this.nodesResource.setNodeResource(nodeResource);
            this.nodesResource.retrieveNodeResource(anyInt());
            verify(nodeResource).setId(anyInt());
            verify(nodeResource).setAccountId(anyInt());
            verify(nodeResource).setLoadBalancerId(anyInt());
        }
    }
}
