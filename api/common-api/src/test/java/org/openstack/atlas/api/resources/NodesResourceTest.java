package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Ignore;
import org.mockito.ArgumentMatchers;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.events.entities.NodeServiceEvent;
import org.openstack.atlas.service.domain.events.repository.LoadBalancerEventRepository;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NodeService;
import org.openstack.atlas.api.integration.AsyncService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
    static final String mappingFile = "loadbalancing-dozer-mapping.xml";
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

            nodesResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

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
            doThrow(EntityNotFoundException.class).when(nodeService).getNodesByAccountIdLoadBalancerId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
            Response response = nodesResource.retrieveNodes(0, 4, 3, null);
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldProduce410WhenDeletedStatusExceptionThrown() throws Exception {
            doThrow(DeletedStatusException.class).when(nodeService).getNodesByAccountIdLoadBalancerId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
            Response response = nodesResource.retrieveNodes(0, 4, 3, null);
            Assert.assertEquals(410, response.getStatus());
        }
    }

    @RunWith(PowerMockRunner.class)
    @PrepareForTest(RestApiConfiguration.class)
    @PowerMockIgnore("javax.management.*")
    public static class WhenGettingNodeEvents {

        private HttpHeaders requestHeaders;
        private AsyncService asyncService;
        private NodesResource nodesResource;
        private NodeService nodeService;
        private LoadBalancerEventRepository loadBalancerEventRepository;

        @Before
        public void setUp() throws Exception {
            requestHeaders = mock(HttpHeaders.class);
            asyncService = mock(AsyncService.class);
            nodeService = mock(NodeService.class);
            loadBalancerEventRepository = mock(LoadBalancerEventRepository.class);

            nodesResource = new NodesResource();
            nodesResource.setRequestHeaders(requestHeaders);
            nodesResource.setAsyncService(asyncService);
            nodesResource.setNodeService(nodeService);
            nodesResource.setLoadBalancerEventRepository(loadBalancerEventRepository);

            nodesResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            List<String> acceptHeaders = new ArrayList<String>();
            acceptHeaders.add(APPLICATION_XML);
            when(requestHeaders.getRequestHeader("Accept")).thenReturn(acceptHeaders);
            nodesResource.setLoadBalancerId(12);
            nodesResource.setAccountId(12345);

        }

        @Test
        public void shouldProduce200WhenSuccessful() throws Exception {
            RestApiConfiguration restApiConfiguration = PowerMockito.mock(RestApiConfiguration.class);
            PowerMockito.doReturn("mockeduri").when(restApiConfiguration, "getString", anyString());
            PaginationHelper paginationHelper = new PaginationHelper();
            paginationHelper.setRestApiConfiguration(restApiConfiguration);

            List<NodeServiceEvent> dEvents = new ArrayList<>();
            NodeServiceEvent rEvent = new NodeServiceEvent();
            rEvent.setDetailedMessage("dmsg");
            dEvents.add(rEvent);

            // no links
            when(loadBalancerEventRepository.getNodeServiceEvents(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(dEvents);
            Response response = nodesResource.retrieveNodeEvents(0, 4, 0, 0);
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(1, ((org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents) response.getEntity()).getNodeServiceEvents().size());
            Assert.assertTrue(((org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents) response.getEntity()).getLinks().isEmpty());

            // links because limit is less than events
            rEvent = new NodeServiceEvent();
            rEvent.setDetailedMessage("dmsg1");
            dEvents.add(rEvent);
            rEvent = new NodeServiceEvent();
            rEvent.setDetailedMessage("dmsg2");
            dEvents.add(rEvent);
            when(loadBalancerEventRepository.getNodeServiceEvents(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(dEvents);
            response = nodesResource.retrieveNodeEvents(0, 1, 0, 0);
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(2, ((org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents) response.getEntity()).getNodeServiceEvents().size());
            Assert.assertFalse(((org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents) response.getEntity()).getLinks().isEmpty());
            Assert.assertTrue(((NodeServiceEvents) response.getEntity()).getLinks().get(0).getRel().equals("next"));
            Assert.assertTrue(((NodeServiceEvents) response.getEntity()).getLinks().get(0).getHref().contains("nodes/events?offset=1&limit=1"));
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

            nodesResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

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

            doThrow(EntityNotFoundException.class).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(404, response.getStatus());
        }

        @Test
        public void shouldProduce422WhenImmutableEntityExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(ImmutableEntityException.class).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldProduce422WhenUnprocessableEntityExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(UnprocessableEntityException.class).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(422, response.getStatus());
        }

        @Test
        public void shouldProduce400WhenBadRequestExceptionThrown() throws Exception {
            LoadBalancer returnLb = new LoadBalancer();
            returnLb.setNodes(new HashSet<org.openstack.atlas.service.domain.entities.Node>());

            doThrow(BadRequestException.class).when(nodeService).createNodes(Matchers.<LoadBalancer>any());
            Response response = nodesResource.createNodes(nodes);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldProduce202WhenPassingInAnEmptyNodesObject() {
            Response response = nodesResource.createNodes(new Nodes());
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldProduce400WhenPassingInAnInvalidNodeIpAddress() throws Exception {
            Node node1 = new Node();
            node1.setAddress("192.168.1.3333333");
            node1.setPort(80);
            node1.setCondition(NodeCondition.ENABLED);
            Nodes nodes = new Nodes();
            nodes.getNodes().add(node1);

            Response response = nodesResource.createNodes(nodes);
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
            verify(nodeResource).setAccountId(ArgumentMatchers.<Integer>any());
            verify(nodeResource).setLoadBalancerId(ArgumentMatchers.<Integer>any());
        }
    }
}
