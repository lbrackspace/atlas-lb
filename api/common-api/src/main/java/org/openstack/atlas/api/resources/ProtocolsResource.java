package org.openstack.atlas.api.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

public class ProtocolsResource extends CommonDependencyProvider {

    @GET
    public Response retrieveLoadBalancingProtocols() {
        List <org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject> lbProtObjects;
        org.openstack.atlas.docs.loadbalancers.api.v1.Protocols rProts = new org.openstack.atlas.docs.loadbalancers.api.v1.Protocols();

        try {
            lbProtObjects = protocolsService.get();
            for(org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject lbProtObject : lbProtObjects) {
                rProts.getProtocols().add(dozerMapper.map(lbProtObject,org.openstack.atlas.docs.loadbalancers.api.v1.Protocol.class));
            }
            return Response.status(200).entity(rProts).build();
        }catch(Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
