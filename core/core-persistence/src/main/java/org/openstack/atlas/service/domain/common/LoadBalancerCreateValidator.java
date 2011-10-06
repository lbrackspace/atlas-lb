package org.openstack.atlas.service.domain.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.BadRequestException;

public class LoadBalancerCreateValidator {
    private static final Log LOG = LogFactory.getLog(LoadBalancerCreateValidator.class);

    private LoadBalancerCreateValidator() {

    }

    public static void verifyTCPProtocolandPort(final LoadBalancer loadBalancer) throws BadRequestException {
        if (loadBalancer.getProtocol() != null && (loadBalancer.getProtocol().equals(CoreProtocolType.TCP))) {
            LOG.info("TCP Protocol detected. Port must exists");
            if (loadBalancer.getPort() == null) {
                throw new BadRequestException(ErrorMessages.TCP_PORT_REQUIRED);
            }
        }
    }

    public static void verifyProtocolAndHealthMonitorType(LoadBalancer loadBalancer) throws BadRequestException {
        if (loadBalancer.getHealthMonitor() == null || loadBalancer.getHealthMonitor().getType() == null) {
            return;
        }
        LOG.info("Health Monitor detected. Verifying that the load balancer's protocol matches the monitor type.");
        String type = loadBalancer.getHealthMonitor().getType();
        if (type.equals(CoreHealthMonitorType.HTTP)) {
            if (!(loadBalancer.getProtocol().equals(CoreProtocolType.HTTP))) {
                throw new BadRequestException(ErrorMessages.HTTP_HEALTH_MONITOR_PROTOCOL_INCOMPATIBLE);
            }
        } else if (type.equals(CoreHealthMonitorType.HTTPS)) {
            if (!(loadBalancer.getProtocol().equals(CoreProtocolType.HTTPS))) {
                throw new BadRequestException(ErrorMessages.HTTPS_HEALTH_MONITOR_PROTOCOL_INCOMPATIBLE);
            }
        }
    }

}
