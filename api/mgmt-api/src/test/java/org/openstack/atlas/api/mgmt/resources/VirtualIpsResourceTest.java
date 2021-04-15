package org.openstack.atlas.api.mgmt.resources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openstack.atlas.api.mgmt.integration.ManagementAsyncService;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;

import javax.ws.rs.core.Response;

@RunWith(Enclosed.class)
public class VirtualIpsResourceTest {

    public static class whenMigratingVips {

        VirtualIpsResource virtualIpsResource;

        @Mock
        ManagementAsyncService asyncService;

        Cidr cidr;

        Integer newClusterId;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            virtualIpsResource = new VirtualIpsResource();
            virtualIpsResource.setMockitoAuth(true);
            virtualIpsResource.setManagementAsyncService(asyncService);
            cidr = new Cidr();
            cidr.setBlock("10.25.0.0/24");
            newClusterId = 1;
        }

        @Test
        public void shouldReturn200WithProperCidrAndClusterId() {

            Response response = virtualIpsResource.updateClusterForVipBlock(newClusterId, cidr);
            Assert.assertEquals(202, response.getStatus());

        }



    }

}
