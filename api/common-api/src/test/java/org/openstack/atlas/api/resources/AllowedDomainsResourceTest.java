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
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Stats;
import org.openstack.atlas.service.domain.services.AllowedDomainsService;
import org.openstack.atlas.service.domain.services.LoadBalancerService;

import javax.jms.JMSException;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class AllowedDomainsResourceTest {

    public static class WhenRetrievingResources {
        private AllowedDomainsService allowedDomainsService;
        private AllowedDomainsResource allowedDomainsResource;
        private Set<String> ads;
        private Response response;

        @Before
        public void setUp() {
            allowedDomainsService = mock(AllowedDomainsService.class);
            allowedDomainsResource = new AllowedDomainsResource();
            allowedDomainsResource.setAllowedDomainsService(allowedDomainsService);
            ads = new HashSet<String>();
            ads.add("Domain1");
            ads.add("Domain2");

        }

        @Test
        public void shouldReturn200OnSuccessfullRetrieval() {
            doReturn(ads).when(allowedDomainsService).getAllowedDomains();
            response = allowedDomainsResource.retireveAllowedDomains();
            Assert.assertEquals(200, response.getStatus());
        }

        @Test
        public void shouldReturn500OnUnsuccessfullRetrieval() {
            doThrow(Exception.class).when(allowedDomainsService).getAllowedDomains();
            response = allowedDomainsResource.retireveAllowedDomains();
            Assert.assertEquals(500, response.getStatus());
        }
    }
}
