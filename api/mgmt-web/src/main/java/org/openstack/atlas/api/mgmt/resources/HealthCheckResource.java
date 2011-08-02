package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Calendar;

public class HealthCheckResource extends ManagementDependencyProvider {

    @GET
    public Response getHealthCheck() throws EntityNotFoundException {
        Calendar time = Calendar.getInstance();
        ClusterResource clusterResource = new ClusterResource();
        try {
            clusterResource.getClusterRepository().getClusterById(1);
        } catch (EntityNotFoundException e) {
            return Response.status(503).entity(clusterResource).build();
        }

        LoadBalancerResource loadBalancerResource = new LoadBalancerResource();
        try {
            loadBalancerResource.getLoadBalancerRepository().getAllProtocols();
        } catch (Exception e) {
            return Response.status(503).entity(null).build();
        }

        String location = configuration.getString(PublicApiServiceConfigurationKeys.health_check);
        File file = new File(location);
        if (!file.exists()){
            return Response.status(503).entity(configuration).build();
        }

        Integer diff = time.compareTo(Calendar.getInstance());
        return Response.status(200).entity(diff).build();
    }
}