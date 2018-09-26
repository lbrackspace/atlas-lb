package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.VirtualIpv6;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.VIPS_POST;

public class LoadbalancerVipResource extends ManagementDependencyProvider {

    private int loadBalancerId;
    private int id;

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addVirtualIpToLoadBalancer(VirtualIp vip) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(VirtualIp.class).validate(vip, VIPS_POST);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault",
                    result.getValidationErrorMessages())).build();
        }
        try {
            // TODO: Refactor to use better dozer mappings.
            org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer apiLb = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer();
            apiLb.getVirtualIps().add(vip);
            LoadBalancer domainLb = getDozerMapper().map(apiLb, LoadBalancer.class);
            domainLb.setId(loadBalancerId);

            final org.openstack.atlas.service.domain.entities.VirtualIp virtualIpToAdd = domainLb.getLoadBalancerJoinVipSet().iterator().next().getVirtualIp();
            final org.openstack.atlas.service.domain.entities.VirtualIp newlyAddedVip = virtualIpService.addVirtualIpToLoadBalancer(virtualIpToAdd, domainLb, virtualIpToAdd.getTicket());

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.getNewVipIds().add(newlyAddedVip.getId());

            managementAsyncService.callAsyncLoadBalancingOperation(Operation.ADD_VIRTUAL_IP, dataContainer);
            
            VirtualIp returnVip = getDozerMapper().map(newlyAddedVip, VirtualIp.class);
            return Response.status(Response.Status.ACCEPTED).entity(returnVip).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    public Response getVipsbyLoadBalancerId() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        List<org.openstack.atlas.service.domain.entities.VirtualIp> dvips;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps rvips = new VirtualIps();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp rvip;
        try {
            dvips = getVipRepository().getVipsByLoadBalancerId(loadBalancerId);

            for (org.openstack.atlas.service.domain.entities.VirtualIp dvip : dvips) {
                rvip = getDozerMapper().map(dvip, VirtualIp.class);
                rvips.getVirtualIps().add(rvip);
            }

            Set<VirtualIpv6> ipv6Vips = virtualIpService.getVirtualIpv6ByLoadBalancerId(loadBalancerId);
            for (org.openstack.atlas.service.domain.entities.VirtualIpv6 ipv6Vip : ipv6Vips) {
                org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp apiIpv6Vip;
                apiIpv6Vip = new org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp();
                apiIpv6Vip.setId(ipv6Vip.getId());
                apiIpv6Vip.setType(VipType.PUBLIC);
                apiIpv6Vip.setIpVersion(IpVersion.IPV6);
                apiIpv6Vip.setAddress(virtualIpService.getVirtualIpv6String(ipv6Vip));
                rvips.getVirtualIps().add(apiIpv6Vip);
            }
            return Response.status(200).entity(rvips).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }
}
