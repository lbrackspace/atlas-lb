package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.config.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HealthCheck;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HealthChecks;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;

public class HealthCheckResource extends ManagementDependencyProvider {

    @GET
    public Response getHealthCheck() throws EntityNotFoundException {
        HealthChecks checks = new HealthChecks();

        checks.getHealthChecks().add(zeusHealthCheck());
        checks.getHealthChecks().add(dbHealthCheck());
        checks.getHealthChecks().add(localHealthCheck());

        return Response.status(200).entity(checks).build();
    }

    private HealthCheck zeusHealthCheck() {
        HealthCheck check = new HealthCheck();
        check.setType("ZEUS");
        check.setStatus("ACTIVE");
        Long time = System.currentTimeMillis();
        try {
            reverseProxyLoadBalancerService.getSubnetMappings(hostRepository.getDefaultActiveHost());
        } catch (Exception e) {
            check.setMessage(e.getMessage());
            check.setStatus("INACTIVE");
        }
        check.setTime(System.currentTimeMillis() - time);
        return check;
    }

    private HealthCheck dbHealthCheck() {
        HealthCheck check = new HealthCheck();
        check.setType("DATABASE");
        check.setStatus("ACTIVE");
        Long time = System.currentTimeMillis();
        try {
            loadBalancerRepository.getAllProtocols();
        } catch (Exception e) {
            check.setMessage(e.getMessage());
            check.setStatus("INACTIVE");
        }
        check.setTime(System.currentTimeMillis() - time);
        return check;
    }

    private HealthCheck localHealthCheck() {
        HealthCheck check = new HealthCheck();
        String location = configuration.getString(PublicApiServiceConfigurationKeys.health_check);
        check.setType("LOCAL");
        check.setStatus("ACTIVE");
        File file = new File(location);
        Long time = System.currentTimeMillis();
        try {
            if (!file.exists()) {
                throw new FileNotFoundException("File " + location + " not found.");
            }
        } catch (Exception e) {
            check.setMessage(e.getMessage());
            check.setStatus("INACTIVE");
        }
        check.setTime(System.currentTimeMillis() - time);
        return check;
    }
}