package org.openstack.atlas.restclients.dns;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.openstack.atlas.restclients.dns.objects.CustomObjects.RootElementRdns;
import org.openstack.atlas.restclients.dns.objects.DnsFault;
import org.openstack.atlas.util.b64aes.Base64;

import org.openstack.atlas.restclients.dns.objects.RecordsList;
import org.openstack.atlas.restclients.dns.objects.Rdns;
import org.openstack.atlas.restclients.dns.objects.Domains;
import org.openstack.atlas.restclients.dns.objects.LinkType;
import org.openstack.atlas.restclients.dns.objects.Record;
import org.openstack.atlas.restclients.dns.objects.RecordType;

public class DnsClient1_0 {

    private static final Logger log = Logger.getLogger(DnsClient1_0.class.getName());
    private String token = "";
    private String endPoint = "";
    private Integer accountId = -1;
    private String adminEndPoint = "";
    private String adminUser = "";
    private String adminPasswd = "";

    public DnsClient1_0(String endPoint, String adminEndPoint, String adminUser,
            String adminPasswd, String token, int accountId) {
        this.accountId = accountId;
        this.endPoint = endPoint;
        this.token = token;
        this.adminEndPoint = adminEndPoint;
        this.adminUser = adminUser;
        this.adminPasswd = adminPasswd;
    }

    public DnsClient1_0(String endPoint, String token, Integer accountId) {
        this.endPoint = endPoint;
        this.accountId = accountId;
        this.token = token;
    }

    public ClientResponse getDomains(String name, Integer limit, Integer offset) {
        Client client = new Client();
        String url = String.format("/%d/domains", accountId);
        WebResource wr = client.resource(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        if (name != null) {
            wr = wr.queryParam("name", name);
        }
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header("x-auth-token", this.token);
        ClientResponse resp = rb.get(ClientResponse.class);
        
        return resp;
    }

    public ClientResponse getPtrRecords(Integer domainId, String deviceUrl, String serviceName, Integer limit, Integer offset) {
        Client client = new Client();
        String url = String.format("/%d/rdns/%s", accountId, serviceName);
        WebResource wr = client.resource(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        wr = wr.queryParam("href", deviceUrl);
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header("x-auth-token", this.token);
        ClientResponse resp = rb.get(ClientResponse.class);
        return resp;
    }

    public ClientResponse addPtrRecord(Integer domainId, String deviceUrl,
            String serviceName, String name, String ip, Integer ttl) {
        RootElementRdns rdnsRequest = new RootElementRdns();
        LinkType link = new LinkType();
        rdnsRequest.setLink(link);
        link.setHref(deviceUrl);
        link.setRel(serviceName);  // Use "cloudLoadBalancers"
        RecordsList records = new RecordsList();
        rdnsRequest.setRecordsList(records);
        Record ptr = new Record();
        records.getRecord().add(ptr);
        ptr.setName(name);
        if (ttl != null) {
            ptr.setTtl(ttl);
        }
        ptr.setData(ip);
        ptr.setType(RecordType.PTR);

        String url = String.format("/%d/rdns", accountId);
        Client client = new Client();
        WebResource wr = client.resource(endPoint).path(url);
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header("x-auth-token", token);
        ClientResponse resp = rb.post(ClientResponse.class, rdnsRequest);
        return resp;
    }

    public Object delPtrRecord(Integer domainId,String deviceUrl,String serviceName,String ip){
        String url = String.format("/%d/rdns/%s",accountId,serviceName);
        Client client = new Client();
        WebResource wr = client.resource(endPoint).path(url);
        wr = wr.queryParam("href",deviceUrl);
        if(ip != null){
            wr = wr.queryParam("ip",ip);
        }
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb.header("x-auth-token",token);
        ClientResponse resp = rb.delete(ClientResponse.class);
        return resp;
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

    public String encodeBasicAuth() throws UnsupportedEncodingException {
        return "BASIC " + Base64.encode(adminUser + ":" + adminPasswd);
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

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAdminEndPoint() {
        return adminEndPoint;
    }

    public void setAdminEndPoint(String adminEndPoint) {
        this.adminEndPoint = adminEndPoint;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPasswd() {
        return adminPasswd;
    }

    public void setAdminPasswd(String adminPasswd) {
        this.adminPasswd = adminPasswd;
    }

    @Override
    public String toString() {
        String fmt = "{ endPoint=\"%s\",accountId=\"%d\" token=\"%s\""
                + "adminEndPoint=\"%s\" adminUser = \"%s\", "
                + "adminPasswd = \"%s\" }";
        String msg = String.format(fmt, endPoint, accountId, token, adminEndPoint,
                adminUser, adminPasswd);
        return msg;
    }
}
