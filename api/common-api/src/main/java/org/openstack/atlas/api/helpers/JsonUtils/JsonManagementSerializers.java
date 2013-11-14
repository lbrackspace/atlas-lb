package org.openstack.atlas.api.helpers.JsonUtils;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.api.helpers.JsonSerializeException;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Zone;
import org.w3.atom.Link;
import java.util.List;

public class JsonManagementSerializers extends SerializationHelper {

    public static void attachLoadBalancers(ObjectNode objectNode, LoadBalancers loadbalancers, boolean includeLinks) throws JsonSerializeException {
        List<LoadBalancer> loadBalancerList = loadbalancers.getLoadBalancers();
        List<Link> atomLinks = loadbalancers.getLinks();
        ArrayNode an = objectNode.putArray("loadBalancers");
        if (loadBalancerList != null && loadBalancerList.size() > 0) {
            for (LoadBalancer lb : loadBalancerList) {
                ObjectNode lbNode = an.addObject();
                attachLoadBalancer(lbNode, lb, false);
            }
        }
        if (includeLinks && atomLinks != null && atomLinks.size() > 0) {
            an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachLoadBalancer(ObjectNode objectNode, LoadBalancer loadBalancer, boolean includeName) throws JsonSerializeException {
        JsonPublicSerializers.attachLoadBalancer(objectNode, loadBalancer, includeName);
        ObjectNode node = objectNode;
        if (loadBalancer.getHost() != null) {
            attachHost(node, loadBalancer.getHost(), true);
        }
        if (loadBalancer.getTickets() != null) {
            attachTickets(node, loadBalancer.getTickets(), false);
        }
        if (loadBalancer.getAccountLoadBalancerServiceEvents() != null) {
            attachAccountLoadBalancerServiceEvents(node, loadBalancer.getAccountLoadBalancerServiceEvents(), false);
        }
        if (loadBalancer.getSuspension() != null) {
            attachSuspension(node, loadBalancer.getSuspension());
        }
        if (loadBalancer.getRateLimit() != null) {
            attachRateLimit(node, loadBalancer.getRateLimit());
        }
        if (loadBalancer.getAccountId() != null) {
            node.put("accountId", loadBalancer.getAccountId());
        }
        if (loadBalancer.isSticky() != null) {
            node.put("sticky", loadBalancer.isSticky());
        }
        if (loadBalancer.getTotalActiveConnections() != null) {
            node.put("totalActiveConnections", loadBalancer.getTotalActiveConnections());
        }
    }

    public static void attachHosts(ObjectNode objectNode, Hosts hosts) throws JsonSerializeException {
        List<Host> hostList = hosts.getHosts();
        ArrayNode an = objectNode.putArray("hosts");
        if (hostList != null && hostList.size() > 0) {
            for (Host host : hostList) {
                ObjectNode hostNode = an.addObject();
                attachHost(hostNode, host, false);
            }
        }
        objectNode.put("sticky", hosts.isSticky());
    }

    public static void attachHost(ObjectNode objectNode, Host host, boolean includeName) throws JsonSerializeException {
        ObjectNode node;
        if (includeName) {
            objectNode.putObject("host");
            node = (ObjectNode) objectNode.get("host");
        } else {
            node = objectNode;
        }
        if (host.getZone() != null) {
            node.put("zone", host.getZone().value());
        }
        if (host.getStatus() != null) {
            node.put("status", host.getStatus().value());
        }
        if (host.getType() != null) {
            node.put("type", host.getType().value());
        }
        if (host.getClusterId() != null) {
            node.put("clusterId", host.getClusterId());
        }
        if (host.getCoreDeviceId() != null) {
            node.put("coreDeviceId", host.getCoreDeviceId());
        }
        if (host.getId() != null) {
            node.put("id", host.getId());
        }
        if (host.getIpv4Public() != null) {
            node.put("ipv4Public", host.getIpv4Public());
        }
        if (host.getIpv4Servicenet() != null) {
            node.put("ipv4Servicenet", host.getIpv4Servicenet());
        }
        if (host.getIpv6Public() != null) {
            node.put("ipv6Public", host.getIpv6Public());
        }
        if (host.getIpv6Servicenet() != null) {
            node.put("ipv6Servicenet", host.getIpv6Servicenet());
        }
        if (host.getManagementIp() != null) {
            node.put("managementIp", host.getManagementIp());
        }
        if (host.getManagementRestInterface() != null) {
            node.put("managementRestInterface", host.getManagementRestInterface());
        }
        if (host.getManagementSoapInterface() != null) {
            node.put("managementSoapInterface", host.getManagementSoapInterface());
        }
        if (host.getMaxConcurrentConnections() != null) {
            node.put("maxConcurrentConnections", host.getMaxConcurrentConnections());
        }
        if (host.getName() != null) {
            node.put("name", host.getName());
        }
        if (host.getNumberOfLoadBalancingConfigurations() != null) {
            node.put("numberOfLoadBalancingConfigurations", host.getNumberOfLoadBalancingConfigurations());
        }
        if (host.getNumberOfUniqueCustomers() != null) {
            node.put("numberOfUniqueCustomers", host.getNumberOfUniqueCustomers());
        }
        if (host.getTrafficManagerName() != null) {
            node.put("trafficManagerName", host.getTrafficManagerName());
        }
        if (host.getUtilization() != null) {
            node.put("utilization", host.getUtilization());
        }
        if (host.isRestEndpointActive()) {
            node.put("restEndpointActive", host.isRestEndpointActive());
        }
        if (host.isSoapEndpointActive()) {
            node.put("soapEndpointActive", host.isSoapEndpointActive());
        }
    }

    public static void attachTickets(ObjectNode objectNode, Tickets tickets, boolean includeLinks) throws JsonSerializeException {

    }

    public static void attachAccountLoadBalancerServiceEvents(ObjectNode objectNode, AccountLoadBalancerServiceEvents events, boolean includeLinks) throws JsonSerializeException {

    }

    public static void attachSuspension(ObjectNode objectNode, Suspension suspension) throws JsonSerializeException {

    }

    public static void attachRateLimit(ObjectNode objectNode, RateLimit rateLimit) throws JsonSerializeException {

    }
}
