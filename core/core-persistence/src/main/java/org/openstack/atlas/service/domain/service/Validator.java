package org.openstack.atlas.service.domain.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entity.HealthMonitorType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.exception.BadRequestException;

public class Validator {
    private static final Log LOG = LogFactory.getLog(Validator.class);

    private Validator() {

    }

    public static void verifyTCPProtocolandPort(final LoadBalancer loadBalancer) throws BadRequestException {
        if (loadBalancer.getProtocol() != null && (loadBalancer.getProtocol().equals(LoadBalancerProtocol.TCP))) {
            LOG.info("TCP Protocol detected. Port must exists");
            if (loadBalancer.getPort() == null) {
                throw new BadRequestException("Must Provide port for TCP Protocol.");
            }
        }
    }

    public static LoadBalancer verifyProtocolAndHealthMonitorType(LoadBalancer loadBalancer) throws BadRequestException {
        if (loadBalancer.getHealthMonitor() == null || loadBalancer.getHealthMonitor().getType() == null) {
            return loadBalancer;
        }
        LOG.info("Health Monitor detected. Verifying that the load balancer's protocol matches the monitor type.");
        HealthMonitorType type = loadBalancer.getHealthMonitor().getType();
        if (type.equals(HealthMonitorType.HTTP)) {
            if (!(loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTP))) {
                throw new BadRequestException("Protocol must be HTTP for an HTTP health monitor.");
            }
        } else if (type.equals(HealthMonitorType.HTTPS)) {
            if (!(loadBalancer.getProtocol().equals(LoadBalancerProtocol.HTTPS))) {
                throw new BadRequestException("Protocol must be HTTPS for an HTTPS health monitor.");
            }
        }
        return loadBalancer;
    }

}
