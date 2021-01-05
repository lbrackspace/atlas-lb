package org.openstack.atlas.api.resources;

import org.dozer.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeMeta;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.NodeMetadataService;
import org.openstack.atlas.service.domain.services.NodeService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class NodeMetadataResourceTest {

    public static class WhenRetrievingResources {
        @Mock
        private NodeMetadataService nodeMetadataService;
        @Mock
        private Mapper dozerMapper;
        private Set<NodeMeta> domainNodeMetaSet;
        private NodeMetadataResource nodeMetadataResource;
        private Meta meta;
        private Response response;
        private NodeMeta domainMeta;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            nodeMetadataResource = new NodeMetadataResource();
            nodeMetadataResource.setAccountId(123);
            nodeMetadataResource.setNodeId(5);
            domainMeta = new NodeMeta();
            domainMeta.setId(111);
            domainNodeMetaSet = new HashSet<NodeMeta>();
            nodeMetadataResource.setNodeMetadataService(nodeMetadataService);
            nodeMetadataResource.setDozerMapper(dozerMapper);
            meta = new Meta();
            meta.setValue("abc");
            when(nodeMetadataService.getNodeMetadataByAccountIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(domainNodeMetaSet);
            doReturn(meta).when(dozerMapper).map(domainMeta, Meta.class, "NODE_META_DATA");
        }

        @Test
        public void shouldRetrieveMetadataWithStatusCode200() {
            response = nodeMetadataResource.retrieveMetadata();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldThrowNullPointerException() throws EntityNotFoundException {
            doReturn(null).when(nodeMetadataService).getNodeMetadataByAccountIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());
            response = nodeMetadataResource.retrieveMetadata();
            Assert.assertEquals(500, response.getStatus());
        }
    }

    public static class WhenCreatingMetadata {
        @Mock
        private NodeMetadataService nodeMetadataService;
        @Mock
        private Mapper dozerMapper;
        private NodeMetadataResource nodeMetadataResource;
        private Response response;
        private Metadata metadata;
        private Meta meta;
        private Set<NodeMeta> domainNodeMetas;
        private NodeMeta nodeMeta;

        @Before
        public void standUp() throws UnprocessableEntityException, ImmutableEntityException, BadRequestException, EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            metadata = new Metadata();
            meta = new Meta();
            metadata.getMetas().add(meta);
            nodeMetadataResource = new NodeMetadataResource();
            nodeMetadataResource.setAccountId(123);
            nodeMetadataResource.setNodeId(5);
            nodeMetadataResource.setLoadbalancerId(888);
            nodeMetadataResource.setNodeMetadataService(nodeMetadataService);
            nodeMetadataResource.setDozerMapper(dozerMapper);
            nodeMeta = new NodeMeta();
            nodeMeta.setId(123);
            domainNodeMetas = new HashSet<NodeMeta>();
            domainNodeMetas.add(nodeMeta);
            doReturn(nodeMeta).when(dozerMapper).map(meta, NodeMeta.class);
            doReturn(meta).when(dozerMapper).map(nodeMeta, Meta.class);
        }

        @Test
        public void shouldCreateMetadataWhenPassedValidation() throws Exception {
            meta.setKey("A");
            meta.setValue("111");
            doReturn(domainNodeMetas).when(nodeMetadataService).createNodeMetadata(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.<NodeMeta>anySet());
            response = nodeMetadataResource.createMetadata(metadata);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldCreateMetadataWhenFailedValidation() throws Exception {
            meta.setId(2233);
            response = nodeMetadataResource.createMetadata(metadata);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldThrowExceptionWhenItOccuresInTryBlock() throws Exception {
            meta.setKey("A");
            meta.setValue("111");
            doReturn(null).when(nodeMetadataService).createNodeMetadata(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.<NodeMeta>anySet());
            response = nodeMetadataResource.createMetadata(metadata);
            Assert.assertEquals(400, response.getStatus());
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class WhenDeletingMetadata {
        @Mock
        private NodeService nodeService;
        @InjectMocks
        private NodeMetadataResource nodeMetadataResource;
        private List<Integer> metaIds;
        private List<String> validationErrors;
        private NodeMetadataService nodeMetadataService;
        private Node node;
        private Response response;

        @Before
        public void standUp() throws DeletedStatusException, EntityNotFoundException {

            nodeMetadataService = mock(NodeMetadataService.class);
            metaIds = new ArrayList<>();

            nodeMetadataResource = new NodeMetadataResource();
            nodeMetadataResource.setNodeId(1);
            nodeMetadataResource.setAccountId(67);
            nodeMetadataResource.setLoadbalancerId(123);
            validationErrors = new ArrayList<String>();
            node = new Node();
            doNothing().when(nodeMetadataService).deleteNodeMetadata(node, metaIds);
            doReturn(validationErrors).when(nodeMetadataService).prepareForNodeMetadataDeletion(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyList());
            MockitoAnnotations.initMocks(this);

        }

        @Test
        public void shouldReturnA202OnSuccess() throws DeletedStatusException, EntityNotFoundException {
            metaIds.add(111);
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(node);
            response = nodeMetadataResource.deleteMetadata(metaIds);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA400WhenmetaIdIsEmpty() throws DeletedStatusException, EntityNotFoundException {
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(node);
            response = nodeMetadataResource.deleteMetadata(metaIds);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturnA400OnWhenValidationErrorIsThere() throws DeletedStatusException, EntityNotFoundException {
            validationErrors.add("Some Error");
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenReturn(node);
            response = nodeMetadataResource.deleteMetadata(metaIds);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturnA400OnGenericException() throws DeletedStatusException, EntityNotFoundException {
            when(nodeService.getNodeByAccountIdLoadBalancerIdNodeId(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())).thenThrow(Exception.class);
            response = nodeMetadataResource.deleteMetadata(metaIds);
            Assert.assertEquals(400, response.getStatus());
        }
    }
}
