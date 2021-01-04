package org.openstack.atlas.api.resources;

import net.spy.memcached.MemcachedClient;
import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openstack.atlas.api.integration.AsyncService;
import org.openstack.atlas.api.integration.ReverseProxyLoadBalancerVTMService;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.NodeMetadataService;
import org.openstack.atlas.service.domain.services.NodeService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NodeMetaResourceTest {

    public static class WhenRetrievingNodeMata {
        private NodeMetaResource nodeMetaResource;
        private NodeMetadataService nodeMetadataService;
        private Mapper dozerMapper;
        private Response response;

        @Before
        public void setUp() {
            nodeMetadataService = mock(NodeMetadataService.class);
            dozerMapper = mock(Mapper.class);
            nodeMetaResource = new NodeMetaResource();
            nodeMetaResource.setNodeId(5);
            nodeMetaResource.setId(123);
            nodeMetaResource.setNodeMetadataService(nodeMetadataService);
            nodeMetaResource.setDozerMapper(dozerMapper);
        }

        @Test
        public void shouldRetrieveNodeMetaSuccessfully() throws EntityNotFoundException {
            NodeMeta domainNodeMeta = new NodeMeta();
            domainNodeMeta.setId(123);
            Meta meta = new Meta();
            when(nodeMetadataService.getNodeMeta(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(domainNodeMeta);
            doReturn(meta).when(dozerMapper).map(domainNodeMeta, org.openstack.atlas.docs.loadbalancers.api.v1.Meta.class);
            response = nodeMetaResource.retrieveNodeMeta();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowExceptionWhileRetrievingNodeMeta() throws EntityNotFoundException {
            doThrow(Exception.class).when(nodeMetadataService).getNodeMeta(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = nodeMetaResource.retrieveNodeMeta();
            Assert.assertEquals(500, response.getStatus());

        }
    }

    public static class WhenUpdatingNodeMeta {
        private NodeMetaResource nodeMetaResource;
        private NodeMetadataService nodeMetadataService;
        private NodeMeta domainNodeMeta;
        private Mapper dozerMapper;
        private Response response;
        private Meta meta;


        @Before
        public void setUp() {
            nodeMetadataService = mock(NodeMetadataService.class);
            dozerMapper = mock(Mapper.class);
            nodeMetaResource = new NodeMetaResource();
            domainNodeMeta = new NodeMeta();
            nodeMetaResource.setNodeId(5);
            nodeMetaResource.setId(123);
            nodeMetaResource.setNodeMetadataService(nodeMetadataService);
            nodeMetaResource.setDozerMapper(dozerMapper);
        }

        @Test
        public void shouldFailValidationForUpdateMeta() throws Exception {
            response = nodeMetaResource.updateMeta(new Meta());
            Assert.assertEquals(400, response.getStatus());
            BadRequest entity = (BadRequest) response.getEntity();
            Assert.assertEquals("Validation Failure", entity.getMessage());
        }

        @Test
        public void shouldSuccessfullyUpdateMeta() throws Exception {
            Meta callNodeMeta = new Meta();
            callNodeMeta.setValue("some random value");
            doReturn(domainNodeMeta).when(dozerMapper).map(callNodeMeta, NodeMeta.class);
            when(nodeMetadataService.updateNodeMeta(ArgumentMatchers.anyInt(),ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.eq(domainNodeMeta))).thenReturn(domainNodeMeta);
            doReturn(meta).when(dozerMapper).map(domainNodeMeta, Meta.class);
            response = nodeMetaResource.updateMeta(callNodeMeta);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowExceptionWhileUpdateMeta() throws Exception {
            Meta callNodeMeta = new Meta();
            callNodeMeta.setValue("some random value");
            doThrow(Exception.class).when(dozerMapper).map(callNodeMeta, NodeMeta.class);
            response = nodeMetaResource.updateMeta(callNodeMeta);
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenDeletingNodeMeta {
        @Mock
        private NodeService nodeService;
        @InjectMocks
        private NodeMetaResource nodeMetaResource;
        private NodeMetadataService nodeMetadataService;
        private Response response;
        private Node node;
        private List<Integer> ids;


        @Before
        public void setUp() {
            nodeMetadataService = mock(NodeMetadataService.class);
            nodeMetaResource = new NodeMetaResource();
            ids = mock(List.class);
            nodeMetaResource.setNodeId(5);
            nodeMetaResource.setId(123);
            nodeMetaResource.setNodeMetadataService(nodeMetadataService);
            node = new Node();
            nodeMetaResource.setAccountId(1);
            nodeMetaResource.setLoadbalancerId(1234);
            nodeMetaResource.setNodeId(5);
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void shouldDeleteMetaSuccessfully() throws Exception {
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(node);
            doReturn(ids).when(nodeMetadataService).prepareForNodeMetadataDeletion(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.eq(ids));
            doNothing().when(nodeMetadataService).deleteNodeMetadata(node, ids);
            response = nodeMetaResource.deleteMeta();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowExceptionWhileDeleteMeta() throws Exception {
            doThrow(Exception.class).when(nodeService).getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = nodeMetaResource.deleteMeta();
            Assert.assertEquals(500, response.getStatus());
        }


    }
}
