package org.openstack.atlas.api.mgmt.resources;

import org.dozer.DozerBeanMapperBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.impl.NodeServiceImpl;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NodeResourceTest {

    static final String mappingFile = "loadbalancing-dozer-management-mapping.xml";

    public static class whenUpdatingNode {

        NodeResource nodeResource;

        @Mock
        private NodeServiceImpl nodeService;
        @Mock
        private LoadBalancerRepository loadBalancerRepository;
        @Mock
        private ManagementAsyncService asyncService;


        private LoadBalancer loadBalancer;

        private Node node;

        private org.openstack.atlas.docs.loadbalancers.api.management.v1.Node requestNode;


        @Before
        public void setUp() throws EntityNotFoundException, BadRequestException, UnprocessableEntityException, ImmutableEntityException {
            MockitoAnnotations.initMocks(this);
            nodeResource = new NodeResource();
            nodeResource.setNodeService(nodeService);
            nodeResource.setLoadBalancerRepository(loadBalancerRepository);
            nodeResource.setMockitoAuth(true);
            nodeResource.setId(1);
            nodeResource.setManagementAsyncService(asyncService);

            requestNode = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Node();
            requestNode.setWeight(60);
            requestNode.setType("SECONDARY");
            requestNode.setStatus("ONLINE");

            node = new Node();
            node.setType(NodeType.SECONDARY);
            node.setStatus(NodeStatus.ONLINE);
            node.setWeight(60);
            node.setId(1);
            loadBalancer = new LoadBalancer();
            loadBalancer.setId(1);
            loadBalancer.getNodes().add(node);
            loadBalancer.setStatus(LoadBalancerStatus.ACTIVE);
            loadBalancer.setName("test");
            node.setLoadbalancer(loadBalancer);
            nodeResource.setDozerMapper(DozerBeanMapperBuilder.create()
                    .withMappingFiles(mappingFile)
                    .build());

            when(nodeService.getNodeById(anyInt())).thenReturn(node);
            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(nodeService.updateNode(any(LoadBalancer.class), any(Node.class))).thenReturn(loadBalancer);


        }

        @Test
        public void shouldReturn202WithValidNode() throws EntityNotFoundException, BadRequestException, UnprocessableEntityException, ImmutableEntityException {

            Response response = nodeResource.updateNode(requestNode);
            Assert.assertEquals(202, response.getStatus());

        }

        @Test
        public void shouldReturn400WithInValidnode() throws EntityNotFoundException {
            requestNode = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Node();
            Response response = nodeResource.updateNode(requestNode);
            Assert.assertEquals(400, response.getStatus());

        }

        @Test
        public void shouldReturn400WhenNodeHasPort() throws EntityNotFoundException {
            requestNode.setPort(81);
            Response response = nodeResource.updateNode(requestNode);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrow404IfLBIsNotFound() throws EntityNotFoundException {
            when(nodeService.getNodeById(anyInt())).thenThrow(EntityNotFoundException.class);
            Response response = nodeResource.updateNode(requestNode);

        }

        @Test
        public void shouldThrow400IfNodeHasId() throws Exception {
            requestNode.setId(1);
            Response response = nodeResource.updateNode(requestNode);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldThrow400IfNodeHasPort() throws Exception {
            requestNode.setPort(60);
            Response response = nodeResource.updateNode(requestNode);
            Assert.assertEquals(400, response.getStatus());
        }

    }

    public static class whenDeletingANode{

        NodeResource nodeResource;

        @Mock
        private NodeServiceImpl nodeService;
        @Mock
        private LoadBalancerRepository loadBalancerRepository;
        @Mock
        private ManagementAsyncService asyncService;

        Node node;
        LoadBalancer loadBalancer;

        @Before
        public void setUp() throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {

            MockitoAnnotations.initMocks(this);

            nodeResource = new NodeResource();
            nodeResource.setMockitoAuth(true);
            nodeResource.setNodeService(nodeService);
            nodeResource.setLoadBalancerRepository(loadBalancerRepository);
            nodeResource.setManagementAsyncService(asyncService);
            node = new Node();
            node.setId(1);
            node.setWeight(1);
            node.setPort(80);
            node.setStatus(NodeStatus.ONLINE);
            node.setType(NodeType.SECONDARY);
            loadBalancer = new LoadBalancer();
            loadBalancer.setName("test");
            loadBalancer.setId(1);
            node.setLoadbalancer(loadBalancer);
            when(nodeService.getNodeById(anyInt())).thenReturn(node);
            when(loadBalancerRepository.getById(anyInt())).thenReturn(loadBalancer);
            when(nodeService.deleteNode(any(LoadBalancer.class), any(Node.class))).thenReturn(loadBalancer);

        }

        @Test
        public void shouldReturn202WhenDeletingNode() throws EntityNotFoundException {

            Response response = nodeResource.deleteNode();
            Assert.assertEquals(202, response.getStatus());
        }

        @Test
        public void shouldReturn400WhenNodeDoesNotHaveLBSetInDB() throws Exception {
            node.setLoadbalancer(null);
            Response response = nodeResource.deleteNode();
            Assert.assertEquals(400, response.getStatus());

        }

        @Test
        public void shouldReturn400WhenNoLBIsFound() throws EntityNotFoundException {
            when(loadBalancerRepository.getById(anyInt())).thenThrow(EntityNotFoundException.class);
            Response response = nodeResource.deleteNode();
            Assert.assertEquals(400, response.getStatus());

        }

        @Test
        public void shouldReturn400WhenLBandNodeDoNotMatch() throws EntityNotFoundException {
            when(nodeService.deleteNode(any(LoadBalancer.class), any(Node.class))).thenThrow(EntityNotFoundException.class);
            Response response = nodeResource.deleteNode();
            Assert.assertEquals(400, response.getStatus());

        }



    }



}
