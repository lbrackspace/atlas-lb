package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadbalancerMeta;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadBalancerService;
import org.openstack.atlas.service.domain.services.LoadbalancerMetadataService;
import org.openstack.atlas.service.domain.services.impl.LoadbalancerMetadataServiceImpl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(Enclosed.class)
public class LoadbalancerMetadataResourceTest {

    public static class WhenCreatingAndDeletingLoadbalancerMetadata {

        private LoadbalancerMetadataResource loadbalancerMetadataResourceResource;

        private Metadata metadata;
        private Meta meta;
        private Meta meta2;
        private Response response;
        private LoadBalancer loadBalancer;
        private List<Integer> metaDataIds;
        List<String> validationErrors;


        @Mock
        private LoadBalancerService loadBalancerService;
        @Mock
        private DozerBeanMapper mapper;
        @Mock
        private LoadbalancerMetadataService loadbalancerMetadataService;


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadbalancerMetadataResourceResource = new LoadbalancerMetadataResource();
            loadbalancerMetadataResourceResource.setLoadBalancerService(loadBalancerService);
            loadbalancerMetadataResourceResource.setDozerMapper(mapper);
            loadbalancerMetadataResourceResource.setLoadbalancerMetadataService(loadbalancerMetadataService);
            loadbalancerMetadataResourceResource.setLoadBalancerId(1);
            loadbalancerMetadataResourceResource.setAccountId(1234);
            loadBalancer = new LoadBalancer();
            metadata = new Metadata();
            meta = new Meta();
            meta2 = new Meta();
            metaDataIds = new ArrayList<>();
            validationErrors = new ArrayList<>();
            meta.setKey("testKey");
            meta.setValue("testValue");
            meta2.setKey("testKey2");
            meta2.setValue("testValue2");
            when(loadBalancerService.get(anyInt(), anyInt())).thenReturn(loadBalancer);


        }

        @Test
        public void shouldReturnA400Response(){
            response = loadbalancerMetadataResourceResource.createMetadata(metadata);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturnA200Response(){
            metadata.getMetas().add(meta);
            metadata.getMetas().add(meta2);
            response = loadbalancerMetadataResourceResource.createMetadata(metadata);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturnA400ResponseIfMetaValueIsNull(){
            meta.setValue(null);
            metadata.getMetas().add(meta);
            metadata.getMetas().add(meta2);
            response = loadbalancerMetadataResourceResource.createMetadata(metadata);
            Assert.assertEquals(400, response.getStatus());
        }

        @Test
        public void shouldReturn200WhenDeletingLBMetadata () throws Exception {
            metaDataIds.add(1);
            metaDataIds.add(2);
            response = loadbalancerMetadataResourceResource.deleteMetadata(metaDataIds);
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn400whenDeletingLBMetadata () throws Exception {
            response = loadbalancerMetadataResourceResource.deleteMetadata(metaDataIds);
            Assert.assertEquals(400, response.getStatus());
        }
        @Test
        public void shouldReturn400WhenHasValidationErrors() throws Exception {
            metaDataIds.add(1);
            metaDataIds.add(2);
            validationErrors.add("testError");
            when(loadbalancerMetadataService.prepareForLoadbalancerMetadataDeletion(anyInt(), anyInt(), anyList())).thenReturn(validationErrors);
            response = loadbalancerMetadataResourceResource.deleteMetadata(metaDataIds);
            Assert.assertEquals(400, response.getStatus());
        }


    }


    public static class whenRetrievingLoadbalancerMetadata {

        private LoadbalancerMetadataResource loadbalancerMetadataResourceResource;

        Set<LoadbalancerMeta> loadbalancerMetas;
        private LoadbalancerMeta meta;
        private LoadbalancerMeta meta2;
        private Response response;

        @Mock
        private LoadbalancerMetadataServiceImpl loadbalancerMetadataService;
        @Mock
        private DozerBeanMapper mapper;


        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadbalancerMetadataResourceResource = new LoadbalancerMetadataResource();
            loadbalancerMetadataResourceResource.setLoadbalancerMetadataService(loadbalancerMetadataService);
            loadbalancerMetadataResourceResource.setDozerMapper(mapper);
            loadbalancerMetadataResourceResource.setLoadBalancerId(1);
            loadbalancerMetadataResourceResource.setAccountId(1234);
            loadbalancerMetas = new HashSet<>();
            meta = new LoadbalancerMeta();
            meta2 = new LoadbalancerMeta();
            meta.setKey("testKey");
            meta.setValue("testValue");
            meta2.setKey("testKey2");
            meta2.setValue("testValue2");
            loadbalancerMetas.add(meta);
            loadbalancerMetas.add(meta2);
            when(loadbalancerMetadataService.getLoadbalancerMetadataByAccountIdLoadBalancerId(anyInt(), anyInt())).thenReturn(loadbalancerMetas);

        }

        @Test
        public void shouldReturn200forRetrieveLBMetadata() throws Exception {
            response = loadbalancerMetadataResourceResource.retrieveMetadata();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn500forRetrieveLBMetadata() throws Exception {
            when(loadbalancerMetadataService.getLoadbalancerMetadataByAccountIdLoadBalancerId(anyInt(), anyInt())).thenThrow(Exception.class);
            response = loadbalancerMetadataResourceResource.retrieveMetadata();
            Assert.assertEquals(500 , response.getStatus());
        }


    }


}
