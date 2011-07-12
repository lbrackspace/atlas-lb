package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.helpers.CheckQueryParams;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

public class LoadBalancersResource extends ManagementDependencyProvider {

    private LoadBalancerResource loadBalancerResource;
    private AccountLimitsResource accountLimitsResource;
    private int accountId;

    private HttpHeaders requestHeaders;

    @Path("{id: [1-9][0-9]*}")
    public LoadBalancerResource getHostResource(@PathParam("id") int id) {
        loadBalancerResource.setId(id);
        return loadBalancerResource;
    }

    @Path("absolutelimits")
    public AccountLimitsResource getAccountLimitsResource() {
        accountLimitsResource.setAccountId(accountId);
        return accountLimitsResource;
    }

    @PUT
    @Path("reassignhosts")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reAssignHosts(LoadBalancers lbs) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult res = ValidatorRepository.getValidatorFor(LoadBalancers.class).validate(lbs, ReassignHostContext.REASSIGN_HOST);
        if (!res.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault",
                    res.getValidationErrorMessages())).build();
        }

        try {
            List<org.openstack.atlas.service.domain.entities.LoadBalancer> domainLoadBalancers = new ArrayList<LoadBalancer>();
            for (org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer lb : lbs.getLoadBalancers()) {
                org.openstack.atlas.service.domain.entities.Host dHost = new org.openstack.atlas.service.domain.entities.Host();
                org.openstack.atlas.service.domain.entities.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entities.LoadBalancer();

                if (lb.getHost() != null) {
                    dHost.setId(lb.getHost().getId());
                }

                loadBalancer.setId(lb.getId());
                loadBalancer.setHost(dHost);
                domainLoadBalancers.add(loadBalancer);
            }

            List<LoadBalancer> validLoadBalancers = loadBalancerService.reassignLoadBalancerHost(domainLoadBalancers);
            EsbRequest req = new EsbRequest();
            req.setLoadBalancers(validLoadBalancers);
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.REASSIGN_LOADBALANCER_HOST, req);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Path("removeoldlimits")
    public Response deleteRateLimitByExpiration() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            LoadBalancer domainLb = new LoadBalancer();
            EsbRequest request = new EsbRequest();
            request.setLoadBalancer(domainLb);

            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.DELETE_OLD_RATE_LIMITS, request);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancers(@QueryParam("vipaddress") String address, @QueryParam("vipid") Integer vipId, @QueryParam("status") String status, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page, @QueryParam("changes-since") Integer time) {
        CheckQueryParams checkParams = new CheckQueryParams();
        Response checkResponseParams = checkParams.checkParams(address, vipId);
        if (checkResponseParams != null) {
            return checkResponseParams;
        } else if (address != null) {
            try {
                return retrieveLoadBalancers(address);
            } catch (Exception ex) {
                return ResponseFactory.getErrorResponse(ex, null, null);
            }
        }

        List<org.openstack.atlas.service.domain.entities.LoadBalancer> domainLbs;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers dataModelLbs = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers();
        if (vipId >= 9000000) {
            domainLbs = virtualIpService.getLoadBalancerByVip6Id(vipId);
        } else {
            domainLbs = virtualIpService.getLoadBalancerByVipId(vipId);
        }

        for (org.openstack.atlas.service.domain.entities.LoadBalancer domainLb : domainLbs) {
            dataModelLbs.getLoadBalancers().add(dozerMapper.map(domainLb, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class, "SIMPLE_VIP_LB"));
        }
        return Response.status(200).entity(dataModelLbs).build();
    }

    private Response retrieveLoadBalancers(String address) {
        List<org.openstack.atlas.service.domain.entities.LoadBalancer> domainLbs;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers dataModelLbs = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers();
        try {
            if (IPUtils.isValidIpv4String(address)) {
                domainLbs = virtualIpService.getLoadBalancerByVipAddress(address);
            } else if (IPUtils.isValidIpv6String(address)) {
                domainLbs = virtualIpService.getLoadBalancerByVip6Address(address);
            } else {
                return Response.status(400).entity("Ip address is invalid").build();
            }


            for (org.openstack.atlas.service.domain.entities.LoadBalancer domainLb : domainLbs) {
                dataModelLbs.getLoadBalancers().add(dozerMapper.map(domainLb, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class, "SIMPLE_VIP_LB"));
            }
        } catch (Exception ex) {
            String msg = getExtendedStackTrace(ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(dataModelLbs).build();
    }

    public void setLoadBalancerResource(LoadBalancerResource loadBalancerResource) {
        this.loadBalancerResource = loadBalancerResource;
    }

    public void setAccountLimitsResource(AccountLimitsResource accountLimitsResource) {
        this.accountLimitsResource = accountLimitsResource;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
