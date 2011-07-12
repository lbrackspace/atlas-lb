package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.services.AlgorithmsService;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import java.util.List;

public class AlgorithmsResource extends CommonDependencyProvider {

    @GET
    public Response retrieveLoadBalancingAlgorithms() {
        org.openstack.atlas.docs.loadbalancers.api.v1.Algorithms ralgos = new org.openstack.atlas.docs.loadbalancers.api.v1.Algorithms();
        List<org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject> dalgos;
        try {
//            dalgos = lbRepository.getAllAlgorithms();
            dalgos = algorithmsService.get();
            for (org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject dalgo : dalgos) {
                ralgos.getAlgorithms().add(dozerMapper.map(dalgo, org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm.class));
            }
            return Response.status(200).entity(ralgos).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
