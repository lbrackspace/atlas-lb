package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

// TODO: Undocumented Resource
public class BounceResource extends ManagementDependencyProvider {

    private static final ZeusUtils zeusUtils;

    static {
        zeusUtils = new ZeusUtils();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("ratelimit")
    public Response echoRateLimit(RateLimit lb) {
        return Response.status(200).entity(lb).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("host")
    public Response echoHost(Host host) {
        return Response.status(200).entity(host).build();
    }

    @POST
    @Path("virtualipblocks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoVirtualIpBlocks(VirtualIpBlocks virtualIpBlocks) {
        return Response.status(200).entity(virtualIpBlocks).build();
    }

    @POST
    @Path("subnetmappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoSubnetMapptings(Hostssubnet hostssubnet) {
        Response resp = Response.status(200).entity(hostssubnet).build();
        return resp;
    }

    @POST
    @Path("byidorname")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoByIdOrName(ByIdOrName byIdOrName) {
        Response resp = Response.status(200).entity(byIdOrName).build();
        return resp;
    }

    @POST
    @Path("virtualip")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoVirtualIp(VirtualIp virtualIp) {
        Response resp = Response.status(200).entity(virtualIp).build();
        return resp;
    }

    @POST
    @Path("alloweddomain")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoAllowedDomain(AllowedDomain ad) {
        Response resp = Response.status(200).entity(ad).build();
        return resp;
    }

    @POST
    @Path("loadbalancers")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoLoadBalancer(LoadBalancers lbs) {
        Response resp = Response.status(200).entity(lbs).build();
        return resp;
    }

    @POST
    @Path("ssltermination")
    public Response echoSslTermination(org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination sslTerm) {
        String key = sslTerm.getPrivatekey();
        String crt = sslTerm.getCertificate();
        String chain = sslTerm.getIntermediateCertificate();
        Response resp;
        ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(key, crt, chain);
        if (zcf.hasFatalErrors()) {
            resp = getValidationFaultResponse(zcf.getFatalErrorList());
        } else {
            resp = Response.status(200).entity(sslTerm).build();
        }
        return resp;
    }
}
