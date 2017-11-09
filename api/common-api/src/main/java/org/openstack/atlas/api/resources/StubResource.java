package org.openstack.atlas.api.resources;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import java.util.Calendar;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import java.util.ArrayList;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;

import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;

import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import java.util.List;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.ws.rs.core.Response;

import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cipher;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolName;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.w3.atom.Link;

public class StubResource extends CommonDependencyProvider {

    @GET
    @Path("loadbalancers")
    public Response stubLoadBalancers() {
        LoadBalancers loadbalancers = new LoadBalancers();
        loadbalancers.getLoadBalancers().add(newLoadBalancer(1, "LB1"));
        loadbalancers.getLoadBalancers().add(newLoadBalancer(2, "LB2"));
        List<Link> links = loadbalancers.getLinks();
        Link link = new Link();
        link.setHref("someHref");
        link.setRel("someRel");
        links.add(link);
        link = new Link();
        link.setHref("anotherHref");
        link.setRel("someOtherRel");
        links.add(link);
        return Response.status(200).entity(loadbalancers).build();
    }

    @GET
    @Path("ciphers")
    public Response stubCiphersList(){
        Ciphers ciphers = dozerMapper.map("NES,AES,ZES,DES",Ciphers.class);
        return Response.status(200).entity(ciphers).build();
    }

    @GET
    @Path("loadbalancer")
    public Response stubLoadBalancer() {
        LoadBalancer lb = newLoadBalancer(1, "LB1");
        return Response.status(200).entity(lb).build();
    }

    @GET
    @Path("virtualip")
    public Response stubVirtualIp() {
        VirtualIp virtualIp = newVip(1, "127.0.0.1");
        return Response.status(200).entity(virtualIp).build();
    }

    @GET
    @Path("virtualips")
    public Response stubVirtualIps() {
        VirtualIps vips = new VirtualIps();
        vips.getVirtualIps().add(newVip(1, "127.0.0.1"));
        vips.getVirtualIps().add(newVip(2, "127.0.0.2"));
        return Response.status(200).entity(vips).build();
    }

    @GET
    @Path("connectionthrottle")
    public Response stubConnectionThrottle() {
        ConnectionThrottle ct;
        ct = new org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        return Response.status(200).entity(ct).build();
    }

    @GET
    @Path("node")
    public Response stubNode() {
        Node node;
        node = newNode(64, 80, "127.0.0.1");
        return Response.status(200).entity(node).build();
    }

    @GET
    @Path("uri")
    public Response uriInfo() {
        String uri = getRequestStateContainer().getUriInfo().getAbsolutePath().toString();
        SourceAddresses sa = new SourceAddresses();
        sa.setIpv4Public(uri);
        return Response.status(200).entity(sa).build();
    }

    @GET
    @Path("healthmonitor")
    public Response stubHealthMonitor() {
        HealthMonitor hm;
        hm = new HealthMonitor();
        hm.setAttemptsBeforeDeactivation(10);
        hm.setBodyRegex(".*");
        hm.setDelay(60);
        hm.setId(64);
        hm.setPath("/");
        hm.setStatusRegex(".*");
        hm.setTimeout(100);
        hm.setType(HealthMonitorType.HTTP);
        return Response.status(200).entity(hm).build();
    }

    @GET
    @Path("sessionpersistence")
    public Response stubSessionPersistence() {
        SessionPersistence sp;
        sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        return Response.status(200).entity(sp).build();
    }

    @GET
    @Path("connectionlogging")
    public Response stubConnectionLogging() {
        ConnectionLogging cl;
        cl = new ConnectionLogging();
        cl.setEnabled(Boolean.TRUE);
        return Response.status(200).entity(cl).build();
    }

    @GET
    @Path("nodes")
    public Response stubNodes() {
        List<Node> nodeList;
        Nodes nodes;
        Node node;
        nodes = new Nodes();
        nodeList = nodes.getNodes();
        nodeList.add(newNode(64, 80, "127.0.0.1"));
        nodeList.add(newNode(64, 443, "127.0.0.2"));
        return Response.status(200).entity(nodes).build();
    }

    @GET
    @Path("accesslist")
    public Response stubAccessList() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(newNetworkItem(1, "10.0.0.0/8"));
        al.getNetworkItems().add(newNetworkItem(2, "192.168.0.0/24"));
        return Response.status(200).entity(al).build();
    }

    @GET
    @Path("updated")
    public Response stubUpdated() {
        Calendar now = Calendar.getInstance();
        Updated updated = new Updated();
        updated.setTime(now);
        return Response.status(200).entity(updated).build();
    }

    @GET
    @Path("errorpage")
    public Response stubErrorPage() {
        String format = "<html><big><big><big><big><big><big>%s</big></big></big></big></big></big></html>";
        String msg = String.format(format, "<b>Error or something happened</b>");
        Errorpage errorpage = new Errorpage();
        errorpage.setContent(msg);
        return Response.status(200).entity(errorpage).build();
    }

    @GET()
    @Path("ssltermination")
    public Response stubSslTerm() {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.ssl_termination)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String key = ""
                + "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIJKAIBAAKCAgEAnaf69IQZC4SBDfhwWz5svh6VHOhwaKXIUCBygKf8p8II7pIm\n"
                + "slkwH2CG0T/3fHtT9tfTb/7eANlBOQP5pAYdB8HgudjGCLnSXFjf4sFKJzFHgqOM\n"
                + "ABzMdZLDzSYn1pwR03eYx6aRYqBHdD/MIRTspdU7FLKkTDCkeE/qlbatdBZDVmyI\n"
                + "JjgcwPdPOnHbAN5XmDiELKWtpvkKghyFOaMCanJSGljHIN8ibOvYZUj/QKDaBQyn\n"
                + "1PGxXscIBYvnn5XCEtw5hJoHxTHX3jYNctAbE/251J0VOThK0oqW4zXG1pmivhwz\n"
                + "EoBAIfQ9dc9kxtsvz3TvBi/O84uuh4B2gLoE1AqKlJ2BI96hiUjXnU1mXovj9BcC\n"
                + "0cE9EZWqMsz20MhuvEmLLSANzmKJ07WOcvn+++m706huKndi6gT/2o10ipF9taY1\n"
                + "LQnwAENtTq6E1NTittEeAeoaNm4C9m8DMD8NpUEYnvaZwDZsWgcRUpmlMiwWE5Ru\n"
                + "GnfPzvQOjBxVAJnhkHEyS0hTOupi4c7EW6nc3X3oL0AmmDyZvNmyBDDpQMyDIGv2\n"
                + "l2+W9aj9Es0JeykTYk012z4hVab4/sUmMjviktRzYBgzaFcxBkW2NZtar7JUS2dn\n"
                + "1ejmloaBxHsNDRGWoCiAtgzJ7poUp+CUrrOkoETtmwMBlGT92dWrQA6GawcCAwEA\n"
                + "AQKCAgAEbvvksm5N350NeoYWWswOEKga1wKKPtdCQZdWvOKjCRbdNqj17QIob7t6\n"
                + "2PSpwIIc9/bPOHifx3xJES6NCUr5s98Q+uKezjL3O9yX8N2X+o/LQbQnMKgjSkxN\n"
                + "UZxfMaZirwNR4gJGpsE7qKuh5oe9JiDyNQ/fwKJva7fqG+gG0rV0EbtGb9+HIa1N\n"
                + "tHP3M0l9U2GMK+CVSH2eKRUqCMaBndNnQEXhS8UZEQzV1FaxR5S5/aAeoeleA/Ta\n"
                + "yxNpbnm1tBG+A+LiDcPHUPfR2b5ZMpJuQzicklOwVgtmOlXsJQfplrts8sRa8BZm\n"
                + "YL2xxeozSFOMdf245Z2z2835UsHd9Q32+fHBx/3Oo8ko4qHt7Zg1iuNa32OEwmBP\n"
                + "K3Wp5MGRa6aKpOuQXNP5fJZgpTMwNrBbnkwNXVlM//qFdOcdc/zcEkleHwh/RbDv\n"
                + "dfSzHpc/tvtFVDPnD8gOdnfnygN5tYwGu912JT6v7HkS5skUFi4+7aqNaNe+zJBh\n"
                + "ZFtS/c4pX2wVrcsGhiLSYMdJfceQf3AjvlQcoRSe+a9hCAtY1vUPqUsfJi+3Ddv8\n"
                + "YTzVUP1o3jn57WPswLo61WJa3NIYVAxRl/0/Tb7kfl2oNrv5VAwcBMrt55MuLdEp\n"
                + "OLp1mllQxsVTHBB8p1/7wkultjZxPc6m2zEv0DhKdNcypCoDAQKCAQEA/cmNFUhf\n"
                + "vG0bUmB7CA3dxRfWga9pT5bzkvaey0htRNhBQXqDjHIupa5LjL8Y1g9QgiTn149V\n"
                + "qjV0C7F4RyzKwGTiEkz4iBESHK+bg4tMBfyp7MNppVsu3645dFkI67nRjG5EJc/V\n"
                + "XxAzzFKmYf2eEKhpuw0HwBZfEqTb105LRNiIECU3b2XEF/xfRr7rHSmQdQDuTWjr\n"
                + "86YKzWCKBRss3QYnwUpeB0MbJ1H0Dyb9GJJsPIySY4ufBuG8ZcDbCcOrRFocIMMK\n"
                + "3KMotgbN2YM3bTpfSeK82QdfD/fLJQaKu/GLO0IuwLTCUdidTStpiAOyUAd8aTce\n"
                + "Jyip9hzJg54chwKCAQEAnwfdvBHpQYPvD6KEgT0zMgMaPHEHG5zt01NhekXiRmfs\n"
                + "WMYHsHiUtboDJner3+V43FQQz/GhWe8LZU0SwzbkeSCYzRMR3VKYSFOx7s6aRQ4D\n"
                + "IwBnK2777pM2B880iFodvQlTeACBUKV3mt3fxTNVbOs3rqs8wC0ODJZIZ+42fq9q\n"
                + "Oa1/YELYlnwbhSaZp+r2f0zEbNuLD2kzUz+8pbXJKbPkMtQqx6JwlPh01MY3zbqg\n"
                + "ReITL51VTU53EiOa0U+ADz6uL3B2nw8DTqg9nWw6LUmyNLleKoeaOV+95oekzzJ3\n"
                + "9AlYSyqac8MJkJOiiIiyeJg9vKZYmTeTcvtL/NtdgQKCAQEAoauEsZsiSbGjpw2J\n"
                + "Mq9KqGSwJHsu9iGuVt++drdTzHiK0YCPTqfqaWcn/6g41Rx6Z/3Ep4BKzRwyKcTL\n"
                + "X2P8YSWjEo9v/5YIWLfRtLHHI0U6pnYx1cHJkXq2ZRTW5vu/rtsLlJ7aSS3UIYRB\n"
                + "M8lRqUDv4dXCKy7VL9ZPqc/ZiSj7PHXI47ELg1AlDbdPpYs12CNYq318WgFbfkvS\n"
                + "gMA4CzEBoFOUpMGuCZVeiUyIDOAyDTxrgPiPvN2Om6+ImabJcsiIhKJbSAS0SYj6\n"
                + "F2dMpst5qmLDdOoKN+zdv190f5e233AgwmgkJel9A4z1NE1OiUbLjWcsUTvJUdwy\n"
                + "zyKo/wKCAQAQIcQkZ8y5kKCXfWzjj0m6MQZgSzblXi3h2ftxY9VoPvKCrtPo2tJ6\n"
                + "/LuFE26j76sq7nwmG+S6Mr19MSxOEStr/hqB8wVE5jP8YkEScHLFvn4i9s+AYGm9\n"
                + "8cDxWduCWWHa4y9MZQC5JY/Ubd1dK6/mtJWZalVnSSq7rCL8J/XvM+wanbbmFOHT\n"
                + "ohNIlnnPxs3qa+chA8Q/c/R45WZFiQM278CeR1dvmNLCydFQJCtU+zF25U/87IDS\n"
                + "rrr1ZBc4VFAxO7J/rXDbAbLcL8TQS0I7hdZF8ufSeJ70YvnogKn/OqdgYfJK7a9t\n"
                + "PsOhnthF8VfpU8gvctBZ+oFCkKtMoxQBAoIBAHELzCrBriRcnFjb1uUNcolNRi0Z\n"
                + "GucGpGJA7InjZhGi3v/Jkklh7VXA3EcHC8o7W+hvniY7QFVLsYn8svWvljY9+nNx\n"
                + "OeknmXHVUng74NRSM9SPTlnopaT/4C8+q/jzHiPdiDCJqBH64w70Np37OSMgwvpw\n"
                + "XAEBGy1YRST3UWGX6oZwmE5Pf9FurWk5Ws9TYiE5/rfhrAnIEFFYOO9OEo8PJ48s\n"
                + "75F3pJaYsKq+aGSham/310DpFoxss8yeWs/aqEN+ceIDccncdWXwOosBpk2GLhhQ\n"
                + "SdbDyf8QTSf+xN3ihfUIf5XbB3cna6rdLCPBT2i80kdTlqmihebxthBkgdQ=\n"
                + "-----END RSA PRIVATE KEY-----\n"
                + "";

        String crt = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIGkTCCBHmgAwIBAgIGAVVWR2MaMA0GCSqGSIb3DQEBCwUAMHoxDDAKBgNVBAMT\n"
                + "A0lNRDEbMBkGA1UECxMSQ2xvdWQgTG9hZEJhbGFuY2VyMRowGAYDVQQKExFSYWNr\n"
                + "c3BhY2UgSG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRl\n"
                + "eGFzMQswCQYDVQQGEwJVUzAeFw0xNjA2MTUyMjU2MDZaFw0yNzA4MzEyMjU2MDZa\n"
                + "MIGGMRgwFgYDVQQDEw93d3cucmFja2V4cC5vcmcxGzAZBgNVBAsTEkNsb3VkIExv\n"
                + "YWRCYWxhbmNlcjEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcT\n"
                + "C1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwggIiMA0G\n"
                + "CSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCdp/r0hBkLhIEN+HBbPmy+HpUc6HBo\n"
                + "pchQIHKAp/ynwgjukiayWTAfYIbRP/d8e1P219Nv/t4A2UE5A/mkBh0HweC52MYI\n"
                + "udJcWN/iwUonMUeCo4wAHMx1ksPNJifWnBHTd5jHppFioEd0P8whFOyl1TsUsqRM\n"
                + "MKR4T+qVtq10FkNWbIgmOBzA9086cdsA3leYOIQspa2m+QqCHIU5owJqclIaWMcg\n"
                + "3yJs69hlSP9AoNoFDKfU8bFexwgFi+eflcIS3DmEmgfFMdfeNg1y0BsT/bnUnRU5\n"
                + "OErSipbjNcbWmaK+HDMSgEAh9D11z2TG2y/PdO8GL87zi66HgHaAugTUCoqUnYEj\n"
                + "3qGJSNedTWZei+P0FwLRwT0RlaoyzPbQyG68SYstIA3OYonTtY5y+f776bvTqG4q\n"
                + "d2LqBP/ajXSKkX21pjUtCfAAQ21OroTU1OK20R4B6ho2bgL2bwMwPw2lQRie9pnA\n"
                + "NmxaBxFSmaUyLBYTlG4ad8/O9A6MHFUAmeGQcTJLSFM66mLhzsRbqdzdfegvQCaY\n"
                + "PJm82bIEMOlAzIMga/aXb5b1qP0SzQl7KRNiTTXbPiFVpvj+xSYyO+KS1HNgGDNo\n"
                + "VzEGRbY1m1qvslRLZ2fV6OaWhoHEew0NEZagKIC2DMnumhSn4JSus6SgRO2bAwGU\n"
                + "ZP3Z1atADoZrBwIDAQABo4IBDjCCAQowDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8E\n"
                + "BAMCBLAwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGoBgNVHSME\n"
                + "gaAwgZ2AFBmALcnULZGNnFRkqv22DqOWgoh9oX2kezB5MQswCQYDVQQDEwJDQTEb\n"
                + "MBkGA1UECxMSQ2xvdWQgTG9hZEJhbGFuY2VyMRowGAYDVQQKExFSYWNrc3BhY2Ug\n"
                + "SG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQsw\n"
                + "CQYDVQQGEwJVU4IGAVVWRpO6MB0GA1UdDgQWBBQ2FvpmWgnWiP5TGldjYZ3gyPsE\n"
                + "ITANBgkqhkiG9w0BAQsFAAOCAgEAqcfuim4iiDSNIRseRurff0pjAm4kvvRHGjAU\n"
                + "5S5JXap4DM/nJn7rBE22NVXQbCr0PksmAmPY/bqZKptfQdhT6h8jAImY6zlL4Obc\n"
                + "vQkrnAZjaBDeefYfucgU0GwtwlkUXn5ERIa97Q+Ff/mckemQQJuLIPu5DgvDxE99\n"
                + "AX2fVhBU3YYkdE690TeB45aeEQJIvb8PAM46vTpRxFwLuq+8hQB1Ir0x+LY3IBSA\n"
                + "pL4NE0LkWAbyIwv5tkUFx1mFjjblP0YVaYEbGvQbatHAc7eCDFHxh2TggWer/x/Y\n"
                + "b16TbH1C8H0aEfYU4o/IiMpXFC5mMvLwGfOy/vG+stgxOy2FkEFIRm7yoiZasMrb\n"
                + "BfccM2zjXWfWfG4PwcQ8xqt9ISegfpDNe4k0z8sU22BcdGnwdjZEJ6zBweXnL4bm\n"
                + "vGFQjIxxRn1IqaZk74rVlTkI82IJyGg+iXPJ9qG1QjXLkD/JHtA/xO7aZ8Ij65VY\n"
                + "9WWhWpjbjxCvTLQIKGW58tu5N/qlDHNr5DcSsjq7Nf0OFgaxPe03p3B5x3V8VRyN\n"
                + "CzlgPauRTtm+mB8vjKnA0F4HFyVsGsdMMWAR4tvPUluXRNkh+V5gb8FbscL2sbu9\n"
                + "WAbSVtgKkUe7/DPPuF09L3Gubq0pwHW7SoS2edSepBbqFqT0eNXrlAGiWAwhDpq3\n"
                + "NbAQvJ4=\n"
                + "-----END CERTIFICATE-----\n"
                + "";

        String imd = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIGgTCCBGmgAwIBAgIGAVVWRpO6MA0GCSqGSIb3DQEBCwUAMHkxCzAJBgNVBAMT\n"
                + "AkNBMRswGQYDVQQLExJDbG91ZCBMb2FkQmFsYW5jZXIxGjAYBgNVBAoTEVJhY2tz\n"
                + "cGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
                + "YXMxCzAJBgNVBAYTAlVTMB4XDTE2MDYxNTIyNTUxM1oXDTI3MDkwMTIyNTUxM1ow\n"
                + "ejEMMAoGA1UEAxMDSU1EMRswGQYDVQQLExJDbG91ZCBMb2FkQmFsYW5jZXIxGjAY\n"
                + "BgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEO\n"
                + "MAwGA1UECBMFVGV4YXMxCzAJBgNVBAYTAlVTMIICIjANBgkqhkiG9w0BAQEFAAOC\n"
                + "Ag8AMIICCgKCAgEAqrSzGbLwNx/KRj5f9EIprvohdrWV/HHF6gTM/Ph26GwtacAb\n"
                + "A7P6IpZMxRvRYYHLsaf+KLhMBx6g0mLoOwLAzsJN6eP0HKptZ7T5uR3XWv620FqP\n"
                + "jEwg+yuOB7wbQbQYYA53di9sbr6YQjAfutFWSuyebv7klYnDRp893VhqIGA5c8tD\n"
                + "o4Lpu2RGDs0oZoXOqSzZXxlAbUnufF2fkDUiIPiPlrK5QcquqW5ooxkRdIwGKvHl\n"
                + "+OlwyGdVmxUJ4N07/wz4ca1txkwx9PHPe7Qh9k9BAyytybh87SBg6KvFhrcHSXuv\n"
                + "MdWuTWiKtXpQs6qoZuoWPp5b4KkWxq9YP7njMoe8ONSQ+fiJw4GVUBD2gh0m3YOo\n"
                + "/liHZyoEH2aHX9NqscDamLti0/pKIHYFvTsbuEPMVMNVBRoIRKcwUZRuXoTruOSx\n"
                + "mbG+o4w/VHBTJGY6elvNRq36H3p3PiV0wxDdXYlTyO5Jsn+kDB5f5IHRTkTrx06u\n"
                + "uv65mq3Hco8jPUaU/mHa5CVsPMSjeW/aGxDPZ5VeumER+RsobRSZtTP5+SLQ0iIx\n"
                + "uuRuAsZ3FX7mN5m4X1kyuzgG7C2dD0MfPHPR2NWjRSNcQws1NBsbhE9crd1wm5Pc\n"
                + "fHYD3EL/7+bLZ9kPfP1iPTU6pV7ncWbQqa6BUTO1WxsGN0A6mIPVhm6TiJkCAwEA\n"
                + "AaOCAQwwggEIMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgK0MCAGA1Ud\n"
                + "JQEB/wQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjCBowYDVR0jBIGbMIGYgBStGewq\n"
                + "ibdL3DvzARt5hrQwVP06O6F9pHsweTELMAkGA1UEAxMCQ0ExGzAZBgNVBAsTEkNs\n"
                + "b3VkIExvYWRCYWxhbmNlcjEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDAS\n"
                + "BgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVOC\n"
                + "AQEwHQYDVR0OBBYEFBmALcnULZGNnFRkqv22DqOWgoh9MA0GCSqGSIb3DQEBCwUA\n"
                + "A4ICAQAMIl3lwc6DjQ7V/WQDpPLyaKmkA7xUThx1HOPO/jOGbth0oHWgrGrjL+IX\n"
                + "SIead3+SElngibg69RLQHSIa+ESbuNn/5u+wa7cfrrXDmFmy+q6TSwZ9xUhdDg3n\n"
                + "VZxs4JgS+TWsRkto0GR5OoVB8OCUs/r2wYMHSrYaYQWjW9f9Cttiig+Adhz1YtrR\n"
                + "5yIyISxmukQ1fNHeKbGFEsuRKBdAPXAJxgjzlhZH268HfwHV4VLIzc6c6BaJQNah\n"
                + "1E+3c9AKL4gSaiToqbFp3CU5/zzeu2VgKjCkSlJLvF3L7dw3Rq2O7Fhep+3fTbCe\n"
                + "/WtPk2pdmbnJEn1df9FCqyqxQeslNnjY4MAcbKD+a/4oA8/c68Jw2pIaYxzPLBMJ\n"
                + "BALbLATZYocvJMZQaDM0n+9esTcFr4P/fy4Vz99h+Mj7XoBfsPyV5n/nFO19ZqiM\n"
                + "R7E3natI6sbP5Wlk77AjH/zm9ye/ZtUVxnRFBrhb/I5M+nkSoUFvJSUmAm+Ry0lc\n"
                + "4fDWcrgHVmZVA+y9n7CSOKcNRSCQIo8X9EQdPgYsmpMf0WUgYSbxgGLN5HwM3tCY\n"
                + "aHhZvyJXlEdW7siLZ/gmRruR0g4udh3Mmj7RjjE9zDQQNsbAGNT2gsyGxwRcr7c8\n"
                + "yxnoyJ1KUGhWzS0AyXkA2d/nctHrNGlx5mxFzDyCP/ZOvuSxeg==\n"
                + "-----END CERTIFICATE-----\n"
                + "";


        SslTermination sslTermination = new SslTermination();
        sslTermination.setCertificate(crt);
        sslTermination.setPrivatekey(key);
        sslTermination.setIntermediateCertificate(imd);
        sslTermination.setEnabled(true);
        sslTermination.setSecurePort(443);
        sslTermination.setSecureTrafficOnly(false);
        SecurityProtocol protocol = new SecurityProtocol();
        protocol.setSecurityProtocolName(SecurityProtocolName.TLS_10);
        protocol.setSecurityProtocolStatus(SecurityProtocolStatus.DISABLED);
        sslTermination.getSecurityProtocols().add(protocol);
        return Response.status(Response.Status.OK).entity(sslTermination).build();
    }

    private Node newNode(Integer id, Integer port, String address) {
        Node node;
        node = new Node();
        node.setAddress(address);
        node.setCondition(NodeCondition.ENABLED);
        node.setId(id);
        node.setPort(port);
        node.setStatus(NodeStatus.ONLINE);
        node.setWeight(1);
        return node;
    }

    private VirtualIp newVip(Integer id, String address) {
        VirtualIp vip;
        vip = new VirtualIp();
        vip.setId(id);
        vip.setAddress(address);
        vip.setIpVersion(IpVersion.IPV4);
        vip.setType(VipType.PUBLIC);
        return vip;
    }

    private NetworkItem newNetworkItem(Integer id, String address) {
        NetworkItem n = new NetworkItem();
        n.setId(id);
        n.setAddress(address);
        n.setIpVersion(IpVersion.IPV4);
        n.setType(NetworkItemType.DENY);
        return n;
    }

    private LoadBalancer newLoadBalancer(Integer id, String name) {
        List<Node> nodes = new ArrayList<Node>();
        List<VirtualIp> vips = new ArrayList<VirtualIp>();
        List<NetworkItem> accessList = new ArrayList<NetworkItem>();
        LoadBalancer lb = new LoadBalancer();
        Created created = new Created();
        Updated updated = new Updated();
        created.setTime(Calendar.getInstance());
        updated.setTime(Calendar.getInstance());
        ConnectionThrottle ct = new ConnectionThrottle();
        Cluster cl = new Cluster();
        ConnectionLogging cnl = new ConnectionLogging();
        cnl.setEnabled(Boolean.TRUE);
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        cl.setName("TestCluster");
        lb.setName(name);
        lb.setAlgorithm("RANDOM");
        lb.setCluster(cl);
        lb.setConnectionLogging(cnl);
        lb.setConnectionThrottle(ct);
        lb.setPort(80);
        lb.setProtocol("HTTP");
        lb.setStatus("BUILD");
        lb.setCreated(created);
        lb.setUpdated(updated);
        nodes.add(newNode(1, 80, "127.0.0.10"));
        nodes.add(newNode(1, 443, "127.0.0.20"));
        vips.add(newVip(1, "127.0.0.1"));
        vips.add(newVip(2, "127.0.0.2"));
        lb.setVirtualIps(vips);
        lb.setNodes(nodes);
        SessionPersistence sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        lb.setSessionPersistence(sp);
        accessList.add(newNetworkItem(1, "10.0.0.0/8"));
        accessList.add(newNetworkItem(2, "192.168.0.0/24"));
        lb.setAccessList(accessList);
        LoadBalancerUsage lu = new LoadBalancerUsage();
        lu.setLoadBalancerId(id);
        lu.setLoadBalancerName(name);
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(1));
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(2));
        lb.setLoadBalancerUsage(lu);
        return lb;
    }

    private LoadBalancerUsageRecord newLoadBalancerUsageRecord(Integer id) {
        LoadBalancerUsageRecord ur = new LoadBalancerUsageRecord();
        ur.setAverageNumConnections(3.0);
        ur.setId(id);
        ur.setEventType("EmptyEvent");
        ur.setIncomingTransfer(new Long(20));
        ur.setNumPolls(50);
        ur.setNumVips(30);
        ur.setOutgoingTransfer(new Long(30));
        ur.setEndTime(Calendar.getInstance());
        return ur;
    }
}
