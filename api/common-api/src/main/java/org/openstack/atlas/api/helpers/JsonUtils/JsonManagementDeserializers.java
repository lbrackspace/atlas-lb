package org.openstack.atlas.api.helpers.JsonUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvent;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;

public class JsonManagementDeserializers extends DeserializationHelper {

    public static LoadBalancers decodeLoadBalancers(JsonNode jn) throws JsonParseException {
        LoadBalancers loadbalancers = new LoadBalancers();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancers") != null
                && (jn.get("loadBalancers") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancers");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode lbNode = an.get(i);
            if (!(lbNode instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", lbNode.toString());
                throw new JsonParseException(msg, lbNode.traverse().getTokenLocation());
            }
            LoadBalancer lb = decodeLoadBalancer((ObjectNode) lbNode);
            loadbalancers.getLoadBalancers().add(lb);
        }
        return loadbalancers;
    }

    public static LoadBalancer decodeLoadBalancer(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancer") != null) {
            if (!(jn.get("loadBalancer") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("virtualIp").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancer");
            }
        }
        LoadBalancer lb = new LoadBalancer();
        lb.setTotalActiveConnections(getInt(jn, "totalActiveConnections"));
        lb.setNodeCount(getInt(jn, "nodeCount"));
        lb.setAccountId(getInt(jn, "accountId"));
        lb.setTimeout(getInt(jn, "timeout"));
        lb.setPort(getInt(jn, "port"));
        lb.setId(getInt(jn, "id"));
        lb.setAlgorithm(getString(jn, "algorithm"));
        lb.setProtocol(getString(jn, "protocol"));
        lb.setStatus(getString(jn, "status"));
        lb.setName(getString(jn, "name"));
        lb.setHalfClosed(getBoolean(jn, "halfClosed"));
        lb.setIsSticky(getBoolean(jn, "isSticky"));
        lb.setSticky(getBoolean(jn, "sticky"));


        if (jn.get("connectionLogging") != null) {
            lb.setConnectionLogging(JsonPublicDeserializers.decodeConnectionLogging((ObjectNode) jn.get("connectionLogging")));
        }
        if (jn.get("connectionThrottle") != null) {
            lb.setConnectionThrottle(JsonPublicDeserializers.decodeConnectionThrottle((ObjectNode) jn.get("connectionThrottle")));
        }
        if (jn.get("contentCaching") != null) {
            lb.setContentCaching(JsonPublicDeserializers.decodeContentCaching((ObjectNode) jn.get("contentCaching")));
        }
        if (jn.get("healthMonitor") != null) {
            lb.setHealthMonitor(JsonPublicDeserializers.decodeHealthMonitor((ObjectNode) jn.get("healthMonitor")));
        }
        if (jn.get("loadBalancerUsage") != null) {
            lb.setLoadBalancerUsage(JsonPublicDeserializers.decodeLoadBalancerUsage((ObjectNode) jn.get("loadBalancerUsage")));
        }
        if (jn.get("sessionPersistence") != null) {
            lb.setSessionPersistence(JsonPublicDeserializers.decodeSessionPersistence((ObjectNode) jn.get("sessionPersistence")));
        }
        if (jn.get("sourceAddresses") != null) {
            lb.setSourceAddresses(JsonPublicDeserializers.decodeSourceAddresses((ObjectNode) jn.get("sourceAddresses")));
        }
        if (jn.get("sslTermination") != null) {
            lb.setSslTermination(JsonPublicDeserializers.decodeSslTermination((ObjectNode) jn.get("sslTermination")));
        }
        if (jn.get("created") != null) {
            lb.setCreated(getCreated((ObjectNode) jn.get("created")));
        }
        if (jn.get("updated") != null) {
            lb.setUpdated(getUpdated((ObjectNode) jn.get("updated")));
        }
        if (jn.get("cluster") != null) {
            lb.setCluster(JsonPublicDeserializers.decodeCluster((ObjectNode) jn.get("cluster")));
        }
        if (jn.get("accessList") != null) {
            lb.setAccessList(JsonPublicDeserializers.decodeAccessList(jn.get("accessList")));
        }
        if (jn.get("nodes") != null) {
            lb.setNodes(JsonPublicDeserializers.decodeNodes(jn.get("nodes")));
        }
        if (jn.get("virtualIps") != null) {
            lb.setVirtualIps(JsonPublicDeserializers.decodeVirtualIps(jn.get("virtualIps")));
        }
        if (jn.get("metadata") != null) {
            lb.setMetadata(JsonPublicDeserializers.decodeMetadata(jn.get("metadata")));
        }
        if (jn.get("host") != null) {
            lb.setHost(decodeHost((ObjectNode) jn.get("host")));
        }
        if (jn.get("rateLimit") != null) {
            lb.setRateLimit(decodeRateLimit((ObjectNode) jn.get("rateLimit")));
        }
        if (jn.get("suspension") != null) {
            lb.setSuspension(decodeSuspension((ObjectNode) jn.get("suspension")));
        }
        if (jn.get("tickets") != null) {
            lb.setTickets(decodeTickets(jn.get("tickets")));
        }
        if (jn.get("accountLoadBalancerServiceEvents") != null) {
            lb.setAccountLoadBalancerServiceEvents(decodeAccountLoadBalancerServiceEvents(jn.get("accountLoadBalancerServiceEvents")));
        }
        return lb;
    }

    public static AccountLoadBalancerServiceEvents decodeAccountLoadBalancerServiceEvents(JsonNode jn) {
        return new AccountLoadBalancerServiceEvents();
    }

    public static LoadBalancerServiceEvents decodeLoadBalancerServiceEvents(JsonNode jn) {
        return new LoadBalancerServiceEvents();
    }

    public static LoadBalancerServiceEvent decodeLoadBalancerServiceEvent(ObjectNode jsonNodeIn) {
        return new LoadBalancerServiceEvent();
    }

    public static Hosts decodeHosts(JsonNode jn) throws JsonParseException {
        return new Hosts();
    }

    public static Host decodeHost(ObjectNode jsonNodeIn) throws JsonParseException {
        return new Host();
    }

    public static Tickets decodeTickets(JsonNode jn) throws JsonParseException {
        return new Tickets();
    }

    public static Ticket decodeTicket(ObjectNode jsonNodeIn) throws JsonParseException {
        return new Ticket();
    }

    public static RateLimit decodeRateLimit(ObjectNode jsonNodeIn) throws JsonParseException {
        return new RateLimit();
    }

    public static Suspension decodeSuspension(ObjectNode jsonNodeIn) throws JsonParseException {
        return new Suspension();
    }
}
