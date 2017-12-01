package org.openstack.atlas.restclients.dns;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientResponse;
import org.openstack.atlas.restclients.dns.exp.JaxbExp;
import org.openstack.atlas.restclients.dns.pub.objects.Record;
import org.openstack.atlas.restclients.dns.pub.objects.Rdns;
import org.openstack.atlas.restclients.dns.pub.objects.RecordType;
import org.openstack.atlas.restclients.dns.pub.objects.RecordsList;

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

    public Response getDomain(Integer domainId, Boolean showRecords,
                                    Boolean showSubDomains,
                                    Integer limit, Integer offset) {
        String url = String.format("/%d/domains/%d", accountId, domainId);
        Client client = ClientBuilder.newClient();
        WebTarget wr = client.target(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        if (showRecords != null) {
            wr.queryParam("showRecords", showRecords ? "true" : "false");
        }
        if (showSubDomains != null) {
            wr.queryParam("showSubDomains", showSubDomains ? "true" : "false");
        }

        Invocation.Builder rb = wr.request(MediaType.APPLICATION_XML);
//        rb = rb.type(MediaType.APPLICATION_XML);
        rb.header("x-auth-token", this.token);
        Response resp = rb.get();
        return resp;
    }

    public Response getDomains(String name, Integer limit, Integer offset) {
        Client client = ClientBuilder.newClient();
        String url = String.format("/%d/domains", accountId);
        WebTarget wr = client.target(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        if (name != null) {
            wr = wr.queryParam("name", name);
        }
        Invocation.Builder rb = wr.request(MediaType.APPLICATION_XML);
//        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header("x-auth-token", this.token);
        Response resp = rb.get();
        return resp;
    }

    public Response getPtrRecords(String deviceUrl, String serviceName, Integer limit, Integer offset) {
        Client client = ClientBuilder.newClient();
        String authKey = "x-auth-token";
        String url = String.format("/%d/rdns/%s", accountId, serviceName);
        WebTarget wr = client.target(endPoint).path(url);
        wr = addLimitOffsetParams(wr, limit, offset);
        wr = wr.queryParam("href", deviceUrl);

        String logMsg = String.format("USEING CRED %s CALLING GET %s",authKey,wr.toString());
        LOG.info(logMsg);
        System.out.printf("%s\n", logMsg);
        Invocation.Builder rb = wr.request(MediaType.APPLICATION_XML);
//        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header(authKey, this.token);
        Response resp = rb.get();
        return resp;
    }

    public Response addPtrRecord(String deviceUrl,
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
        Client client = ClientBuilder.newClient();
        WebTarget wr = client.target(endPoint).path(url);
        Invocation.Builder rb = wr.request(MediaType.APPLICATION_XML);
        fmt = "USEING CRED %s CALLING POST %s\nbody=%s";
        msg = String.format(fmt,authKey,wr.toString(),xml);
        LOG.info(msg);
        System.out.printf("%s\n", msg);
//        rb = rb.type(MediaType.APPLICATION_XML);
        rb = rb.header(authKey, token);
        Response resp = wr.request().post(Entity.entity(rdnsRequest, MediaType.APPLICATION_XML));
        return resp;
    }

    public Response delPtrRecordPub(String deviceUrl, String serviceName, String ip) {
        return delPtrRecordBaseMethod(deviceUrl, serviceName, ip, "x-auth-token", token, endPoint);
    }

    public Response delPtrRecordMan(String deviceUrl, String serviceName, String ip) throws UnsupportedEncodingException {
        return delPtrRecordBaseMethod(deviceUrl, serviceName, ip, "authorization", encodeBasicAuth(), adminEndPoint);
    }

    private Response delPtrRecordBaseMethod(String deviceUrl, String serviceName, String ip,
            String authKey, String authValue, String endPoint) {
        String url = String.format("/%d/rdns/%s", accountId, serviceName);
        Client client = ClientBuilder.newClient();
        WebTarget wr = client.target(endPoint).path(url);
        wr = wr.queryParam("href", deviceUrl);
        if (ip != null) {
            wr = wr.queryParam("ip", ip);
        }
        String logMsg = String.format("USEING CRED %s CALLING DELETE %s",authKey,wr.toString());
        LOG.info(logMsg);
        System.out.printf("%s\n", logMsg);
        Invocation.Builder rb = wr.request(MediaType.APPLICATION_XML);
//        rb = rb.type(MediaType.APPLICATION_XML);
        rb.header(authKey, authValue);
        Response resp = rb.delete();
        return resp;
    }

    public Response getDomains() {
        return getDomains(null, null, null);
    }

    private WebTarget addLimitOffsetParams(WebTarget wr, Integer limit, Integer offset) {
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
        String fmt = "{ endPoint=\"%s\",accountId=\"%d\" "
                + "adminEndPoint=\"%s\" adminUser = \"%s\", "
                + "adminPasswd = \"%s\" }";
        String msg = String.format(fmt, endPoint, accountId, adminEndPoint,
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
