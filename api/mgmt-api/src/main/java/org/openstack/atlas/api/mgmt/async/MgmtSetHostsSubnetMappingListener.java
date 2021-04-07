package org.openstack.atlas.api.mgmt.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.service.domain.entities.Cluster;
import org.openstack.atlas.service.domain.entities.Host;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.services.helpers.AlertType;

import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MgmtSetHostsSubnetMappingListener extends BaseListener {

    private final Log LOG = LogFactory.getLog(MgmtSetHostsSubnetMappingListener.class);

    @Override
    public void doOnMessage(final Message message) throws Exception {
        LOG.debug("Entering " + getClass());
        
        boolean isRolledback = false;
        HashMap<Host, Hostssubnet> hostSubnetmappings = new HashMap<>();

        Cluster dCluster = getEsbRequestFromMessage(message).getCluster();
        List<Host> dHosts = clusterService.getHosts(dCluster.getId());

        Hostssubnet hostssubnet = getEsbRequestFromMessage(message).getHostssubnet();

        for(Host dHost : dHosts){
            LOG.debug(String.format("Setting Host %s Subnet Mappings...", dHost.getId()));

            try{
                hostssubnet.getHostsubnets().get(0).setName(dHost.getTrafficManagerName());
                reverseProxyLoadBalancerVTMService.setSubnetMappings(dHost, hostssubnet);
            } catch(Exception ex){
                // Attempt to rollback for the hosts in the hashmap we've already processed
                for(Host h : hostSubnetmappings.keySet()){
                    reverseProxyLoadBalancerVTMService.deleteSubnetMappings(h, hostSubnetmappings.get(h));
                }
                notificationService.saveAlert(ex, AlertType.ZEUS_FAILURE.name(), ex.getMessage());
                isRolledback = true;
                break;
            }
            // Build rollback objects that avoid name overwriting 
            Hostssubnet savedHostSubnet = new Hostssubnet();
            List<Hostsubnet> hss = new ArrayList<>();
            Hostsubnet hs = new Hostsubnet();
            hs.setName(dHost.getTrafficManagerName());
            hs.setNetInterfaces(hostssubnet.getHostsubnets().get(0).getNetInterfaces());
            hss.add(hs);
            savedHostSubnet.setHostsubnets(hss);
            hostSubnetmappings.put(dHost, savedHostSubnet);
        }

        if(!isRolledback) {
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
        }
        LOG.info("Completed setting host subnet mappings...");
    }
}
