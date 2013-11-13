package org.openstack.atlas.api.helpers.JsonUtils;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Account;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountBillings;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountGroup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountGroups;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountInCluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountInHost;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecords;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Accounts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountsInCluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountsInHost;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AlertStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AllAbsoluteLimits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Backups;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ByIdOrName;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CapacityPlanningVirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cidr;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CidrTest;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterDetails;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ClusterStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomLimitAccount;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomLimitAccounts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomLimits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Customer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.CustomerList;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Customers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.DataCenter;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Event;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HealthCheck;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HealthChecks;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostCapacityReport;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostCapacityReports;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostMachineDetails;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostStatus;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsage;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageList;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsages;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Job;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Jobs;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LdapGroup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LdapInfo;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerEvent;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroups;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvent;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerStatusHistory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerUsageRecords;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancersStatusHistory;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.NetInterface;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RateLimit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Suspension;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.UserRole;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReports;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlock;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusRateLimitedLoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusRateLimitedLoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Zone;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling;

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
        lb.setSticky(getBoolean(jn, "sticky"));
        lb.setTotalActiveConnections(getInt(jn, "totalActiveConnections"));
        lb.setAccountId(getInt(jn, "accountId"));
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
            JsonNode historyNode = an.get(i);
            if (!(historyNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, historyNode.toString());
                throw new JsonParseException(msg, historyNode.traverse().getTokenLocation());
            }
            LoadBalancerStatusHistory history = decodeLoadBalancerStatusHistory((ObjectNode) historyNode);
            histories.getLoadBalancerStatusHistories().add(history);
        }
        return histories;
    }

    public static LoadBalancerStatusHistory decodeLoadBalancerStatusHistory(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerStatusHistory") != null) {
            if (!(jn.get("loadBalancerStatusHistory") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerStatusHistory").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerStatusHistory");
            }
        }
        LoadBalancerStatusHistory history = new LoadBalancerStatusHistory();
        history.setLoadBalancerId(getInt(jn, "loadBalancerId"));
        history.setAccountId(getInt(jn, "accountId"));
        history.setCreated(getDate(jn, "created"));
        history.setStatus(getString(jn, "status"));
        return history;
    }

    public static ByIdOrName decodeByIdOrName(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("byIdOrName") != null) {
            if (!(jn.get("byIdOrName") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("byIdOrName").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("byIdOrName");
            }
        }
        ByIdOrName obj = new ByIdOrName();
        obj.setId(getInt(jn, "id"));
        obj.setName(getString(jn, "name"));
        return obj;
    }

    public static AccountLoadBalancers decodeAccountLoadBalancers(JsonNode jn) throws JsonParseException {
        AccountLoadBalancers accountLbs = new AccountLoadBalancers();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountLoadBalancers") != null
                && (jn.get("accountLoadBalancers") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountLoadBalancers");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode lbNode = an.get(i);
            if (!(lbNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, lbNode.toString());
                throw new JsonParseException(msg, lbNode.traverse().getTokenLocation());
            }
            AccountLoadBalancer accountLb = decodeAccountLoadBalancer((ObjectNode) lbNode);
            accountLbs.getAccountLoadBalancers().add(accountLb);
        }
        accountLbs.setAccountId(getInt(jn, "accountId"));
        return accountLbs;
    }

    public static AccountLoadBalancer decodeAccountLoadBalancer(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountLoadBalancer") != null) {
            if (!(jn.get("accountLoadBalancer") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountLoadBalancer").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountLoadBalancer");
            }
        }
        AccountLoadBalancer lb = new AccountLoadBalancer();
        lb.setLoadBalancerStatus(getString(jn, "loadBalancerStatus"));
        lb.setLoadBalancerName(getString(jn, "loadBalancerName"));
        lb.setLoadBalancerId(getInt(jn, "loadBalancerId"));
        lb.setClusterName(getString(jn, "clusterName"));
        lb.setProtocol(getString(jn, "protocol"));
        lb.setClusterId(getInt(jn, "clusterId"));
        lb.setStatus(getString(jn, "status"));
        return lb;
    }

    public static ExtendedAccountLoadbalancers decodeExtendedAccountLoadbalancers(JsonNode jn) throws JsonParseException {
        ExtendedAccountLoadbalancers extendedLbs = new ExtendedAccountLoadbalancers();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("extendedAccountLoadbalancers") != null
                && (jn.get("extendedAccountLoadbalancers") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("extendedAccountLoadbalancers");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode lbNode = an.get(i);
            if (!(lbNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, lbNode.toString());
                throw new JsonParseException(msg, lbNode.traverse().getTokenLocation());
            }
            ExtendedAccountLoadbalancer extendedLb = decodeExtendedAccountLoadBalancer((ObjectNode) lbNode);
            extendedLbs.getExtendedAccountLoadbalancers().add(extendedLb);
        }
        extendedLbs.setAccountId(getInt(jn, "accountId"));
        return extendedLbs;
    }

    public static ExtendedAccountLoadbalancer decodeExtendedAccountLoadBalancer(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("extendedAccountLoadbalancer") != null) {
            if (!(jn.get("extendedAccountLoadbalancer") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("extendedAccountLoadbalancer").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("extendedAccountLoadbalancer");
            }
        }
        ExtendedAccountLoadbalancer lb = new ExtendedAccountLoadbalancer();
        lb.setVirtualIps(decodeVirtualIps(jn.get("virtualIps")));
        lb.setLoadBalancerName(getString(jn, "loadBalancerName"));
        lb.setLoadBalancerId(getInt(jn, "loadBalancerId"));
        lb.setClusterName(getString(jn, "clusterName"));
        lb.setProtocol(getString(jn, "protocol"));
        lb.setClusterId(getInt(jn, "clusterId"));
        lb.setStatus(getString(jn, "status"));
        lb.setRegion(getDataCenter(jn, "region"));
        return lb;
    }

    public static Blacklist decodeBlacklist(JsonNode jn) throws JsonParseException {
        Blacklist blacklist = new Blacklist();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("blacklist") != null
                && (jn.get("blacklist") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("blacklist");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode blacklistItem = an.get(i);
            if (!(blacklistItem instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, blacklistItem.toString());
                throw new JsonParseException(msg, blacklistItem.traverse().getTokenLocation());
            }
            BlacklistItem item = decodeBlacklistItem((ObjectNode) blacklistItem);
            blacklist.getBlacklistItems().add(item);
        }
        return blacklist;
    }

    public static BlacklistItem decodeBlacklistItem(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("blacklistItem") != null) {
            if (!(jn.get("blacklistItem") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("blacklistItem").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("blacklistItem");
            }
        }
        BlacklistItem item = new BlacklistItem();
        item.setType(getBlacklistType(jn, "blacklistType"));
        item.setIpVersion(getIpVersion(jn, "ipVersion"));
        item.setCidrBlock(getString(jn, "cidrBlock"));
        item.setId(getInt(jn, "id"));
        return item;
    }

    public static VirtualIps decodeVirtualIps(JsonNode jn) throws JsonParseException {
        return new VirtualIps();
    }

    public static VirtualIp decodeVirtualIp(ObjectNode jsonNodeIn) throws JsonParseException {
        return new VirtualIp();
    }

    public static AccountsInCluster decodeAccountsInCluster(JsonNode jn) throws JsonParseException {
        AccountsInCluster accounts = new AccountsInCluster();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountsInCluster") != null
                && (jn.get("accountsInCluster") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountsInCluster");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode accountNode = an.get(i);
            if (!(accountNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, accountNode.toString());
                throw new JsonParseException(msg, accountNode.traverse().getTokenLocation());
            }
            AccountInCluster account = decodeAccountInCluster((ObjectNode) accountNode);
            accounts.getAccountInClusters().add(account);
        }
        accounts.setTotalAccounts(getInt(jn, "totalAccounts"));
        return accounts;
    }

    public static AccountInCluster decodeAccountInCluster(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountInCluster") != null) {
            if (!(jn.get("accountInCluster") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountInCluster").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountInCluster");
            }
        }
        AccountInCluster account = new AccountInCluster();
        account.setLoadBalancerCount(getLong(jn, "loadBalancerCount"));
        account.setAccountId(getInt(jn, "accountId"));
        account.setClusterId(getInt(jn, "clusterId"));
        return account;
    }

    public static AccountsInHost decodeAccountsInHost(JsonNode jn) throws JsonParseException {
        AccountsInHost accounts = new AccountsInHost();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountsInCluster") != null
                && (jn.get("accountsInCluster") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountsInCluster");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode accountNode = an.get(i);
            if (!(accountNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, accountNode.toString());
                throw new JsonParseException(msg, accountNode.traverse().getTokenLocation());
            }
            AccountInHost account = decodeAccountInHost((ObjectNode) accountNode);
            accounts.getAccountInHosts().add(account);
        }
        accounts.setTotalAccounts(getInt(jn, "totalAccounts"));
        return accounts;
    }

    public static AccountInHost decodeAccountInHost(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountInHost") != null) {
            if (!(jn.get("accountInHost") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountInHost").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountInHost");
            }
        }
        AccountInHost account = new AccountInHost();
        account.setLoadBalancerCount(getLong(jn, "loadBalancerCount"));
        account.setAccountId(getInt(jn, "accountId"));
        account.setHostId(getInt(jn, "hostId"));
        return account;
    }

    public static ClusterDetails decodeClusterDetails(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("clusterDetails") != null) {
            if (!(jn.get("clusterDetails") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("clusterDetails").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("clusterDetails");
            }
        }
        ClusterDetails details = new ClusterDetails();
        details.setHostMachineDetails(decodeHostMachineDetails((ObjectNode) jn.get("hostMachineDetails")));
        details.setVirtualIpBlocks(decodeCapacityPlanningVirtualIpBlocks((ObjectNode) jn.get("virtualIpBlocks")));
        details.setAverageUtilizationofHosts(getString(jn, "averageUtilizationofHosts"));
        details.setNumberOfActiveLoadBalancer(getInt(jn, "numberOfActiveLoadBalancer"));
        details.setNumberOfHostMachines(getInt(jn, "numberOfHostMachines"));
        details.setCluster(decodeCluster((ObjectNode) jn.get("cluster")));
        details.setNoOfuniqueCustomer(getInt(jn, "noOfuniqueCustomer"));
        details.setClusterStatus(getString(jn, "clusterStatus"));
        return details;
    }

    public static List<Cluster> decodeClusters(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("clusters") != null
                && (jn.get("clusters") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("clusters");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<Cluster> clusters = new ArrayList<Cluster>();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            Cluster cluster = decodeCluster((ObjectNode) recordNode);
            clusters.add(cluster);
        }
        return clusters;
    }

    public static Cluster decodeCluster(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("cluster") != null) {
            if (!(jn.get("cluster") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("cluster").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("cluster");
            }
        }
        Cluster cluster = new Cluster();
        cluster.setNumberOfLoadBalancingConfigurations(getInt(jn, "numberOfLoadBalancingConfigurations"));
        cluster.setNumberOfUniqueCustomers(getInt(jn, "numberOfUniqueCustomers"));
        cluster.setNumberOfHostMachines(getInt(jn, "numberOfHostMachines"));
        cluster.setClusterIpv6Cidr(getString(jn, "clusterIpv6Cidr"));
        cluster.setDataCenter(getDataCenter(jn, "dataCenter"));
        cluster.setDescription(getString(jn, "description"));
        cluster.setUtilization(getString(jn, "utilization"));
        cluster.setStatus(getClusterStatus(jn, "status"));
        cluster.setPassword(getString(jn, "password"));
        cluster.setUsername(getString(jn, "username"));
        cluster.setName(getString(jn, "name"));
        cluster.setId(getInt(jn, "id"));
        return cluster;
    }

    public static HostMachineDetails decodeHostMachineDetails(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("hostMachineDetails") != null) {
            if (!(jn.get("hostMachineDetails") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("hostMachineDetails").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("hostMachineDetails");
            }
        }
        HostMachineDetails details = new HostMachineDetails();
        details.setAvailableConcurrentConnections(getInt(jn, "availableConcurrentConnections"));
        details.setTotalConcurrentConnections(getInt(jn, "totalConcurrentConnections"));
        details.setActiveLBConfigurations(getLong(jn, "activeLBConfigurations"));
        details.setCurrentUtilization(getString(jn, "currentUtilization"));
        details.setUniqueCustomers(getInt(jn, "uniqueCustomers"));
        details.setHost(decodeHost((ObjectNode) jn.get("host")));
        return details;
    }

    public static CapacityPlanningVirtualIpBlocks decodeCapacityPlanningVirtualIpBlocks(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIpBlocks") != null) {
            if (!(jn.get("virtualIpBlocks") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("virtualIpBlocks").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("virtualIpBlocks");
            }
        }
        CapacityPlanningVirtualIpBlocks blocks = new CapacityPlanningVirtualIpBlocks();
        blocks.setReceltyAllocatedServiceNet(getLong(jn, "receltyAllocatedServiceNet"));
        blocks.setServiceNetdaysavailable(getLong(jn, "serviceNetdaysavailable"));
        blocks.setDeallocatedServiceNet(getLong(jn, "deallocatedServiceNet"));
        blocks.setConfiguredServiceNet(getLong(jn, "configuredServiceNet"));
        blocks.setAllocatedServiceNet(getLong(jn, "allocatedServiceNet"));
        blocks.setRecentlyAllocatedIP(getLong(jn, "recentlyAllocatedIP"));
        blocks.setIpdaysavailable(getLong(jn, "ipdaysavailable"));
        blocks.setFreeServiceNet(getLong(jn, "freeServiceNet"));
        blocks.setDeallocatiedIP(getLong(jn, "deallocatiedIP"));
        blocks.setConfiguredIP(getLong(jn, "configuredIP"));
        blocks.setAllocatedIP(getLong(jn, "allocatedIP"));
        blocks.setFreeIP(getLong(jn, "freeIP"));
        return blocks;
    }

    public static VirtualIpBlocks decodeVirtualIpBlocks(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("virtualIpBlocks") != null
                && (jn.get("virtualIpBlocks") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("virtualIpBlocks");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        VirtualIpBlocks virtualIpBlocks = new VirtualIpBlocks();
        for (i = 0; i < an.size(); i++) {
            JsonNode blockNode = an.get(i);
            if (!(blockNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, blockNode.toString());
                throw new JsonParseException(msg, blockNode.traverse().getTokenLocation());
            }
            VirtualIpBlock block = decodeVirtualIpBlock((ObjectNode) blockNode);
            virtualIpBlocks.getVirtualIpBlocks().add(block);
        }
        virtualIpBlocks.setType(JsonPublicDeserializers.getVipType(jn, "type"));
        return virtualIpBlocks;
    }

    public static VirtualIpBlock decodeVirtualIpBlock(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIpBlock") != null) {
            if (!(jn.get("virtualIpBlock") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("virtualIpBlock").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("virtualIpBlock");
            }
        }
        VirtualIpBlock block = new VirtualIpBlock();
        block.setFirstIp(getString(jn, "firstIp"));
        block.setLastIp(getString(jn, "lastIp"));
        return block;
    }

    public static Alerts decodeAlerts(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("alerts") != null
                && (jn.get("alerts") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("alerts");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Alerts alerts = new Alerts();
        for (i = 0; i < an.size(); i++) {
            JsonNode alertNode = an.get(i);
            if (!(alertNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, alertNode.toString());
                throw new JsonParseException(msg, alertNode.traverse().getTokenLocation());
            }
            Alert alert = decodeAlert((ObjectNode) alertNode);
            alerts.getAlerts().add(alert);
        }
        return alerts;
    }

    public static Alert decodeAlert(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("alert") != null) {
            if (!(jn.get("alert") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("alert").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("alert");
            }
        }
        Alert alert = new Alert();
        alert.setLoadbalancerId(getInt(jn, "loadbalancerId"));
        alert.setMessageName(getString(jn, "messageName"));
        alert.setAlertType(getString(jn, "alertType"));
        alert.setStatus(getAlertStatus(jn, "status"));
        alert.setAccountId(getInt(jn, "accountId"));
        alert.setMessage(getString(jn, "message"));
        alert.setCreated(getDate(jn, "created"));
        alert.setId(getInt(jn, "id"));
        return alert;
    }

    public static LoadBalancerLimitGroups decodeLoadBalancerLimitGroups(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerLimitGroups") != null
                && (jn.get("loadBalancerLimitGroups") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerLimitGroups");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        LoadBalancerLimitGroups groups = new LoadBalancerLimitGroups();
        for (i = 0; i < an.size(); i++) {
            JsonNode alertNode = an.get(i);
            if (!(alertNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, alertNode.toString());
                throw new JsonParseException(msg, alertNode.traverse().getTokenLocation());
            }
            LoadBalancerLimitGroup group = decodeLoadBalancerLimitGroup((ObjectNode) alertNode);
            groups.getLoadBalancerLimitGroups().add(group);
        }
        return groups;
    }

    public static LoadBalancerLimitGroup decodeLoadBalancerLimitGroup(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerLimitGroup") != null) {
            if (!(jn.get("loadBalancerLimitGroup") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerLimitGroup").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerLimitGroup");
            }
        }
        LoadBalancerLimitGroup group = new LoadBalancerLimitGroup();
        group.setAccounts(decodeAccounts(jn.get("accounts")));
        group.setIsDefault(getBoolean(jn, "isDefault"));
        group.setName(getString(jn, "name"));
        group.setLimit(getInt(jn, "limit"));
        group.setId(getInt(jn, "id"));
        return group;
    }

    public static Accounts decodeAccounts(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accounts") != null
                && (jn.get("accounts") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accounts");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Accounts accounts = new Accounts();
        for (i = 0; i < an.size(); i++) {
            JsonNode accountNode = an.get(i);
            if (!(accountNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, accountNode.toString());
                throw new JsonParseException(msg, accountNode.traverse().getTokenLocation());
            }
            Account account = decodeAccount((ObjectNode) accountNode);
            accounts.getAccounts().add(account);
        }
        return accounts;
    }

    public static Account decodeAccount(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("account") != null) {
            if (!(jn.get("account") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("account").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("account");
            }
        }
        Account account = new Account();
        account.setId(getInt(jn, "id"));
        return account;
    }

    public static Backups decodeBackups(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("backups") != null
                && (jn.get("backups") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("backups");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Backups backups = new Backups();
        for (i = 0; i < an.size(); i++) {
            JsonNode backupNode = an.get(i);
            if (!(backupNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, backupNode.toString());
                throw new JsonParseException(msg, backupNode.traverse().getTokenLocation());
            }
            Backup backup = decodeBackup((ObjectNode) backupNode);
            backups.getBackups().add(backup);
        }
        return backups;
    }

    public static Backup decodeBackup(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("backup") != null) {
            if (!(jn.get("backup") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("backup").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("backup");
            }
        }
        Backup backup = new Backup();
        backup.setBackupTime(getDate(jn, "backupTime"));
        backup.setHostId(getInt(jn, "hostId"));
        backup.setName(getString(jn, "name"));
        backup.setId(getInt(jn, "id"));
        return backup;
    }

    public static CustomerList decodeCustomerList(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("customerList") != null) {
            if (!(jn.get("customerList") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("customerList").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("customerList");
            }
        }
        CustomerList list = new CustomerList();
        list.setCustomers(decodeCustomers(jn.get("customers")));
        return list;
    }

    public static Customers decodeCustomers(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("customers") != null
                && (jn.get("customers") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("customers");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Customers customers = new Customers();
        for (i = 0; i < an.size(); i++) {
            JsonNode customerNode = an.get(i);
            if (!(customerNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, customerNode.toString());
                throw new JsonParseException(msg, customerNode.traverse().getTokenLocation());
            }
            Customer customer = decodeCustomer((ObjectNode) customerNode);
            customers.getCustomers().add(customer);
        }
        return customers;
    }

    public static Customer decodeCustomer(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("customer") != null) {
            if (!(jn.get("customer") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("customer").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("customer");
            }
        }
        Customer customer = new Customer();
        customer.setLoadBalancers(decodeLoadBalancers(jn.get("loadBalancers")));
        customer.setAccountId(getInt(jn, "accountId"));
        return customer;
    }

    public static HostCapacityReports decodeHostCapacityReports(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hostCapacityReports") != null
                && (jn.get("hostCapacityReports") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hostCapacityReports");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        HostCapacityReports reports = new HostCapacityReports();
        for (i = 0; i < an.size(); i++) {
            JsonNode reportNode = an.get(i);
            if (!(reportNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, reportNode.toString());
                throw new JsonParseException(msg, reportNode.traverse().getTokenLocation());
            }
            HostCapacityReport report = decodeHostCapacityReport((ObjectNode) reportNode);
            reports.getHostCapacityReports().add(report);
        }
        return reports;
    }

    public static HostCapacityReport decodeHostCapacityReport(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("hostCapacityReport") != null) {
            if (!(jn.get("hostCapacityReport") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("hostCapacityReport").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("hostCapacityReport");
            }
        }
        HostCapacityReport report = new HostCapacityReport();
        report.setAllocatedConcurrentConnectionsInLastSevenDays(getInt(jn, "allocatedConcurrentConnectionsInLastSevenDays"));
        report.setAllocatedConcurrentConnectionsToday(getInt(jn, "allocatedConcurrentConnectionsToday"));
        report.setTotalConcurrentConnectionCapacity(getInt(jn, "totalConcurrentConnectionCapacity"));
        report.setAllocatedConcurrentConnections(getInt(jn, "allocatedConcurrentConnections"));
        report.setAvailableConcurrentConnections(getInt(jn, "availableConcurrentConnections"));
        report.setRemainingDaysOfCapacity(getDouble(jn, "remainingDaysOfCapacity"));
        report.setHostName(getString(jn, "hostName"));
        report.setHostId(getInt(jn, "hostId"));
        return report;
    }

    public static VirtualIpAvailabilityReports decodeVirtualIpAvailabilityReports(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("virtualIpAvailabilityReports") != null
                && (jn.get("virtualIpAvailabilityReports") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("virtualIpAvailabilityReports");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        VirtualIpAvailabilityReports reports = new VirtualIpAvailabilityReports();
        for (i = 0; i < an.size(); i++) {
            JsonNode reportNode = an.get(i);
            if (!(reportNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, reportNode.toString());
                throw new JsonParseException(msg, reportNode.traverse().getTokenLocation());
            }
            VirtualIpAvailabilityReport report = decodeVirtualIpAvailabilityReport((ObjectNode) reportNode);
            reports.getVirtualIpAvailabilityReports().add(report);
        }
        return reports;
    }

    public static VirtualIpAvailabilityReport decodeVirtualIpAvailabilityReport(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIpAvailabilityReport") != null) {
            if (!(jn.get("virtualIpAvailabilityReport") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("virtualIpAvailabilityReport").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("virtualIpAvailabilityReport");
            }
        }
        VirtualIpAvailabilityReport report = new VirtualIpAvailabilityReport();
        report.setAllocatedServiceNetIpAddressesInLastSevenDays(getLong(jn, "allocatedServiceNetIpAddressesInLastSevenDays"));
        report.setAllocatedPublicIpAddressesInLastSevenDays(getLong(jn, "allocatedPublicIpAddressesInLastSevenDays"));
        report.setRemainingDaysOfServiceNetIpAddresses(getDouble(jn, "remainingDaysOfServiceNetIpAddresses"));
        report.setServiceNetIpAddressesAllocatedToday(getLong(jn, "serviceNetIpAddressesAllocatedToday"));
        report.setFreeAndClearServiceNetIpAddresses(getLong(jn, "freeAndClearServiceNetIpAddresses"));
        report.setRemainingDaysOfPublicIpAddresses(getDouble(jn, "remainingDaysOfPublicIpAddresses"));
        report.setPublicIpAddressesAllocatedToday(getLong(jn, "publicIpAddressesAllocatedToday"));
        report.setServiceNetIpAddressesInHolding(getLong(jn, "serviceNetIpAddressesInHolding"));
        report.setFreeAndClearPublicIpAddresses(getLong(jn, "freeAndClearPublicIpAddresses"));
        report.setPublicIpAddressesInHolding(getLong(jn, "publicIpAddressesInHolding"));
        report.setTotalServiceNetAddresses(getLong(jn, "totalServiceNetAddresses"));
        report.setTotalPublicIpAddresses(getLong(jn, "totalPublicIpAddresses"));
        report.setClusterName(getString(jn, "clusterName"));
        report.setClusterId(getInt(jn, "clusterId"));
        return report;
    }

    public static ZeusEvent decodeZeusEvent(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("zeusEvent") != null) {
            if (!(jn.get("zeusEvent") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("zeusEvent").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("zeusEvent");
            }
        }
        ZeusEvent event = new ZeusEvent();
        event.setEventType(getString(jn, "eventType"));
        event.setParamLine(getString(jn, "paramLine"));
        return event;
    }

    public static AccountBillings decodeAccountBillings(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountBillings") != null
                && (jn.get("accountBillings") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountBillings");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        AccountBillings billings = new AccountBillings();
        for (i = 0; i < an.size(); i++) {
            JsonNode billingNode = an.get(i);
            if (!(billingNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, billingNode.toString());
                throw new JsonParseException(msg, billingNode.traverse().getTokenLocation());
            }
            AccountBilling billing = JsonPublicDeserializers.decodeAccountBilling((ObjectNode) billingNode);
            billings.getAccountBillings().add(billing);
        }
        return billings;
    }

    public static AccountUsageRecords decodeAccountUsageRecords(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountUsageRecords") != null
                && (jn.get("accountUsageRecords") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountUsageRecords");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        AccountUsageRecords records = new AccountUsageRecords();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            AccountUsageRecord record = decodeAccountUsageRecord((ObjectNode) recordNode);
            records.getAccountUsageRecords().add(record);
        }
        return records;
    }

    public static AccountUsageRecord decodeAccountUsageRecord(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountUsageRecord") != null) {
            if (!(jn.get("accountUsageRecord") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountUsageRecord").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountUsageRecord");
            }
        }
        AccountUsageRecord record = new AccountUsageRecord();
        record.setNumServicenetVips(getInt(jn, "numServicenetVips"));
        record.setNumLoadBalancers(getInt(jn, "numLoadBalancers"));
        record.setNumPublicVips(getInt(jn, "numPublicVips"));
        record.setStartTime(getDate(jn, "startTime"));
        record.setAccountId(getInt(jn, "accountId"));
        return record;
    }

    public static LoadBalancerUsageRecords decodeLoadBalancerUsageRecords(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerUsageRecords") != null
                && (jn.get("loadBalancerUsageRecords") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerUsageRecords");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        LoadBalancerUsageRecords records = new LoadBalancerUsageRecords();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            LoadBalancerUsageRecord record = decodeLoadBalancerUsageRecord((ObjectNode) recordNode);
            records.getLoadBalancerUsageRecords().add(record);
        }
        return records;
    }

    public static LoadBalancerUsageRecord decodeLoadBalancerUsageRecord(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerUsageRecord") != null) {
            if (!(jn.get("loadBalancerUsageRecord") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerUsageRecord").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerUsageRecord");
            }
        }
        LoadBalancerUsageRecord record = new LoadBalancerUsageRecord();
        record.setAverageNumConnectionsSsl(getDouble(jn, "averageNumConnectionsSsl"));
        record.setAverageNumConnections(getDouble(jn, "averageNumConnections"));
        record.setVipType(JsonPublicDeserializers.getVipType(jn, "vipType"));
        record.setIncomingTransferSsl(getLong(jn, "incomingTransferSsl"));
        record.setOutgoingTransferSsl(getLong(jn, "outgoingTransferSsl"));
        record.setIncomingTransfer(getLong(jn, "incomingTransfer"));
        record.setOutgoingTransfer(getLong(jn, "outgoingTransfer"));
        record.setLoadBalancerId(getInt(jn, "loadBalancerId"));
        record.setEventType(getString(jn, "eventType"));
        record.setStartTime(getDate(jn, "startTime"));
        record.setAccountId(getInt(jn, "accountId"));
        record.setSslMode(getString(jn, "sslMode"));
        record.setNumPolls(getInt(jn, "numPolls"));
        record.setEndTime(getDate(jn, "endTime"));
        record.setNumVips(getInt(jn, "numVips"));
        record.setId(getInt(jn, "id"));
        return record;
    }

    //Todo: Double check the logic all the way down the line of this set of nested objects to make sure all the lists are populated
    public static Hostssubnet decodeHostsSubnet(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hostssubnet") != null
                && (jn.get("hostssubnet") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hostssubnet");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Hostssubnet subnets = new Hostssubnet();
        for (i = 0; i < an.size(); i++) {
            JsonNode subnetNode = an.get(i);
            if (!(subnetNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, subnetNode.toString());
                throw new JsonParseException(msg, subnetNode.traverse().getTokenLocation());
            }
            Hostsubnet subnet = decodeHostSubnet(subnetNode);
            subnets.getHostsubnets().add(subnet);
        }
        return subnets;
    }

    public static Hostsubnet decodeHostSubnet(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hostsubnet") != null
                && (jn.get("hostsubnet") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hostsubnet");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Hostsubnet subnet = new Hostsubnet();
        for (i = 0; i < an.size(); i++) {
            JsonNode interfaceNode = an.get(i);
            if (!(interfaceNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, interfaceNode.toString());
                throw new JsonParseException(msg, interfaceNode.traverse().getTokenLocation());
            }
            NetInterface netInterface = decodeNetInterface((ObjectNode) interfaceNode);
            subnet.getNetInterfaces().add(netInterface);
        }
        subnet.setName(getString(jn, "name"));
        return subnet;
    }

    public static NetInterface decodeNetInterface(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("netInterface") != null
                && (jn.get("netInterface") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("netInterface");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        NetInterface netInterface = new NetInterface();
        for (i = 0; i < an.size(); i++) {
            JsonNode cidrNode = an.get(i);
            if (!(cidrNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, cidrNode.toString());
                throw new JsonParseException(msg, cidrNode.traverse().getTokenLocation());
            }
            Cidr cidr = decodeCidr((ObjectNode) cidrNode);
            netInterface.getCidrs().add(cidr);
        }
        netInterface.setName(getString(jn, "name"));
        return netInterface;
    }

    public static CidrTest decodeCidrTest(JsonNode jn) throws JsonParseException    {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("cidrTest") != null
                && (jn.get("cidrTest") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("cidrTest");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        CidrTest cidrTest = new CidrTest();
        for (i = 0; i < an.size(); i++) {
            JsonNode cidrNode = an.get(i);
            if (!(cidrNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, cidrNode.toString());
                throw new JsonParseException(msg, cidrNode.traverse().getTokenLocation());
            }
            Cidr cidr = decodeCidr((ObjectNode) cidrNode);
            cidrTest.getCidrBlocks().add(cidr);
        }
        cidrTest.setIpVersion(getIpVersion((ObjectNode) jn, "ipVersion"));
        cidrTest.setIpAddress(getString(jn, "ipAddress"));
        return cidrTest;
    }

    public static Cidr decodeCidr(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("cidr") != null) {
            if (!(jn.get("cidr") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("cidr").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("cidr");
            }
        }
        Cidr cidr = new Cidr();
        cidr.setBlock(getString(jn, "block"));
        return cidr;
    }

    public static LoadBalancerAudits decodeLoadBalancerAudits(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerAudits") != null
                && (jn.get("loadBalancerAudits") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerAudits");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        LoadBalancerAudits audits = new LoadBalancerAudits();
        for (i = 0; i < an.size(); i++) {
            JsonNode auditNode = an.get(i);
            if (!(auditNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, auditNode.toString());
                throw new JsonParseException(msg, auditNode.traverse().getTokenLocation());
            }
            LoadBalancerAudit audit = decodeLoadBalancerAudit(auditNode);
            audits.getLoadBalancerAudits().add(audit);
        }
        return audits;
    }

    //Todo: figure out the proper way to handle the list of alerts
    public static LoadBalancerAudit decodeLoadBalancerAudit(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerAudit") != null
                && (jn.get("loadBalancerAudit") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerAudit");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        LoadBalancerAudit audit = new LoadBalancerAudit();
        for (i = 0; i < an.size(); i++) {
            JsonNode alertNode = an.get(i);
            if (!(alertNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, alertNode.toString());
                throw new JsonParseException(msg, alertNode.traverse().getTokenLocation());
            }
            Alerts alerts = decodeAlerts((ObjectNode) alertNode);
            audit.getAlertAudits().add(alerts);
        }
        audit.setCreated(getDate(jn, "created"));
        audit.setUpdated(getDate(jn, "updated"));
        audit.setStatus(getString(jn, "status"));
        audit.setId(getInt(jn, "id"));
        return audit;
    }

    public static HostUsageList decodeHostUsageList(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hostUsageRecords") != null
                && (jn.get("hostUsageRecords") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hostUsageRecords");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        HostUsageList list = new HostUsageList();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            HostUsageRecord record = decodeHostUsageRecord((ObjectNode) recordNode);
            list.getHostUsageRecords().add(record);
        }
        return list;
    }

    public static HostUsageRecord decodeHostUsageRecord(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("hostUsageRecord") != null) {
            if (!(jn.get("hostUsageRecord") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("hostUsageRecord").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("hostUsageRecord");
            }
        }
        HostUsageRecord record = new HostUsageRecord();
        record.setHostId(getInt(jn, "hostId"));
        record.setHostUsages(decodeHostUsages(jn.get("hostUsages")));
        return record;
    }

    public static HostUsages decodeHostUsages(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("hostUsages") != null
                && (jn.get("hostUsages") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("hostUsages");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        HostUsages usages = new HostUsages();
        for (i = 0; i < an.size(); i++) {
            JsonNode usageNode = an.get(i);
            if (!(usageNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, usageNode.toString());
                throw new JsonParseException(msg, usageNode.traverse().getTokenLocation());
            }
            HostUsage usage = decodeHostUsage((ObjectNode) usageNode);
            usages.getHostUsages().add(usage);
        }
        return usages;
    }

    public static HostUsage decodeHostUsage(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("hostUsage") != null) {
            if (!(jn.get("hostUsage") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("hostUsage").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("hostUsage");
            }
        }
        HostUsage usage = new HostUsage();
        usage.setBandwidthIn(getLong(jn, "bandwidthIn"));
        usage.setBandwidthOut(getLong(jn, "bandwidthOut"));
        usage.setDay(getDate(jn, "day"));
        return usage;
    }

    public static AccountGroups decodeAccountGroups(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("accountGroups") != null
                && (jn.get("accountGroups") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accountGroups");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        AccountGroups groups = new AccountGroups();
        for (i = 0; i < an.size(); i++) {
            JsonNode groupNode = an.get(i);
            if (!(groupNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, groupNode.toString());
                throw new JsonParseException(msg, groupNode.traverse().getTokenLocation());
            }
            AccountGroup usage = decodeAccountGroup((ObjectNode) groupNode);
            groups.getAccountGroups().add(usage);
        }
        return groups;
    }

    public static AccountGroup decodeAccountGroup(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountGroup") != null) {
            if (!(jn.get("accountGroup") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountGroup").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountGroup");
            }
        }
        AccountGroup group = new AccountGroup();
        group.setId(getInt(jn, "id"));
        group.setAccountId(getInt(jn, "accountId"));
        group.setGroup(decodeGroup((ObjectNode) jn.get("group")));
        return group;
    }

    public static GroupRateLimits decodeGroupRateLimits(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("groupRateLimits") != null
                && (jn.get("groupRateLimits") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("groupRateLimits");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        GroupRateLimits limits = new GroupRateLimits();
        for (i = 0; i < an.size(); i++) {
            JsonNode limitNode = an.get(i);
            if (!(limitNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, limitNode.toString());
                throw new JsonParseException(msg, limitNode.traverse().getTokenLocation());
            }
            GroupRateLimit limit = decodeGroup((ObjectNode) limitNode);
            limits.getGroupRateLimits().add(limit);
        }
        return limits;
    }

    public static GroupRateLimit decodeGroup(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("groupRateLimit") != null) {
            if (!(jn.get("groupRateLimit") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("groupRateLimit").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("groupRateLimit");
            }
        }
        GroupRateLimit limit = new GroupRateLimit();
        limit.setDescription(getString(jn, "description"));
        limit.setIsDefault(getBoolean(jn, "isDefault"));
        limit.setName(getString(jn, "name"));
        limit.setId(getInt(jn, "id"));
        return limit;
    }

    public static LdapInfo decodeLdapInfo(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("ldapInfo") != null) {
            if (!(jn.get("ldapInfo") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("ldapInfo").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("ldapInfo");
            }
        }
        LdapInfo info = new LdapInfo();
        info.getLdapGroups().addAll(decodeLdapGroups(jn.get("ldapGroup")));
        info.getUserRoles().addAll(decodeUserRoles(jn.get("userRole")));
        info.setUserName(getString(jn, "userName"));
        return info;
    }

    private static List<LdapGroup> decodeLdapGroups(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("ldapGroup") != null
                && (jn.get("ldapGroup") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("ldapGroup");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<LdapGroup> groups = new ArrayList<LdapGroup>();
        for (i = 0; i < an.size(); i++) {
            JsonNode groupNode = an.get(i);
            if (!(groupNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, groupNode.toString());
                throw new JsonParseException(msg, groupNode.traverse().getTokenLocation());
            }
            LdapGroup limit = decodeLdapGroup((ObjectNode) groupNode);
            groups.add(limit);
        }
        return groups;
    }

    public static LdapGroup decodeLdapGroup(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("ldapGroup") != null) {
            if (!(jn.get("ldapGroup") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("ldapGroup").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("ldapGroup");
            }
        }
        LdapGroup group = new LdapGroup();
        group.setGroup(getString(jn, "group"));
        return group;
    }

    private static List<UserRole> decodeUserRoles(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("userRole") != null
                && (jn.get("userRole") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("userRole");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<UserRole> roles = new ArrayList<UserRole>();
        for (i = 0; i < an.size(); i++) {
            JsonNode roleNode = an.get(i);
            if (!(roleNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, roleNode.toString());
                throw new JsonParseException(msg, roleNode.traverse().getTokenLocation());
            }
            UserRole role = decodeUserRole((ObjectNode) roleNode);
            roles.add(role);
        }
        return roles;
    }

    public static UserRole decodeUserRole(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("userRole") != null) {
            if (!(jn.get("userRole") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("userRole").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("userRole");
            }
        }
        UserRole role = new UserRole();
        role.setRoleName(getString(jn, "roleName"));
        return role;
    }

    public static LoadBalancerEvent decodeLoadBalancerEvent(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerEvent") != null) {
            if (!(jn.get("loadBalancerEvent") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerEvent").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerEvent");
            }
        }
        LoadBalancerEvent role = (LoadBalancerEvent) decodeEvent(jn);
        return role;
    }

    public static AccountLoadBalancerServiceEvents decodeAccountLoadBalancerServiceEvents(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountLoadBalancerServiceEvents") != null) {
            if (!(jn.get("accountLoadBalancerServiceEvents") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountLoadBalancerServiceEvents").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountLoadBalancerServiceEvents");
            }
        }
        AccountLoadBalancerServiceEvents events = new AccountLoadBalancerServiceEvents();
        events.getLoadBalancerServiceEvents().addAll(decodeLoadBalanacerServiceEventsList(jn.get("loadBalancerServiceEvents")));
        events.setAccountId(getInt(jn, "accountId"));
        return events;
    }

    private static List<LoadBalancerServiceEvents> decodeLoadBalanacerServiceEventsList(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("userRole") != null
                && (jn.get("userRole") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("userRole");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<LoadBalancerServiceEvents> list = new ArrayList<LoadBalancerServiceEvents>();
        for (i = 0; i < an.size(); i++) {
            JsonNode listNode = an.get(i);
            if (!(listNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, listNode.toString());
                throw new JsonParseException(msg, listNode.traverse().getTokenLocation());
            }
            LoadBalancerServiceEvents events = decodeLoadBalancerServiceEvents(listNode);
            list.add(events);
        }
        return list;
    }

    public static ZeusRateLimitedLoadBalancers decodeZeusRateLimitedLoadBalancers(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("zeusRateLimitedLoadBalancers") != null
                && (jn.get("zeusRateLimitedLoadBalancers") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("zeusRateLimitedLoadBalancers");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        ZeusRateLimitedLoadBalancers loadBalancers = new ZeusRateLimitedLoadBalancers();
        for (i = 0; i < an.size(); i++) {
            JsonNode listNode = an.get(i);
            if (!(listNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, listNode.toString());
                throw new JsonParseException(msg, listNode.traverse().getTokenLocation());
            }
            ZeusRateLimitedLoadBalancer events = decodeZeusRateLimitedLoadBalancer((ObjectNode) listNode);
            loadBalancers.getZeusRateLimitedLoadBalancers().add(events);
        }
        return loadBalancers;
    }

    public static ZeusRateLimitedLoadBalancer decodeZeusRateLimitedLoadBalancer(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("zeusRateLimitedLoadBalancer") != null) {
            if (!(jn.get("zeusRateLimitedLoadBalancer") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("zeusRateLimitedLoadBalancer").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("zeusRateLimitedLoadBalancer");
            }
        }
        ZeusRateLimitedLoadBalancer loadBalancer = new ZeusRateLimitedLoadBalancer();
        loadBalancer.setMaxRequestsPerSecond(getInt(jn, "maxRequestsPerSecond"));
        loadBalancer.setExpirationTime(getString(jn, "expirationTime"));
        loadBalancer.setLoadbalancerId(getInt(jn, "loadbalancerId"));
        loadBalancer.setTickets(decodeTickets(jn.get("tickets")));
        loadBalancer.setAccountId(getInt(jn, "accountId"));
        return loadBalancer;
    }

    public static CustomLimitAccounts decodeCustomLimitAccounts(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("customLimitAccounts") != null
                && (jn.get("customLimitAccounts") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("customLimitAccounts");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        CustomLimitAccounts accounts = new CustomLimitAccounts();
        for (i = 0; i < an.size(); i++) {
            JsonNode accountNode = an.get(i);
            if (!(accountNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, accountNode.toString());
                throw new JsonParseException(msg, accountNode.traverse().getTokenLocation());
            }
            CustomLimitAccount account = decodeCustomLimitAccount((ObjectNode) accountNode);
            accounts.getCustomLimitAccounts().add(account);
        }
        return accounts;
    }

    public static CustomLimitAccount decodeCustomLimitAccount(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("customLimitAccount") != null) {
            if (!(jn.get("customLimitAccount") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("customLimitAccount").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("customLimitAccount");
            }
        }
        CustomLimitAccount account = new CustomLimitAccount();
        account.setAccountId(getInt(jn, "accountId"));
        account.setCustomLimits(decodeCustomLimits(jn.get("customLimits")));
        return account;
    }

    public static AllAbsoluteLimits decodeAllAbsoluteLimits(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("allAbsoluteLimits") != null) {
            if (!(jn.get("allAbsoluteLimits") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("allAbsoluteLimits").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("allAbsoluteLimits");
            }
        }
        AllAbsoluteLimits limits = new AllAbsoluteLimits();
        limits.setCustomLimits(decodeDefaultLimits(jn.get("defaultLimits")));
        limits.setCustomLimits(decodeCustomLimits(jn.get("customLimits")));
        return limits;
    }

    private static List<Limit> decodeCustomLimits(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("customLimits") != null
                && (jn.get("customLimits") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("customLimits");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<Limit> limits = new ArrayList<Limit>();
        for (i = 0; i < an.size(); i++) {
            JsonNode limitNode = an.get(i);
            if (!(limitNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, limitNode.toString());
                throw new JsonParseException(msg, limitNode.traverse().getTokenLocation());
            }
            Limit account = decodeLimit((ObjectNode) limitNode);
            limits.add(account);
        }
        return limits;
    }

    private static List<Limit> decodeDefaultLimits(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("defaultLimits") != null
                && (jn.get("defaultLimits") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("defaultLimits");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<Limit> limits = new ArrayList<Limit>();
        for (i = 0; i < an.size(); i++) {
            JsonNode limitNode = an.get(i);
            if (!(limitNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, limitNode.toString());
                throw new JsonParseException(msg, limitNode.traverse().getTokenLocation());
            }
            Limit account = decodeLimit((ObjectNode) limitNode);
            limits.add(account);
        }
        return limits;
    }

    public static Limit decodeLimit(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("limit") != null) {
            if (!(jn.get("limit") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("limit").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("limit");
            }
        }
        Limit limit = new Limit();
        limit.setName(getString(jn, "name"));
        limit.setValue(getInt(jn, "value"));
        limit.setId(getInt(jn, "id"));
        return limit;
    }

    public static Jobs decodeJobs(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("jobs") != null
                && (jn.get("jobs") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("jobs");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        Jobs jobs = new Jobs();
        for (i = 0; i < an.size(); i++) {
            JsonNode jobNode = an.get(i);
            if (!(jobNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jobNode.toString());
                throw new JsonParseException(msg, jobNode.traverse().getTokenLocation());
            }
            Job job = decodeJob((ObjectNode) jobNode);
            jobs.getJobs().add(job);
        }
        return jobs;
    }

    public static Job decodeJob(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("job") != null) {
            if (!(jn.get("job") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("job").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("job");
            }
        }
        Job job = new Job();
        job.setInputPath(getString(jn, "inputPath"));
        job.setStartTime(getDate(jn, "startTime"));
        job.setJobName(getString(jn, "jobName"));
        job.setEndTime(getDate(jn, "endTime"));
        job.setState(getString(jn, "state"));
        job.setId(getInt(jn, "id"));
        return job;
    }

    public static HealthChecks decodeHealthChecks(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("healthChecks") != null
                && (jn.get("healthChecks") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("healthChecks");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        HealthChecks checks = new HealthChecks();
        for (i = 0; i < an.size(); i++) {
            JsonNode checkNode = an.get(i);
            if (!(checkNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, checkNode.toString());
                throw new JsonParseException(msg, checkNode.traverse().getTokenLocation());
            }
            HealthCheck check = decodeHealthCheck((ObjectNode) checkNode);
            checks.getHealthChecks().add(check);
        }
        return checks;
    }

    public static HealthCheck decodeHealthCheck(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("healthCheck") != null) {
            if (!(jn.get("healthCheck") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("healthCheck").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("healthCheck");
            }
        }
        HealthCheck check = new HealthCheck();
        check.setMessage(getString(jn, "message"));
        check.setStatus(getString(jn, "status"));
        check.setType(getString(jn, "type"));
        check.setTime(getLong(jn, "time"));
        return check;
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

    public static DataCenter getDataCenter(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String dcString = getString(jsonNodeIn, prop);
        DataCenter dc;
        if (dcString == null) {
            return null;
        }
        try {
            dc = DataCenter.fromValue(dcString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal data center found %s in %s", dcString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return dc;
    }

    public static BlacklistType getBlacklistType(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String typeString = getString(jsonNodeIn, prop);
        BlacklistType type;
        if (typeString == null) {
            return null;
        }
        try {
            type = BlacklistType.fromValue(typeString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal blacklist item type found %s in %s", typeString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return type;
    }

    public static IpVersion getIpVersion(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String dcString = getString(jsonNodeIn, prop);
        IpVersion version;
        if (dcString == null) {
            return null;
        }
        try {
            version = IpVersion.fromValue(dcString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal ip version found %s in %s", dcString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return version;
    }

    public static ClusterStatus getClusterStatus(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String statusString = getString(jsonNodeIn, prop);
        ClusterStatus status;
        if (statusString == null) {
            return null;
        }
        try {
            status = ClusterStatus.fromValue(statusString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal cluster status found %s in %s", statusString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return status;
    }

    public static AlertStatus getAlertStatus(ObjectNode jsonNodeIn, String prop) throws JsonParseException {
        String statusString = getString(jsonNodeIn, prop);
        AlertStatus status;
        if (statusString == null) {
            return null;
        }
        try {
            status = AlertStatus.fromValue(statusString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal alert status found %s in %s", statusString, jsonNodeIn.toString());
            throw new JsonParseException(msg, jsonNodeIn.traverse().getCurrentLocation());
        }
        return status;
    }
}
