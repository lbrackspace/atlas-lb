package org.openstack.atlas.api.resources.integration;

import org.junit.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;import javax.ws.rs.core.UriBuilder;import java.io.IOException;import java.net.URI;import java.util.HashMap;import java.util.Map;

public class LoadBalancersIntegrationTest {

    private SelectorThread threadSelector;
    private WebResource webResource;

    @Before
    public void setUp() throws Exception {
        threadSelector = Helper.startServer();

        Client client = Client.create(new DefaultClientConfig());
        webResource = client.resource(Helper.BASE_URI);
    }

    @Ignore
    @Test
    public void should_be_able_to_grab_the_wadl() {
        String applicationWadl = webResource.path("/application.wadl").get(String.class);
        Assert.assertTrue("Something wrong. Returned wadl length is not > 0", applicationWadl.length() > 0);
    }

    @Test
    @Ignore
    public void should_get_all_loadBalancers_in_xml_format() {
        LoadBalancers response = webResource.path("435453/loadbalancers/detail").
                accept("application/xml").get(LoadBalancers.class);

        Assert.assertEquals("Expected number of initial entries not found",
                2, response.getLoadBalancers().size());
    }

    @After
    public void tearDown() throws Exception {
        threadSelector.stopEndpoint();
    }
}
