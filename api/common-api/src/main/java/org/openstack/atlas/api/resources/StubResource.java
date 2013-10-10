package org.openstack.atlas.api.resources;

import java.math.BigInteger;
import java.util.Map;
import javax.xml.namespace.QName;
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
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.V1StubFactory;
import org.w3.atom.Link;

public class StubResource extends CommonDependencyProvider {

    @GET
    @Path("atomlink")
    public Response stubAtomLink() {
        Link atomLink = new Link();
        atomLink.setBase("SomeBase");
        atomLink.setContent("SomeContent");
        atomLink.setHref("SomeHref");
        atomLink.setHreflang("SomeLangRef");
        atomLink.setLang("SomeLang");
        atomLink.setLength(new BigInteger("10000000000000000000"));
        atomLink.setRel("somRel");
        atomLink.setTitle("someTitle");
        atomLink.setType("someType");
        atomLink.getOtherAttributes().put(new QName("someNSURI", "SomelocalPart", "somePrefix"), "SomeAttr");
        atomLink.getOtherAttributes().put(new QName("anotherNSURI", "anotherLocalPart", "AnoterPrefix"), "AnotherAttr");
        return Response.status(200).entity(atomLink).build();
    }

    @GET
    @Path("loadbalancers")
    public Response stubLoadBalancers() {
        LoadBalancers loadbalancers = new LoadBalancers();
        loadbalancers.getLoadBalancers().add(V1StubFactory.newLoadBalancer(1, "LB1"));
        loadbalancers.getLoadBalancers().add(V1StubFactory.newLoadBalancer(2, "LB2"));
        loadbalancers.getLinks().add(V1StubFactory.makeLink("someHref", "somRel"));
        return Response.status(200).entity(loadbalancers).build();
    }

    @GET
    @Path("loadbalancer")
    public Response stubLoadBalancer() {
        LoadBalancer lb = V1StubFactory.newLoadBalancer(1, "LB1");
        return Response.status(200).entity(lb).build();
    }

    @GET
    @Path("virtualip")
    public Response stubVirtualIp() {
        VirtualIp virtualIp = V1StubFactory.newVip(1, "127.0.0.1");
        return Response.status(200).entity(virtualIp).build();
    }

    @GET
    @Path("virtualips")
    public Response stubVirtualIps() {
        VirtualIps vips = V1StubFactory.newVirtualIps(3, 3);
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
        node = V1StubFactory.newNode(64, 80, "127.0.0.1");
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
        Nodes nodes;
        nodes = new Nodes();
        nodes.getNodes().add(V1StubFactory.newNode(1, 80, "127.0.0.1"));
        nodes.getNodes().add(V1StubFactory.newNode(2, 443, "127.0.0.2"));
        nodes.getNodes().add(V1StubFactory.newNode(3, 8080, "127.0.0.3"));
        nodes.getLinks().add(V1StubFactory.makeLink("href1", "prev"));
        nodes.getLinks().add(V1StubFactory.makeLink("href2", "self"));
        nodes.getLinks().add(V1StubFactory.makeLink("href3", "next"));
        return Response.status(200).entity(nodes).build();
    }

    @GET
    @Path("accesslist")
    public Response stubAccessList() {
        AccessList al = V1StubFactory.newAccessList();
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
                + "MIIEpAIBAAKCAQEAwIudSMpRZx7TS0/AtDVX3DgXwLD9g+XrNaoazlhwhpYALgzJ\n"
                + "LAbAnOxT6OT0gTpkPus/B7QhW6y6Auf2cdBeW31XoIwPsSoyNhxgErGBxzNARRB9\n"
                + "lI1HCa1ojFrcULluj4W6rpaOycI5soDBJiJHin/hbZBPZq6vhPCuNP7Ya48Zd/2X\n"
                + "CQ9ft3XKfmbs1SdrdROIhigse/SGRbMrCorn/vhNIuohr7yOlHG3GcVcUI9k6ZSZ\n"
                + "BbqF+ZA4ApSF/Q6/cumieEgofhkYbx5fg02s9Jwr4IWnIR2bSHs7UQ6sVgKYzjs7\n"
                + "Pd3Unpa74jFw6/H6shABoO2CIYLotGmQbFgnpwIDAQABAoIBAQCBCQ+PCIclJHNV\n"
                + "tUzfeCA5ZR4F9JbxHdRTUnxEbOB8UWotckQfTScoAvj4yvdQ42DrCZxj/UOdvFOs\n"
                + "PufZvlp91bIz1alugWjE+p8n5+2hIaegoTyHoWZKBfxak0myj5KYfHZvKlbmv1ML\n"
                + "XV4TwEVRfAIG+v87QTY/UUxuF5vR+BpKIbgUJLfPUFFvJUdl84qsJ44pToxaYUd/\n"
                + "h5YAGC00U4ay1KVSAUnTkkPNZ0lPG/rWU6w6WcTvNRLMd8DzFLTKLOgQfHhbExAF\n"
                + "+sXPWjWSzbBRP1O7fHqq96QQh4VFiY/7w9W+sDKQyV6Ul17OSXs6aZ4f+lq4rJTI\n"
                + "1FG96YiBAoGBAO1tiH0h1oWDBYfJB3KJJ6CQQsDGwtHo/DEgznFVP4XwEVbZ98Ha\n"
                + "BfBCn3sAybbaikyCV1Hwj7kfHMZPDHbrcUSFX7quu/2zPK+wO3lZKXSyu4YsguSa\n"
                + "RedInN33PpdnlPhLyQdWSuD5sVHJDF6xn22vlyxeILH3ooLg2WOFMPmVAoGBAM+b\n"
                + "UG/a7iyfpAQKYyuFAsXz6SeFaDY+ZYeX45L112H8Pu+Ie/qzon+bzLB9FIH8GP6+\n"
                + "QpQgmm/p37U2gD1zChUv7iW6OfQBKk9rWvMpfRF6d7YHquElejhizfTZ+ntBV/VY\n"
                + "dOYEczxhrdW7keLpatYaaWUy/VboRZmlz/9JGqVLAoGAHfqNmFc0cgk4IowEj7a3\n"
                + "tTNh6ltub/i+FynwRykfazcDyXaeLPDtfQe8gVh5H8h6W+y9P9BjJVnDVVrX1RAn\n"
                + "biJ1EupLPF5sVDapW8ohTOXgfbGTGXBNUUW+4Nv+IDno+mz/RhjkPYHpnM0I7c/5\n"
                + "tGzOZsC/2hjNgT8I0+MWav0CgYEAuULdJeQVlKalI6HtW2Gn1uRRVJ49H+LQkY6e\n"
                + "W3+cw2jo9LI0CMWSphNvNrN3wIMp/vHj0fHCP0pSApDvIWbuQXfzKaGko7UCf7rK\n"
                + "f6GvZRCHkV4IREBAb97j8bMvThxClMNqFfU0rFZyXP+0MOyhFQyertswrgQ6T+Fi\n"
                + "2mnvKD8CgYAmJHP3NTDRMoMRyAzonJ6nEaGUbAgNmivTaUWMe0+leCvAdwD89gzC\n"
                + "TKbm3eDUg/6Va3X6ANh3wsfIOe4RXXxcbcFDk9R4zO2M5gfLSjYm5Q87EBZ2hrdj\n"
                + "M2gLI7dt6thx0J8lR8xRHBEMrVBdgwp0g1gQzo5dAV88/kpkZVps8Q==\n"
                + "-----END RSA PRIVATE KEY-----\n";
        String crt = "-----BEGIN CERTIFICATE-----\n"
                + "MIIEXTCCA0WgAwIBAgIGATTEAjK3MA0GCSqGSIb3DQEBBQUAMIGDMRkwFwYDVQQD\n"
                + "ExBUZXN0IENBIFNUdWIgS2V5MRcwFQYDVQQLEw5QbGF0Zm9ybSBMYmFhczEaMBgG\n"
                + "A1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcTC1NhbiBBbnRvbmlvMQ4w\n"
                + "DAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwHhcNMTIwMTA5MTk0NjQ1WhcNMTQw\n"
                + "MTA4MTk0NjQ1WjCBgjELMAkGA1UEBhMCVVMxDjAMBgNVBAgTBVRleGFzMRQwEgYD\n"
                + "VQQHEwtTYW4gQW50b25pbzEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFzAV\n"
                + "BgNVBAsTDlBsYXRmb3JtIExiYWFzMRgwFgYDVQQDEw9UZXN0IENsaWVudCBLZXkw\n"
                + "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDAi51IylFnHtNLT8C0NVfc\n"
                + "OBfAsP2D5es1qhrOWHCGlgAuDMksBsCc7FPo5PSBOmQ+6z8HtCFbrLoC5/Zx0F5b\n"
                + "fVegjA+xKjI2HGASsYHHM0BFEH2UjUcJrWiMWtxQuW6Phbqulo7JwjmygMEmIkeK\n"
                + "f+FtkE9mrq+E8K40/thrjxl3/ZcJD1+3dcp+ZuzVJ2t1E4iGKCx79IZFsysKiuf+\n"
                + "+E0i6iGvvI6UcbcZxVxQj2TplJkFuoX5kDgClIX9Dr9y6aJ4SCh+GRhvHl+DTaz0\n"
                + "nCvghachHZtIeztRDqxWApjOOzs93dSelrviMXDr8fqyEAGg7YIhgui0aZBsWCen\n"
                + "AgMBAAGjgdUwgdIwgbAGA1UdIwSBqDCBpYAUNpx1Pc6cGA7KqEwHMmHBTZMA7lSh\n"
                + "gYmkgYYwgYMxGTAXBgNVBAMTEFRlc3QgQ0EgU1R1YiBLZXkxFzAVBgNVBAsTDlBs\n"
                + "YXRmb3JtIExiYWFzMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UE\n"
                + "BxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVU4IBATAd\n"
                + "BgNVHQ4EFgQULueOfsjZZOHwJHZwBy6u0swnpccwDQYJKoZIhvcNAQEFBQADggEB\n"
                + "AFNuqSVUaotUJoWDv4z7Kbi6JFpTjDht5ORw4BdVYlRD4h9DACAFzPrPV2ym/Osp\n"
                + "hNMdZq6msZku7MdOSQVhdeGWrSNk3M8O9Hg7cVzPNXOF3iNoo3irQ5tURut44xs4\n"
                + "Ww5YWQqS9WyUY5snD8tm7Y1rQTPfhg+678xIq/zWCv/u+FSnfVv1nlhLVQkEeG/Y\n"
                + "gh1uMaTIpUKTGEjIAGtpGP7wwIcXptR/HyfzhTUSTaWc1Ef7zoKT9LL5z3IV1hC2\n"
                + "jVWz+RwYs98LjMuksJFoHqRfWyYhCIym0jb6GTwaEmpxAjc+d7OLNQdnoEGoUYGP\n"
                + "YjtfkRYg265ESMA+Kww4Xy8=\n"
                + "-----END CERTIFICATE-----\n";
        String imd = "-----BEGIN CERTIFICATE-----\n"
                + "MIIDtTCCAp2gAwIBAgIBATANBgkqhkiG9w0BAQUFADCBgzEZMBcGA1UEAxMQVGVz\n"
                + "dCBDQSBTVHViIEtleTEXMBUGA1UECxMOUGxhdGZvcm0gTGJhYXMxGjAYBgNVBAoT\n"
                + "EVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UE\n"
                + "CBMFVGV4YXMxCzAJBgNVBAYTAlVTMB4XDTEyMDEwOTE5NDU0OVoXDTE0MDEwODE5\n"
                + "NDU0OVowgYMxGTAXBgNVBAMTEFRlc3QgQ0EgU1R1YiBLZXkxFzAVBgNVBAsTDlBs\n"
                + "YXRmb3JtIExiYWFzMRowGAYDVQQKExFSYWNrc3BhY2UgSG9zdGluZzEUMBIGA1UE\n"
                + "BxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQswCQYDVQQGEwJVUzCCASIw\n"
                + "DQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNh55lwTVwQvNoEZjq1zGdYz9jA\n"
                + "XXdjizn8AJhjHLOAallfPtvCfTEgKanhdoyz5FnhQE8HbDAop/KNS1lN2UMvdl5f\n"
                + "ZNLTSjJrNtedqxQwxN/i3bpyBxNVejUH2NjV1mmyj+5CJYwCzWalvI/gLPq/A3as\n"
                + "O2EQqtf3U8unRgn0zXLRdYxV9MrUzNAmdipPNvNrsVdrCgA42rgF/8KsyRVQfJCX\n"
                + "fN7PGCfrsC3YaUvhymraWxNnXIzMYTNa9wEeBZLUw8SlEtpa1Zsvui+TPXu3USNZ\n"
                + "VnWH8Lb6ENlnoX0VBwo62fjOG3JzhNKoJawi3bRqyDdINOvafr7iPrrs/T8CAwEA\n"
                + "AaMyMDAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUNpx1Pc6cGA7KqEwHMmHB\n"
                + "TZMA7lQwDQYJKoZIhvcNAQEFBQADggEBAMoRgH3iTG3t317viLKoY+lNMHUgHuR7\n"
                + "b3mn9MidJKyYVewe6hCDIN6WY4fUojmMW9wFJWJIo0hRMNHL3n3tq8HP2j20Mxy8\n"
                + "acPdfGZJa+jiBw72CrIGdobKaFduIlIEDBA1pNdZIJ+EulrtqrMesnIt92WaypIS\n"
                + "8JycbIgDMCiyC0ENHEk8UWlC6429c7OZAsplMTbHME/1R4btxjkdfrYZJjdJ2yL2\n"
                + "8cjZDUDMCPTdW/ycP07Gkq30RB5tACB5aZdaCn2YaKC8FsEdhff4X7xEOfOEHWEq\n"
                + "SRxADDj8Lx1MT6QpR07hCiDyHfTCtbqzI0iGjX63Oh7xXSa0f+JVTa8=\n"
                + "-----END CERTIFICATE-----\n";
        SslTermination sslTermination = new SslTermination();
        sslTermination.setCertificate(crt);
        sslTermination.setPrivatekey(key);
        sslTermination.setIntermediateCertificate(imd);
        sslTermination.setEnabled(true);
        sslTermination.setSecurePort(443);
        sslTermination.setSecureTrafficOnly(false);
        return Response.status(Response.Status.OK).entity(sslTermination).build();
    }
}
