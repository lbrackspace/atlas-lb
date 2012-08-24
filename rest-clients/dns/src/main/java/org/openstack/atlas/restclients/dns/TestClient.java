package org.openstack.atlas.restclients.dns;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.ApacheHttpClientHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.io.IOUtils;

public class TestClient {

    private String endPoint;

    public TestClient(String endPoint) {
        this.endPoint = endPoint;
    }

    public List<ClientResponse> testPost(String url, String acceptType, String contentType, List<Object> objList) {
        int i;
        List<ClientResponse> out = new ArrayList<ClientResponse>();
        Client client = new Client();
        for (Object obj : objList) {
            WebResource wr = client.resource(endPoint).path(url);
            Builder rb = wr.accept(acceptType);
            rb.type(contentType);
            rb.header("body", "echo");
            ClientResponse resp = rb.post(ClientResponse.class, obj);
            out.add(resp);
        }
        client.destroy();
        return out;
    }

    public List<ClientResponse> testApache(String url, String acceptType, String contentType, List<Object> objList) {
        int i;
        List<ClientResponse> out = new ArrayList<ClientResponse>();
        HttpClientParams hcp = new HttpClientParams();
        hcp.setConnectionManagerClass(SimpleHttpConnectionManager.class);
        HttpClient hc = new HttpClient(hcp);
        ApacheHttpClientHandler htc = new ApacheHttpClientHandler(hc);
        Client client = new Client(htc);
        for (Object obj : objList) {
            WebResource wr = client.resource(endPoint).path(url);
            Builder rb = wr.accept(acceptType);
            rb = rb.type(contentType);
            rb = rb.header("body", "echo");
            ClientResponse resp = rb.post(ClientResponse.class, obj);
            out.add(resp);
        }
        client.destroy();
        return out;
    }
}
