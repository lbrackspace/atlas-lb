package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;
import java.util.List;


public class MgmtSetHostsSubnetMappingListener extends BaseListener {


    private final Log LOG = LogFactory.getLog(MgmtSetHostsSubnetMappingListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        LOG.debug(message);
        List<Host> rHosts = getEsbRequestFromMessage(message).getHosts();
        List<Host> dHosts = null;
        dHosts = clusterService.getHosts(rHosts.get(0).getId());

        Hostssubnet hostssubnet = getEsbRequestFromMessage(message).getHostssubnet();
        hostssubnet.getHostsubnets().get(0).setName(dHosts.get(0).getTrafficManagerName());

        LOG.debug("Setting Host Subnet Mappings...");
        for(Host dHost : dHosts){
            try{
                reverseProxyLoadBalancerVTMService.setSubnetMappings(dHost, hostssubnet);
            }catch(Exception ex){
                // Rollback for the hosts in case of failure
                for(Host h : dHosts){
                    Hostssubnet subnetMapping = reverseProxyLoadBalancerVTMService.getSubnetMappings(h);
                    reverseProxyLoadBalancerVTMService.setSubnetMappings(h, subnetMapping);
                }
                notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), ex.getMessage());
            }
        }

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
                    clusterService.addVirtualIpBlocks(ranges, vType, dHosts.get(0).getCluster().getId());
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
