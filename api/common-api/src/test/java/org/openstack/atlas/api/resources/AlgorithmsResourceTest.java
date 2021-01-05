package org.openstack.atlas.api.resources;

import org.dozer.DozerBeanMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.docs.loadbalancers.api.v1.Algorithms;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm;
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject;
import org.openstack.atlas.service.domain.services.AlgorithmsService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class AlgorithmsResourceTest {

    public static class whenRetirievingLoadBalancingAlgorithms {


        @Mock
        AlgorithmsService algorithmsService;
        @Mock
        DozerBeanMapper dozerBeanMapper;

        AlgorithmsResource algorithmsResource;

        LoadBalancerAlgorithmObject loadBalancerAlgorithmObject;
        LoadBalancerAlgorithmObject loadBalancerAlgorithmObject2;
        List<org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject> loadBalancerProtocolObjects;


        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);

            algorithmsResource = new AlgorithmsResource();
            algorithmsResource.setDozerMapper(dozerBeanMapper);
            algorithmsResource.setAlgorithmsService(algorithmsService);
            loadBalancerAlgorithmObject = new LoadBalancerAlgorithmObject();
            loadBalancerAlgorithmObject2 = new LoadBalancerAlgorithmObject();
            loadBalancerProtocolObjects = new ArrayList<>();
            loadBalancerAlgorithmObject.setName(LoadBalancerAlgorithm.ROUND_ROBIN);
            loadBalancerAlgorithmObject.setDescription("test");
            loadBalancerAlgorithmObject.setEnabled(true);
            loadBalancerAlgorithmObject2.setName(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN);
            loadBalancerAlgorithmObject2.setDescription("test2");
            loadBalancerAlgorithmObject2.setEnabled(false);
            loadBalancerProtocolObjects.add(loadBalancerAlgorithmObject);
            when(algorithmsService.get()).thenReturn(loadBalancerProtocolObjects);

        }


        @Test
        public void shouldReturn200WhenRetrievingAlgorithms() {

            Response response = algorithmsResource.retrieveLoadBalancingAlgorithms();
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(Algorithms.class, response.getEntity().getClass());
        }

        @Test
        public void shouldReturn200WhenRetrievingMultipleAlgorithms() {
            loadBalancerProtocolObjects.add(loadBalancerAlgorithmObject2);
            Response response = algorithmsResource.retrieveLoadBalancingAlgorithms();
            Assert.assertEquals(200, response.getStatus());
            Assert.assertEquals(Algorithms.class, response.getEntity().getClass());

        }


        @Test
        public void shouldReturn500WhenNullIsReturnedFromAlgorithmService() {
            when(algorithmsService.get()).thenReturn(null);
            Response response = algorithmsResource.retrieveLoadBalancingAlgorithms();
            Assert.assertEquals(500, response.getStatus());
        }

        @Test
        public void shouldReturn200WithEmptyAlgorithmArray() {
            loadBalancerProtocolObjects.clear();
            Response response = algorithmsResource.retrieveLoadBalancingAlgorithms();
            Assert.assertEquals(200, response.getStatus());
        }


    }






}
