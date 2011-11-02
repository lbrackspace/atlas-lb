package org.openstack.atlas.rax.domain.service;

import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.service.domain.common.ErrorMessages;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.service.impl.LoadBalancerServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class RaxLoadBalancerServiceImpl extends LoadBalancerServiceImpl {

    @Override
    protected void setPropertiesForUpdate(LoadBalancer loadBalancer, LoadBalancer dbLoadBalancer) throws BadRequestException {
        super.setPropertiesForUpdate(loadBalancer, dbLoadBalancer);
        setPort(loadBalancer, dbLoadBalancer);
        setProtocol(loadBalancer, dbLoadBalancer);
        setCrazyName(loadBalancer, dbLoadBalancer);
    }

    private void setProtocol(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws BadRequestException {
        boolean portHMTypecheck = true;
        if (loadBalancer.getProtocol() != null && !loadBalancer.getProtocol().equals(dbLoadBalancer.getProtocol())) {

            //check for health monitor type and allow update only if protocol matches health monitory type for HTTP and HTTPS
            if (dbLoadBalancer.getHealthMonitor() != null) {
                if (dbLoadBalancer.getHealthMonitor().getType() != null) {
                    if (dbLoadBalancer.getHealthMonitor().getType().equals(CoreProtocolType.HTTP)) {
                        //incoming port not HTTP
                        if (!(loadBalancer.getProtocol().equals(CoreProtocolType.HTTP))) {
                            portHMTypecheck = false;
                        }
                    } else if (dbLoadBalancer.getHealthMonitor().getType().equals(CoreProtocolType.HTTPS)) {
                        //incoming port not HTTP
                        if (!(loadBalancer.getProtocol().equals(CoreProtocolType.HTTPS))) {
                            portHMTypecheck = false;
                        }
                    }
                }
            }

            if (portHMTypecheck) {
                /* Notify the Usage Processor on changes of protocol to and from secure protocols */
                //notifyUsageProcessorOfSslChanges(message, queueLb, dbLoadBalancer);

                if (loadBalancer.getProtocol().equals(CoreProtocolType.HTTP)) {
                    LOG.debug("Updating loadbalancer protocol to " + loadBalancer.getProtocol());
                    dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                } else {
                    dbLoadBalancer.setSessionPersistence(null);
                    dbLoadBalancer.setProtocol(loadBalancer.getProtocol());
                }
            } else {
                LOG.error("Cannot update port as the loadbalancer has a incompatible Health Monitor type");
                throw new BadRequestException(ErrorMessages.PORT_HEALTH_MONITOR_INCOMPATIBLE);
            }
        }
    }

    private void setPort(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws BadRequestException {
        if (loadBalancer.getPort() != null && !loadBalancer.getPort().equals(dbLoadBalancer.getPort())) {
            LOG.debug("Updating loadbalancer port to " + loadBalancer.getPort());
            if (loadBalancerRepository.canUpdateToNewPort(loadBalancer.getPort(), dbLoadBalancer.getLoadBalancerJoinVipSet())) {
                loadBalancerRepository.updatePortInJoinTable(loadBalancer);
                dbLoadBalancer.setPort(loadBalancer.getPort());
            } else {
                LOG.error("Cannot update load balancer port as it is currently in use by another virtual ip.");
                throw new BadRequestException(ErrorMessages.PORT_IN_USE);
            }
        }
    }

    private void setCrazyName(final LoadBalancer loadBalancer, final LoadBalancer dbLoadBalancer) throws BadRequestException {

        if (loadBalancer instanceof RaxLoadBalancer) {
            RaxLoadBalancer raxLoadBalancer = (RaxLoadBalancer) loadBalancer;
            RaxLoadBalancer raxDbLoadBalancer = (RaxLoadBalancer) dbLoadBalancer;

            if (raxLoadBalancer.getCrazyName() != null && !raxLoadBalancer.getCrazyName().equals(raxDbLoadBalancer.getCrazyName())) {
                LOG.debug("Updating loadbalancer crazy name to " + raxLoadBalancer.getCrazyName());
                raxDbLoadBalancer.setCrazyName(raxLoadBalancer.getCrazyName());
            }
        } else {
            LOG.error("Trying to set crazy name on a load balancer that is not a RaxLoadBalancer!");
        }
    }
}
