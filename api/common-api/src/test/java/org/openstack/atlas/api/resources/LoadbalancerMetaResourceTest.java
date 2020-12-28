package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapper;
import org.glassfish.grizzly.utils.ArraySet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadbalancerMeta;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.LoadbalancerMetadataService;

import javax.ws.rs.core.Response;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadbalancerMetaResourceTest {

    public static class whenRetrievingMeta {

        @Mock
        LoadbalancerMetadataService loadbalancerMetadataService;
        @Mock
        DozerBeanMapper dozerBeanMapper;

        LoadbalancerMetaResource loadbalancerMetaResource;
        LoadbalancerMeta loadbalancerMeta;
        LoadBalancer loadbalancer;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadbalancerMetaResource = new LoadbalancerMetaResource();
            loadbalancerMeta = new LoadbalancerMeta();
            loadbalancerMetaResource.setDozerMapper(dozerBeanMapper);
            loadbalancerMetaResource.setLoadbalancerMetadataService(loadbalancerMetadataService);

            loadbalancer = new LoadBalancer();

            loadbalancerMeta.setId(1);
            loadbalancerMeta.setKey("testKey");
            loadbalancerMeta.setValue("testValue");
            loadbalancerMeta.setLoadbalancer(loadbalancer);
            when(loadbalancerMetadataService.getLoadbalancerMeta(anyInt(), anyInt(), anyInt())).thenReturn(loadbalancerMeta);

        }

        @Test
        public void shouldReturn200whenGettingMeta() throws Exception {
            Response response = loadbalancerMetaResource.retrieveMeta();
            Assert.assertEquals(200, response.getStatus());
        }
        @Test
        public void shouldReturn500whenNoMetasFound()throws  Exception {
            loadbalancerMetaResource.setLoadbalancerMetadataService(null);
            Response response = loadbalancerMetaResource.retrieveMeta();

            Assert.assertEquals(500, response.getStatus());
        }



    }

    public static class whenUpdatingMeta {

        @Mock
        LoadbalancerMetadataService loadbalancerMetadataService;
        @Mock
        DozerBeanMapper dozerBeanMapper;

        LoadbalancerMetaResource loadbalancerMetaResource;
        LoadBalancer loadbalancer;
        Meta meta;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadbalancerMetaResource = new LoadbalancerMetaResource();
            loadbalancerMetaResource.setDozerMapper(dozerBeanMapper);
            loadbalancerMetaResource.setLoadbalancerMetadataService(loadbalancerMetadataService);
            loadbalancerMetaResource.setAccountId(1234);
            loadbalancerMetaResource.setLoadBalancerId(1);
            meta = new Meta();
            loadbalancer = new LoadBalancer();
            meta.setValue("blue");
            when(dozerBeanMapper.map(any(), any())).thenReturn(loadbalancer);

        }


        @Test
        public void shouldReturn200WhenUpdatingMeta() throws Exception {

            Response response = loadbalancerMetaResource.updateMeta(meta);
            Assert.assertEquals(200, response.getStatus());

        }

        @Test
        public void shouldReturn400WhenUpdatingInvalidMeta() throws Exception{
            meta.setId(1);
            Response response = loadbalancerMetaResource.updateMeta(meta);
            Assert.assertEquals(400, response.getStatus());

        }





    }

    public static class whenDeletingMeta {

        @Mock
        LoadbalancerMetadataService loadbalancerMetadataService;

        LoadbalancerMetaResource loadbalancerMetaResource;
        LoadBalancer loadbalancer;
        Meta meta;

        @Before
        public void setUp() throws EntityNotFoundException {
            MockitoAnnotations.initMocks(this);
            loadbalancerMetaResource = new LoadbalancerMetaResource();
            loadbalancerMetaResource.setLoadbalancerMetadataService(loadbalancerMetadataService);
            loadbalancerMetaResource.setAccountId(1234);
            loadbalancerMetaResource.setLoadBalancerId(1);
            meta = new Meta();
            loadbalancer = new LoadBalancer();
            meta.setValue("blue");

        }

        @Test
        public void shouldReturn200ResponseWhenDeletingMeta() throws Exception {
            Response response = loadbalancerMetaResource.deleteMeta();
            Assert.assertEquals(200, response.getStatus());
        }
        @Test
        public void shouldReturn500WhenDeletingMeta() throws Exception {
            loadbalancerMetaResource.setLoadbalancerMetadataService(null);
            Response response = loadbalancerMetaResource.deleteMeta();
            Assert.assertEquals(500, response.getStatus());
        }


    }

}
