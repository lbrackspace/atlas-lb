package org.openstack.atlas.restclients.dns;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.openstack.atlas.restclients.dns.objects.DnsFault;
import org.openstack.atlas.restclients.dns.objects.Domains;

public class DnsClient1_0 {

    private String token;
    private String endPoint;
    private Integer accountId;

    public DnsClient1_0(String endPoint, String token, int accountId) {
        this.accountId = accountId;
        this.endPoint = endPoint;
        this.token = token;
    }

    public Object getDomains(String name, Integer limit, Integer offset) {
        Builder rb;
        WebResource wr;
        Client client = new Client();
        ClientResponse resp;
        String url = String.format("/%d/domains", accountId);
        wr = client.resource(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        if (name != null) {
            wr = wr.queryParam("name", name);
        }
        rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header("x-auth-token", this.token);
        resp = rb.get(ClientResponse.class);
        Object out = getDomainsEntity(resp);
        return out;
    }

    private Object getDomainsEntity(ClientResponse resp) {
        Object out;
        int sc = resp.getClientResponseStatus().getStatusCode();
        switch (sc) {
            case 200:
                out = resp.getEntity(Domains.class);
                break;
            case 400:
            case 401:
            case 404:
            case 406:
            case 413:
            case 500:
            case 503:
                out = resp.getEntity(DnsFault.class);
                break;
            default:
                InputStream is = resp.getEntityInputStream();
                try {
                    String body = IOUtils.toString(is, "UTF-8");
                    String fmt = "Error reading domains from Dns service: %s";
                    String msg = String.format(fmt, body);
                    throw new RuntimeException(msg);
                } catch (IOException ex) {
                    throw new RuntimeException("Error reading response from Dns service");
                }
        }
        return out;
    }

    public Object getDomains() {
        return getDomains(null, null, null);
    }

    private WebResource addLimitOffsetParams(WebResource wr, Integer limit, Integer offset) {
        if (limit != null) {
            wr = wr.queryParam("limit", limit.toString());
        }
        if (offset != null) {
            wr = wr.queryParam("offset", offset.toString());
        }
        return wr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public Integer getAccountId() {
        return accountId;
    }
}
