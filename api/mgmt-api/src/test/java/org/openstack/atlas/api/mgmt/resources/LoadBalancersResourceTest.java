package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class LoadBalancersResourceTest {

    public static class whenReassigningALoadBalancersHost {

        LoadBalancersResource loadBalancersResource;

        @Mock
        ManagementAsyncService asyncService;
        @Mock
        LoadBalancerService loadBalancerService;
        @Mock
        LoadBalancerRepository loadBalancerRepository;
        @Mock
        LoadBalancerResource loadBalancerResource;

        org.openstack.atlas.docs.loadbalancers.api.management.v1.Host host2;
        List<org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer> loadBalancers;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer loadBalancer;

        LoadBalancers lbs;
        Host host;
        List<LoadBalancer> validLbs;
        LoadBalancer validLb;

        @Before
        public void setUp() throws Exception {
            MockitoAnnotations.initMocks(this);
            loadBalancersResource = new LoadBalancersResource();
            loadBalancersResource.setMockitoAuth(true);
            loadBalancersResource.setManagementAsyncService(asyncService);
            loadBalancersResource.setLoadBalancerService(loadBalancerService);
            loadBalancersResource.setLoadBalancerRepository(loadBalancerRepository);
            loadBalancersResource.setLoadBalancerResource(loadBalancerResource);
            loadBalancers = new ArrayList<>();
            loadBalancer = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            host2 = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Host();
            loadBalancer = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            host2.setId(3);
            loadBalancer.setId(2);
            loadBalancer.setHost(host2);
            lbs = new LoadBalancers();
            lbs.getLoadBalancers().add(loadBalancer);
            lbs.getLoadBalancers();
            host = new Host();
            validLbs = new ArrayList<>();
            validLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            validLb.setHost(host);
            validLb.setId(1);
            host.setId(1);
            host.setName("testHost");
            validLbs.add(validLb);
            when(loadBalancerService.reassignLoadBalancerHost(anyList())).thenReturn(validLbs);
        }

        @Test
        public void reAssignHostsShouldReturn202() throws Exception {
           Response resp  = loadBalancersResource.reAssignHosts(lbs);
           Assert.assertEquals(202, resp.getStatus());
        }
        @Test
        public void reAssignHostsShouldReturn400whenLBIdNull() throws Exception {
            loadBalancer.setId(null);
            Response resp  = loadBalancersResource.reAssignHosts(lbs);
            Assert.assertEquals(400, resp.getStatus());
        }
        @Test
        public void reassignHostShouldReturn202WhenNoHostSpecified() throws Exception {
            loadBalancer.setHost(null);
            Response resp  = loadBalancersResource.reAssignHosts(lbs);
            Assert.assertEquals(202, resp.getStatus());
        }
        @Test
        public void reAssignHostsShouldReturn202ForMultipleLBs() throws Exception {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer loadBalancer2 = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            loadBalancer2.setId(4);
            lbs.getLoadBalancers().add(loadBalancer2);
            Response resp  = loadBalancersResource.reAssignHosts(lbs);
            Assert.assertEquals(202, resp.getStatus());
        }


    }


}
