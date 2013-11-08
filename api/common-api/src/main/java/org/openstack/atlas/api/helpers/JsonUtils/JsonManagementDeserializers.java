package org.openstack.atlas.api.helpers.JsonUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Event;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvent;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancersStatusHistory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Zone;

import java.util.ArrayList;
import java.util.List;

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
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancer").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancer");
            }
        }
        LoadBalancer lb = (LoadBalancer) JsonPublicDeserializers.decodeLoadBalancer(jsonNodeIn);
        lb.setHost(decodeHost((ObjectNode) jn.get("host")));
        lb.setRateLimit(decodeRateLimit((ObjectNode) jn.get("rateLimit")));
        lb.setSuspension(decodeSuspension((ObjectNode) jn.get("suspension")));
        lb.setTickets(decodeTickets(jn.get("tickets")));
        lb.setAccountLoadBalancerServiceEvents(decodeAccountLoadBalancerServiceEvents(jn.get("accountLoadBalancerServiceEvents")));
        return lb;
    }

    public static AccountLoadBalancerServiceEvents decodeAccountLoadBalancerServiceEvents(JsonNode jn) throws JsonParseException {
        AccountLoadBalancerServiceEvents events = new AccountLoadBalancerServiceEvents();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountLoadBalancerServiceEvents") != null
                && (jn.get("accountLoadBalancerServiceEvents") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountLoadBalancerServiceEvents");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode eventsNode = an.get(i);
            if (!(eventsNode instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", eventsNode.toString());
                throw new JsonParseException(msg, eventsNode.traverse().getTokenLocation());
            }
            LoadBalancerServiceEvents lbevents = decodeLoadBalancerServiceEvents(eventsNode);
            events.getLoadBalancerServiceEvents().add(lbevents);
        }
        return events;
    }

    public static LoadBalancerServiceEvents decodeLoadBalancerServiceEvents(JsonNode jn) throws JsonParseException {
        LoadBalancerServiceEvents events = new LoadBalancerServiceEvents();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerServiceEvents") != null
                && (jn.get("loadBalancerServiceEvents") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerServiceEvents");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode eventsNode = an.get(i);
            if (!(eventsNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, eventsNode.toString());
                throw new JsonParseException(msg, eventsNode.traverse().getTokenLocation());
            }
            LoadBalancerServiceEvent event = decodeLoadBalancerServiceEvent((ObjectNode) eventsNode);
            events.getLoadBalancerServiceEvents().add(event);
        }

        events.setLoadbalancerId(getInt(jn, "loadbalancerId"));
        return events;
    }

    public static LoadBalancerServiceEvent decodeLoadBalancerServiceEvent(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerServiceEvent") != null) {
            if (!(jn.get("loadBalancerServiceEvent") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerServiceEvent").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerServiceEvent");
            }
        }
        LoadBalancerServiceEvent event = (LoadBalancerServiceEvent) decodeEvent(jn);
        event.setDetailedMessage(getString(jn, "detailedMessage"));
        return event;
    }

    public static Event decodeEvent(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("event") != null) {
            if (!(jn.get("event") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("event").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("event");
            }
        }
        Event event = new Event();
        event.setId(getInt(jn, "id"));
        event.setLoadbalancerId(getInt(jn, "loadbalancerId"));
        event.setType(getString(jn, "type"));
        event.setDescription(getString(jn, "description"));
        event.setCategory(getString(jn, "category"));
        event.setSeverity(getString(jn, "severity"));
        event.setRelativeUri(getString(jn, "relativeUri"));
        event.setAccountId(getInt(jn, "accountId"));
        event.setTitle(getString(jn, "title"));
        event.setAuthor(getString(jn, "author"));
        event.setCreated(getString(jn, "created"));
        return event;
    }

    public static Hosts decodeHosts(JsonNode jn) throws JsonParseException {
        Hosts hosts = new Hosts();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hosts") != null
                && (jn.get("hosts") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hosts");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode hostNode = an.get(i);
            if (!(hostNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, hostNode.toString());
                throw new JsonParseException(msg, hostNode.traverse().getTokenLocation());
            }
            Host host = decodeHost((ObjectNode) hostNode);
            hosts.getHosts().add(host);
        }
        return hosts;
    }

    public static HostMachineDetails decodeHostMachine(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        HostMachineDetails details = new HostMachineDetails();
        details.setHost(decodeHost((ObjectNode) jn.get("host")));
        details.setActiveLBConfigurations(getLong(jn, "activeLBConfigurations"));
        details.setAvailableConcurrentConnections(getInt(jn, "availableConcurrentConnections"));
        details.setCurrentUtilization(getString(jn, "currentUtilization"));
        details.setTotalConcurrentConnections(getInt(jn, "totalConcurrentConnections"));
        details.setUniqueCustomers(getInt(jn, "uniqueCustomers"));
        return details;
    }

    public static Host decodeHost(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("host") != null) {
            if (!(jn.get("host") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("host").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("host");
            }
        }
        Host host = new Host();
        host.setNumberOfLoadBalancingConfigurations(getInt(jn, "numberOfLoadBalancingConfigurations"));
        host.setManagementRestInterface(getString(jn, "managementRestInterface"));
        host.setManagementSoapInterface(getString(jn, "managementSoapInterface"));
        host.setMaxConcurrentConnections(getInt(jn, "maxConcurrentConnections"));
        host.setNumberOfUniqueCustomers(getInt(jn, "numberOfUniqueCustomers"));
        host.setRestEndpointActive(getBoolean(jn, "restEndpointActive"));
        host.setSoapEndpointActive(getBoolean(jn, "soapEndpointActive"));
        host.setTrafficManagerName(getString(jn, "trafficManagerName"));
        host.setIpv4Servicenet(getString(jn, "ipv4Servicenet"));
        host.setIpv6Servicenet(getString(jn, "ipv6Servicenet"));
        host.setCoreDeviceId(getString(jn, "coreDeviceId"));
        host.setManagementIp(getString(jn, "managementIp"));
        host.setUtilization(getString(jn, "utilization"));
        host.setIpv4Public(getString(jn, "ipv4Public"));
        host.setIpv6Public(getString(jn, "ipv6Public"));
        host.setStatus(getHostStatus(jn, "status"));
        host.setClusterId(getInt(jn, "clusterId"));
        host.setType(getHostType(jn, "type"));
        host.setName(getString(jn, "name"));
        host.setZone(getZone(jn, "zone"));
        host.setId(getInt(jn, "id"));
        return host;
    }

    public static Suspension decodeSuspension(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("suspension") != null) {
            if (!(jn.get("suspension") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("suspension").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("suspension");
            }
        }
        Suspension suspension = new Suspension();
        suspension.setTicket(decodeTicket((ObjectNode) jn.get("ticket")));
        suspension.setId(getInt(jn, "id"));
        suspension.setReason(getString(jn, "reason"));
        suspension.setUser(getString(jn, "user"));
        return suspension;
    }

    public static RateLimit decodeRateLimit(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("rateLimit") != null) {
            if (!(jn.get("rateLimit") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("rateLimit").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("rateLimit");
            }
        }
        RateLimit limit = new RateLimit();
        limit.setTicket(decodeTicket((ObjectNode) jn.get("ticket")));
        limit.setExpirationTime(getDate(jn, "expirationTime"));
        limit.setMaxRequestsPerSecond(getInt(jn, "maxRequestsPerSecond"));
        return limit;
    }

    public static Tickets decodeTickets(JsonNode jn) throws JsonParseException {
        Tickets tickets = new Tickets();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("tickets") != null
                && (jn.get("tickets") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("tickets");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode ticketNode = an.get(i);
            if (!(ticketNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, ticketNode.toString());
                throw new JsonParseException(msg, ticketNode.traverse().getTokenLocation());
            }
            Ticket ticket = decodeTicket((ObjectNode) ticketNode);
            tickets.getTickets().add(ticket);
        }
        return tickets;
    }

    public static Ticket decodeTicket(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("ticket") != null) {
            if (!(jn.get("ticket") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("ticket").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("ticket");
            }
        }
        Ticket ticket = new Ticket();
        ticket.setTicketId(getString(jn, "ticketId"));
        ticket.setComment(getString(jn, "comment"));
        ticket.setId(getInt(jn, "id"));
        return ticket;
    }

    public static LoadBalancersStatusHistory decodeLoadBalancersStatusHistory(JsonNode jn) throws JsonParseException {
        LoadBalancersStatusHistory histories = new LoadBalancersStatusHistory();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancersStatusHistory") != null
                && (jn.get("loadBalancersStatusHistory") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancersStatusHistory");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode ticketNode = an.get(i);
            if (!(ticketNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, ticketNode.toString());
                throw new JsonParseException(msg, ticketNode.traverse().getTokenLocation());
            }
            LoadBalancerStatusHistory history = decodeLoadBalancerStatusHistory((ObjectNode) ticketNode);
            histories.getLoadBalancerStatusHistories().add(history);
        }
        return histories;
    }

    public static LoadBalancerStatusHistory decodeLoadBalancerStatusHistory(ObjectNode jsonNodeIn) throws JsonParseException {
        return new LoadBalancerStatusHistory();
    }

    public static HostType getHostType(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String typeString = getString(jsonNodeIn, prop);
        HostType type;
        if (typeString == null) {
            return null;
        }
        try {
            type = HostType.fromValue(typeString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal Host type found %s in %s", typeString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return type;
    }

    public static HostStatus getHostStatus(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String statusString = getString(jsonNodeIn, prop);
        HostStatus status;
        if (statusString == null) {
            return null;
        }
        try {
            status = HostStatus.fromValue(statusString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal Host status found %s in %s", statusString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return status;
    }

    public static Zone getZone(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String zoneString = getString(jsonNodeIn, prop);
        Zone zone;
        if (zoneString == null) {
            return null;
        }
        try {
            zone = Zone.fromValue(zoneString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal zone found %s in %s", zoneString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return zone;
    }
}
