package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Suspension;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class LoadBalancerSuspensionResource extends ManagementDependencyProvider {

    private int id;
    private int loadBalancerId;

    @GET
    public Response LoadbalancerSuspensionResource() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        Suspension domainSuspension;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension apiSuspension;
        try {
            domainSuspension = getLoadBalancerRepository().getSuspensionByLbIdAndAccountId(loadBalancerId);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension rsp = null;
            if (domainSuspension.getId() != null) {
                rsp = getDozerMapper().map(domainSuspension, org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension.class, "SIMPLE_SUSPENSION");
            } else {
                rsp = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension();
            }

            return Response.status(200).entity(rsp).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createLoadBalancerSuspension(org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension lbsr) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension.class).validate(lbsr, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }
        try {

            org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            apiLb.setSuspension(lbsr);

            LoadBalancer domainLb = getDozerMapper().map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);

            suspensionService.createSuspension(domainLb);

            EsbRequest request = new EsbRequest();
            request.setLoadBalancer(domainLb);

            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.CREATE_SUSPENSION, request);

           return Response.status(202).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response removeSuspension() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            getLoadBalancerRepository().getSuspensionByLbIdAndAccountId(loadBalancerId);

            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setId(loadBalancerId);

            suspensionService.deleteSuspension(loadBalancer);
            //create requestObject
            EsbRequest req = new EsbRequest();
            req.setLoadBalancer(loadBalancer);

            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.REMOVE_SUSPENSION, req);

            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
