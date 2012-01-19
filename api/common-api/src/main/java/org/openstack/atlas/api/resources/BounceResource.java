package org.openstack.atlas.api.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCertFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtil;

// TODO: Remove this class resource when we go to production
public class BounceResource extends CommonDependencyProvider {

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("loadbalancer")
    public Response echoLoadBalancer(LoadBalancer lb) {
        return Response.status(200).entity(lb).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("virtualip")
    public Response echoVirtualIp(VirtualIp virtualIp) {
        return Response.status(200).entity(virtualIp).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("virtualips")
    public Response echoVirtualIps(VirtualIps virtualIps) {
        return Response.status(200).entity(virtualIps).build();
    }

    @POST
    @Path("connectionthrottle")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoConnectionThrottle(ConnectionThrottle ct) {
        Response resp = Response.status(200).entity(ct).build();
        return resp;
    }

    @POST
    @Path("node")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoNode(Node node) {
        Response resp = Response.status(200).entity(node).build();
        return resp;
    }

    @POST
    @Path("healthmonitor")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoHealthMonitor(HealthMonitor hm) {
        Response resp = Response.status(200).entity(hm).build();
        return resp;
    }

    @POST
    @Path("sessionpersistence")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoSessionPersistence(SessionPersistence sp) {
        Response resp = Response.status(200).entity(sp).build();
        return resp;
    }

    @POST
    @Path("connectionlogging")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoConnectionLogging(ConnectionLogging cl) {
        Response resp = Response.status(200).entity(cl).build();
        return resp;
    }

    @POST
    @Path("nodes")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoNodes(Nodes nodes) {
        Response resp = Response.status(200).entity(nodes).build();
        return resp;
    }

    @POST
    @Path("accesslist")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoAccessList(AccessList accessList) {
        Response resp = Response.status(200).entity(accessList).build();
        return resp;
    }

    @POST
    @Path("updated")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoUpdated(Updated updated) {
        Response resp = Response.status(200).entity(updated).build();
        return resp;
    }

    @POST
    @Path("errorpage")
    public Response echoErrorpage(Errorpage errorpage) {
        Errorpage errorpage_out = new Errorpage();
        errorpage_out.setContent(errorpage.getContent());
        Response resp = Response.status(200).entity(errorpage_out).build();
        return resp;
    }

    @POST
    @Path("ssltermination")
    public Response echoSslTerminationValidation(SslTermination in) {
        String key = in.getPrivatekey();
        String crt = in.getCertificate();
        String chain = in.getIntermediateCertificate();
        Response resp;
        ZeusCertFile zcf = ZeusUtil.getCertFile(key, crt, chain);
        if (zcf.isError()) {
            resp = getValidationFaultResponse(zcf.getErrorList());
        } else {
            resp = ResponseFactory.getSuccessResponse("ssltermination was valid", 200);
        }
        return resp;
    }
}
