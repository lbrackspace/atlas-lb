package org.openstack.atlas.api.resources.integration;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/lbService/").port(getPort(9998)).build();
    }

    public static final URI BASE_URI = getBaseURI();

    public static SelectorThread startServer() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages",
                "org.openstack.atlas.api.resources");

        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
        return threadSelector;
    }
}
