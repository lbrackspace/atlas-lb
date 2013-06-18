package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.traffic.ip.TrafficIp;
import org.rackspace.stingray.client.traffic.ip.TrafficIpBasic;
import org.rackspace.stingray.client.traffic.ip.TrafficIpProperties;
import org.rackspace.stingray.client.virtualserver.VirtualServer;
import org.rackspace.stingray.client.virtualserver.VirtualServerBasic;
import org.rackspace.stingray.client.virtualserver.VirtualServerConnectionError;
import org.rackspace.stingray.client.virtualserver.VirtualServerProperties;

import java.util.HashSet;
import java.util.Set;

public class ReverseResourceTranslator {
    private static StingrayRestClient client = new StingrayRestClient();
    public static LoadBalancer getLoadBalancer(Integer loadBalancerID, Integer accountID) throws InsufficientRequestException, StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        LoadBalancer loadBalancer = new LoadBalancer();
        VirtualServer server;
        VirtualServerProperties properties;

        loadBalancer.setId(loadBalancerID);
        loadBalancer.setAccountId(accountID);
        loadBalancer.setName(ZxtmNameBuilder.genVSName(loadBalancer));

        server = client.getVirtualServer(loadBalancer.getName());
        properties = server.getProperties();

        setConnectionInfo(loadBalancer, properties);
        setNodes(loadBalancer, properties);
        setErrorPage(loadBalancer, properties);

        //TODO: Not sure where to get the rest of the data to complete this LB object

        return loadBalancer;
    }

    private static void setErrorPage(LoadBalancer loadBalancer, VirtualServerProperties properties) {
        VirtualServerConnectionError connectionError;
        UserPages userPages = new UserPages();

        connectionError = properties.getConnection_errors();
        userPages.setErrorpage(connectionError.getError_file());
        loadBalancer.setUserPages(userPages);
    }

    private static void setNodes(LoadBalancer loadBalancer, VirtualServerProperties properties) {
        Set<Node> nodeSet = new HashSet<Node>();

        // Will I need to set this up?

        loadBalancer.setNodes(nodeSet);
    }

    private static void setConnectionInfo(LoadBalancer loadBalancer, VirtualServerProperties properties) {
        VirtualServerBasic basic = properties.getBasic();

        // Where do I get the public/servicenet IPs? Is this the right track?
        Set<String> trafficIpGroups = basic.getListen_on_traffic_ips();
        for (String ipGroup : trafficIpGroups) {
            try {
                TrafficIp ip = client.getTrafficIp(ipGroup);
                TrafficIpProperties ipp = ip.getProperties();
                TrafficIpBasic ipb = ipp.getBasic();
                if (ipb.getEnabled() == true) {
                    Set<String> ipAddresses = ipb.getIpaddresses();
                    // How many IP Addresses can we set?! It looks like only one public and one servicenet...
                    // Still not sure how to tell which is which, either
                }
            } catch (StingrayRestClientException e) {
                //log
            } catch (StingrayRestClientObjectNotFoundException e) {
                //log
            }
        }

//        loadBalancer.setIpv4Public(null);
//        loadBalancer.setIpv6Public(null);
//        loadBalancer.setIpv4Servicenet(null);
        loadBalancer.setPort(basic.getPort());
    }
}
