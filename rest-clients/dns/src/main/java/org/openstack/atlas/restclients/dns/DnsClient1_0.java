package org.openstack.atlas.restclients.dns;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map.Entry;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.restclients.dns.exp.JaxbExp;
import org.openstack.atlas.restclients.dns.pub.objects.Record;
import org.openstack.atlas.restclients.dns.pub.objects.Rdns;
import org.openstack.atlas.restclients.dns.pub.objects.RecordType;
import org.openstack.atlas.restclients.dns.pub.objects.RecordsList;

import org.openstack.atlas.util.b64aes.Base64;
import org.openstack.atlas.util.debug.Debug;
import org.w3._2005.atom.Link;

public class DnsClient1_0 {

    private static final Log LOG = LogFactory.getLog(DnsClient1_0.class);
    private static final int SB_INIT_SIZE = 1024 * 4;
    private int accountId = -1;
    private String token = "";
    private String endPoint = "";
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

    public DnsClient1_0(String endPoint, String token, int accountId) {
        this.endPoint = endPoint;
        this.accountId = accountId;
        this.token = token;
    }

    public ClientResponse getDomain(Integer domainId, Boolean showRecords,
            Boolean showSubDomains,
            Integer limit, Integer offset) {
        String url = String.format("/%d/domains/%d", accountId, domainId);
        Client client = new Client();
        WebResource wr = client.resource(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        if (showRecords != null) {
            wr.queryParam("showRecords", showRecords ? "true" : "false");
        }
        if (showSubDomains != null) {
            wr.queryParam("showSubDomains", showSubDomains ? "true" : "false");
        }

        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb.header("x-auth-token", this.token);
        ClientResponse resp = rb.get(ClientResponse.class);
        return resp;
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

    public ClientResponse getPtrRecords(String deviceUrl, String serviceName, Integer limit, Integer offset) {
        Client client = new Client();
        String authKey = "x-auth-token";
        String url = String.format("/%d/rdns/%s", accountId, serviceName);
        WebResource wr = client.resource(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        wr = wr.queryParam("href", deviceUrl);

        String logMsg = String.format("USEING CRED %s:%s CALLING GET %s",authKey,token,wr.toString());
        LOG.info(logMsg);
        System.out.printf("%s\n", logMsg);
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header(authKey, this.token);
        ClientResponse resp = rb.get(ClientResponse.class);
        return resp;
    }

    public ClientResponse addPtrRecord(String deviceUrl,
            String serviceName, String name, String ip, Integer ttl) {
        Rdns rdnsRequest = new Rdns();
        String fmt;
        String msg;
        String authKey = "x-auth-token";
        Link link = new Link();
        rdnsRequest.setLink(link);
        link.setHref(deviceUrl);
        link.setRel(serviceName);  // Use "cloudLoadBalancers"
        RecordsList records = new RecordsList();
        rdnsRequest.setRecordsList(records);
        Record ptr = new Record();
        records.getRecords().add(ptr);
        ptr.setName(name);
        if (ttl != null) {
            ptr.setTtl(ttl);
        }
        ptr.setData(ip);
        ptr.setType(RecordType.PTR);

        String url = String.format("/%d/rdns", accountId);
        String xml;
        try {
            xml = JaxbExp.serialize(rdnsRequest);
            nop();
        } catch (JAXBException ex) {
            xml = "ERROR";
        }
        Client client = new Client();
        WebResource wr = client.resource(endPoint).path(url);
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        fmt = "USEING CRED %s:%s CALLING POST %s\nbody=%s";
        msg = String.format(fmt,authKey,token,wr.toString(),xml);
        LOG.info(msg);
        System.out.printf("%s\n", msg);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header(authKey, token);
        ClientResponse resp = rb.post(ClientResponse.class, rdnsRequest);
        return resp;
    }

    public ClientResponse delPtrRecordPub(String deviceUrl, String serviceName, String ip) {
        return delPtrRecordBaseMethod(deviceUrl, serviceName, ip, "x-auth-token", token, endPoint);
    }

    public ClientResponse delPtrRecordMan(String deviceUrl, String serviceName, String ip) throws UnsupportedEncodingException {
        return delPtrRecordBaseMethod(deviceUrl, serviceName, ip, "authorization", encodeBasicAuth(), adminEndPoint);
    }

    private ClientResponse delPtrRecordBaseMethod(String deviceUrl, String serviceName, String ip,
            String authKey, String authValue, String endPoint) {
        String url = String.format("/%d/rdns/%s", accountId, serviceName);
        Client client = new Client();
        WebResource wr = client.resource(endPoint).path(url);
        wr = wr.queryParam("href", deviceUrl);
        if (ip != null) {
            wr = wr.queryParam("ip", ip);
        }
        String logMsg = String.format("USEING CRED %s:%s CALLING DELETE %s",authKey,authValue,wr.toString());
        LOG.info(logMsg);
        System.out.printf("%s\n", logMsg);
        Builder rb = wr.accept(MediaType.APPLICATION_XML);
        rb = rb.type(MediaType.APPLICATION_XML);
        rb.header(authKey, authValue);
        ClientResponse resp = rb.delete(ClientResponse.class);
        return resp;
    }

    public ClientResponse getDomains() {
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
        return StaticDNSClientUtils.encodeBasicAuth(adminUser,adminPasswd);
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
        String fmt = "{ endPoint=\"%s\",accountId=\"%d\" token=\"%s\","
                + "adminEndPoint=\"%s\" adminUser = \"%s\", "
                + "adminPasswd = \"%s\" }";
        String msg = String.format(fmt, endPoint, accountId, token, adminEndPoint,
                adminUser, adminPasswd);
        return msg;
    }

    private static String getEST(Throwable th) {
        Throwable t;
        StringBuilder sb = new StringBuilder(4096);
        Exception currEx;
        String msg;

        t = th;
        while (t != null) {
            if (t instanceof Exception) {
                currEx = (Exception) t;
                sb.append(String.format("Exception: %s:%s\n", currEx.getMessage(), currEx.getClass().getName()));
                for (StackTraceElement se : currEx.getStackTrace()) {
                    sb.append(String.format("%s\n", se.toString()));
                }
                sb.append("\n");
                t = t.getCause();
            }
        }
        return sb.toString();
    }

    private static void nop() {
    }
}
