package org.openstack.atlas.api.resources.integration;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class LoadBalancersIntegrationTest {

    private HttpServer threadSelector;
    private WebTarget webResource;

    @Before
    public void setUp() throws Exception {
        threadSelector = Helper.startServer();

        Client client = ClientBuilder.newBuilder().build();
        webResource = client.target(Helper.BASE_URI);
    }

    @Ignore
    @Test
    public void should_be_able_to_grab_the_wadl() {
        String applicationWadl = webResource.path("/application.wadl").request().get(String.class);
        Assert.assertTrue("Something wrong. Returned wadl length is not > 0", applicationWadl.length() > 0);
    }

    @Test
    @Ignore
    public void should_get_all_loadBalancers_in_xml_format() {
        LoadBalancers response = webResource.path("435453/loadbalancers/detail").
                request("application/xml").get(LoadBalancers.class);

        Assert.assertEquals("Expected number of initial entries not found",
                2, response.getLoadBalancers().size());
    }

    @After
    public void tearDown() throws Exception {
        threadSelector.shutdown();
    }
}
