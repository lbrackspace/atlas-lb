package org.openstack.atlas.api.resources;

import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;

import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

// TODO: Remove this class resource when we go to production
public class BounceResource extends CommonDependencyProvider {

    private static final ZeusUtils zeusUtils;

    static {
        zeusUtils = new ZeusUtils();
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("loadbalancer")
    public Response echoLoadBalancer(LoadBalancer lb) {
        return Response.status(200).entity(lb).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("virtualip")
    public Response echoVirtualIp(VirtualIp virtualIp) {
        return Response.status(200).entity(virtualIp).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("virtualips")
    public Response echoVirtualIps(VirtualIps virtualIps) {
        return Response.status(200).entity(virtualIps).build();
    }

    @POST
    @Path("connectionthrottle")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoConnectionThrottle(ConnectionThrottle ct) {
        Response resp = Response.status(200).entity(ct).build();
        return resp;
    }

    @POST
    @Path("node")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoNode(Node node) {
        Response resp = Response.status(200).entity(node).build();
        return resp;
    }

    @POST
    @Path("healthmonitor")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoHealthMonitor(HealthMonitor hm) {
        Response resp = Response.status(200).entity(hm).build();
        return resp;
    }

    @POST
    @Path("sessionpersistence")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoSessionPersistence(SessionPersistence sp) {
        Response resp = Response.status(200).entity(sp).build();
        return resp;
    }

    @POST
    @Path("connectionlogging")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response echoConnectionLogging(ConnectionLogging cl) {
        Response resp = Response.status(200).entity(cl).build();
        return resp;
    }

    @POST
    @Path("nodes")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})

    public Response echoNodes(Nodes nodes) {
        Response resp = Response.status(200).entity(nodes).build();
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("accesslist")
    public Response echoAccessList(AccessList accessList) {
        Response resp = Response.status(200).entity(accessList).build();
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("updated")
    public Response echoUpdated(Updated updated) {
        Response resp = Response.status(200).entity(updated).build();
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("errorpage")
    public Response echoErrorpage(Errorpage errorpage) {
        Errorpage errorpage_out = new Errorpage();
        errorpage_out.setContent(errorpage.getContent());
        Response resp = Response.status(200).entity(errorpage_out).build();
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("certificatemapping")
    public Response CertificateMapping(CertificateMapping certificateMapping) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(CertificateMapping.class).validate(certificateMapping, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }
        Response resp = Response.status(200).entity(certificateMapping).build();
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("ssltermination")
    public Response SslTermination(SslTermination sslTerm) {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.ssl_termination)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(SslTermination.class).validate(sslTerm, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        String key = sslTerm.getPrivatekey();
        String crt = sslTerm.getCertificate();
        String chain = sslTerm.getIntermediateCertificate();
        Response resp;
        ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(key, crt, chain);
        if (zcf.hasFatalErrors()) {
            resp = getValidationFaultResponse(zcf.getFatalErrorList());
        } else {
            SslTermination sslOut = new SslTermination();
            sslOut.setPrivatekey(zcf.getPrivate_key());
            sslOut.setCertificate(zcf.getPublic_cert());

            resp = Response.status(200).entity(sslOut).build();
        }
        return resp;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("sslterminationvalidation")
    public Response SslTerminationValidation(SslTermination sslTerm) {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.ssl_termination)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(SslTermination.class).validate(sslTerm, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }
        SecurityProtocol sp = new SecurityProtocol();
        org.openstack.atlas.service.domain.entities.SslTermination dbSslTerm;
        SslTermination apiSslTerm;
        dbSslTerm = dozerMapper.map(sslTerm, org.openstack.atlas.service.domain.entities.SslTermination.class);
        apiSslTerm = dozerMapper.map(dbSslTerm, SslTermination.class);
        Response resp = Response.status(200).entity(apiSslTerm).build();
        return resp;
    }
}
