package org.openstack.atlas.api.mgmt.async;

import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;


public class MgmtSetHostSubnetMappingListener extends BaseListener {


    private final Log LOG = LogFactory.getLog(MgmtSetHostSubnetMappingListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        Host rHost = getEsbRequestFromMessage(message).getHost();
        Host dHost = null;
        try {
            dHost = hostService.getById(rHost.getId());
        } catch (EntityNotFoundException enfe) {
            return;
        }

        Hostssubnet hostssubnet = getEsbRequestFromMessage(message).getHostssubnet();
        hostssubnet.getHostsubnets().get(0).setName(dHost.getTrafficManagerName());

        LOG.debug("Setting Host Subnet Mappings...");
        reverseProxyLoadBalancerVTMService.setSubnetMappings(dHost, hostssubnet);

        try {
            // Add related subnet blocks to database if requested
            if (getEsbRequestFromMessage(message).getAddVips()) {
                LOG.debug("Adding vips...");

                try {
                    IPv4Ranges ranges = new IPv4Ranges();
                    for (org.openstack.atlas.service.domain.pojos.Hostsubnet hs : hostssubnet.getHostsubnets()) {
                        for (org.openstack.atlas.service.domain.pojos.NetInterface ni : hs.getNetInterfaces()) {
                            for (org.openstack.atlas.service.domain.pojos.Cidr cidr : ni.getCidrs()) {
                                ranges.add(IPv4ToolSet.ipv4BlockToRange(cidr.getBlock()));
                            }
                        }
                    }

                    VirtualIpType vType = VirtualIpType.PUBLIC;
                    VirtualIpType vipType = getEsbRequestFromMessage(message).getVirtualIpType();
                    if (vipType != null) {
                        vType = vipType;
                    }
                    clusterService.addVirtualIpBlocks(ranges, vType, dHost.getCluster().getId());
                } catch (Exception ex) {
                    notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), ex.getMessage());
                }
            }

        } catch (Exception ex) {
            notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), ex.getMessage());
        }

        LOG.info("Completed setting host subnet mappings...");

    }
}
