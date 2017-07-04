package org.openstack.atlas.api.mgmt.resources;

import java.util.Arrays;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfStrings;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

public class CiphersResource extends ManagementDependencyProvider {

    private int loadBalancerId;

    @POST
    public Response setCiphers(Ciphers ciphers) {
        String cipersStr = ciphers.getCipherList();
        int lid = loadBalancerId;
        org.openstack.atlas.service.domain.entities.LoadBalancer lb = null;
        try {
            lb = loadBalancerRepository.getLoadBalancerIdAccountAndName(lid);
        } catch (EntityNotFoundException ex) {
            String errorMsg = String.format("loadbalancer %d not found", lid);
            return ResponseFactory.getErrorResponse(ex, errorMsg, null);
        }
        try {
            reverseProxyLoadBalancerService.setSslCiphers(lid, loadBalancerId, cipersStr);
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getResponseWithStatus(404, String.format("Loadbalancer with id %d not found", lid), VFAIL);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(ciphers).build();
    }

    @GET
    public Response getCiphers() {
        ListOfStrings cipherList = new ListOfStrings();
        String ciphers;
        int lid = loadBalancerId;
        org.openstack.atlas.service.domain.entities.LoadBalancer lb = null;
        try {
            lb = loadBalancerRepository.getLoadBalancerIdAccountAndName(lid);
        } catch (EntityNotFoundException ex) {
            String errorMsg = String.format("loadbalancer %d not found", lid);
            return ResponseFactory.getErrorResponse(ex, errorMsg, null);
        }
        try {
            ciphers = reverseProxyLoadBalancerService.getSslCiphers(lb.getAccountId(), lb.getId());
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getResponseWithStatus(404, String.format("Loadbalancer with id %d not found", lid), VFAIL);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        cipherList.getStrings().addAll(Arrays.asList(ciphers.split(",")));
        return Response.status(200).entity(cipherList).build();
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
