package org.openstack.atlas.adapter.helpers;

import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.service.domain.entities.*;
import org.rackspace.stingray.client.StingrayRestClient;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.pool.Pool;
import org.rackspace.stingray.client.pool.PoolBasic;
import org.rackspace.stingray.client.pool.PoolProperties;
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
        String vsName;

        loadBalancer.setId(loadBalancerID);
        loadBalancer.setAccountId(accountID);
        vsName = ZxtmNameBuilder.genVSName(loadBalancer);

        server = client.getVirtualServer(ZxtmNameBuilder.genVSName(loadBalancer));
        properties = server.getProperties();

        initializeTrafficGroups(loadBalancer, properties);
        initializePool(loadBalancer, properties, vsName);
        initializeErrorPage(loadBalancer, properties);

        //TODO: Not sure where to get the rest of the data to complete this LB object (or what data I actually need)

        return loadBalancer;
    }

    private static void initializeErrorPage(LoadBalancer loadBalancer, VirtualServerProperties properties) {
        VirtualServerConnectionError connectionError;
        UserPages userPages = new UserPages();

        connectionError = properties.getConnection_errors();
        userPages.setErrorpage(connectionError.getError_file());
        loadBalancer.setUserPages(userPages);
    }

    private static void initializePool(LoadBalancer loadBalancer, VirtualServerProperties properties, String vsName) throws StingrayRestClientObjectNotFoundException, StingrayRestClientException {
        Set<Node> nodeSet = new HashSet<Node>();
        Pool pool = client.getPool(vsName);
        PoolProperties poolProperties = pool.getProperties();
        PoolBasic basic = poolProperties.getBasic();
        LoadBalancerAlgorithm algorithm = null;

        String algorithmString = poolProperties.getLoad_balancing().getAlgorithm();
        // I have no idea if any of these strings are right (besides "random", because that was from the example I debugged)
        if (algorithmString.toLowerCase().equals("roundrobin")) {
            algorithm = LoadBalancerAlgorithm.ROUND_ROBIN;
        } else if (algorithmString.toLowerCase().equals("weightedroundrobin")) {
            algorithm = LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN;
        } else if (algorithmString.toLowerCase().equals("leastconnections")) {
            algorithm = LoadBalancerAlgorithm.LEAST_CONNECTIONS;
        } else if (algorithmString.toLowerCase().equals("weightedleastconnections")) {
            algorithm = LoadBalancerAlgorithm.WEIGHTED_LEAST_CONNECTIONS;
        } else { // (algorithmString.toLowerCase().equals("random"))
            algorithm = LoadBalancerAlgorithm.RANDOM;
        }
        loadBalancer.setAlgorithm(algorithm);

        Set<String> nodeStrings = basic.getNodes();
        // There's probably a lot more to the Nodes than I'm setting here, which is just the absolute basics
        for (String n : nodeStrings) {
            Node node = new Node();
            // I seriously have to do this? what happens if there isn't a port in the string (or will there always be one)?
            String address = n.substring(0,n.lastIndexOf(':'));
            Integer port = Integer.parseInt(n.substring(n.lastIndexOf(':')+1));
            node.setIpAddress(address);
            node.setPort(port);
            nodeSet.add(node);
        }

        loadBalancer.setNodes(nodeSet);
    }

    private static void initializeTrafficGroups(LoadBalancer loadBalancer, VirtualServerProperties properties) {
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
                    if (ipAddresses.isEmpty() == false && ipAddresses.iterator().next().contains(":")) { // ipv6 -- Doesn't work yet
                        Set<LoadBalancerJoinVip6> joinVips = new HashSet<LoadBalancerJoinVip6>();
                        for (String address : ipAddresses) {
                            LoadBalancerJoinVip6 joinVip = new LoadBalancerJoinVip6();
                            VirtualIpv6 vip6 = new VirtualIpv6();
                            Cluster cluster = new Cluster();
                            cluster.setClusterIpv6Cidr(address);
                            // WTF I DON'T EVEN -- vip6.setLoadBalancerJoinVip6Set() is a thing?! someone is trolling me
                            vip6.setCluster(cluster);
                            joinVip.setPort(basic.getPort());
                            joinVip.setVirtualIp(vip6);
                            joinVips.add(joinVip);
                        }
                        loadBalancer.setLoadBalancerJoinVip6Set(joinVips);
                    }
                    else if (ipAddresses.isEmpty() == false && ipAddresses.iterator().next().contains(".")) { // ipv4 -- Maybe works?
                        Set<LoadBalancerJoinVip> joinVips = new HashSet<LoadBalancerJoinVip>();
                        for (String address : ipAddresses) {
                            LoadBalancerJoinVip joinVip = new LoadBalancerJoinVip();
                            VirtualIp vip = new VirtualIp();
                            vip.setIpAddress(address);
                            vip.setIpVersion(IpVersion.IPV4);
                            joinVip.setPort(basic.getPort()); // Assuming these use the same port as the parent?
                            joinVip.setVirtualIp(vip);
                            joinVips.add(joinVip);
                        }
                        loadBalancer.setLoadBalancerJoinVipSet(joinVips);
                    }
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
