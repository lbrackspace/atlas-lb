package org.openstack.atlas.api.helpers.JsonUtils;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.Algorithms;
import org.openstack.atlas.docs.loadbalancers.api.v1.Algorithm;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.ContentCaching;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvent;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Protocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.Protocols;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;

public class JsonPublicDeserializers {

    private static final String NOT_OBJ_NODE = "Error was expecting an ObjectNode({}) but instead found %s";
    private static final String NOT_OBJ_OR_ARR = "Error was expecting an ObjectNode({}) or an ArrayNode([]) but found %s";

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
        lb.setId(getInt(jn, "id"));
        lb.setName(getString(jn, "name"));
        lb.setNodeCount(getInt(jn, "nodeCount"));
        lb.setPort(getInt(jn, "port"));
        lb.setTimeout(getInt(jn, "timeout"));
        lb.setHalfClosed(getBoolean(jn, "halfClosed"));
        lb.setIsSticky(getBoolean(jn, "isSticky"));
        lb.setAlgorithm(getString(jn, "algorithm"));
        lb.setProtocol(getString(jn, "protocol"));
        lb.setStatus(getString(jn, "status"));

        if (jn.get("connectionLogging") != null) {
            lb.setConnectionLogging(decodeConnectionLogging((ObjectNode) jn.get("connectionLogging")));
        }
        if (jn.get("connectionThrottle") != null) {
            lb.setConnectionThrottle(decodeConnectionThrottle((ObjectNode) jn.get("connectionThrottle")));
        }
        if (jn.get("contentCaching") != null) {
            lb.setContentCaching(decodeContentCaching((ObjectNode) jn.get("contentCaching")));
        }
        if (jn.get("healthMonitor") != null) {
            lb.setHealthMonitor(decodeHealthMonitor((ObjectNode) jn.get("healthMonitor")));
        }
        if (jn.get("loadBalancerUsage") != null) {
            lb.setLoadBalancerUsage(decodeLoadBalancerUsage((ObjectNode) jn.get("loadBalancerUsage")));
        }
        if (jn.get("sessionPersistence") != null) {
            lb.setSessionPersistence(decodeSessionPersistence((ObjectNode) jn.get("sessionPersistence")));
        }
        if (jn.get("sourceAddresses") != null) {
            lb.setSourceAddresses(decodeSourceAddresses((ObjectNode) jn.get("sourceAddresses")));
        }
        if (jn.get("sslTermination") != null) {
            lb.setSslTermination(decodeSslTermination((ObjectNode) jn.get("sslTermination")));
        }
        if (jn.get("created") != null) {
            lb.setCreated(getCreated((ObjectNode) jn.get("created")));
        }
        if (jn.get("updated") != null) {
            lb.setUpdated(getUpdated((ObjectNode) jn.get("updated")));
        }
        if (jn.get("cluster") != null) {
            lb.setCluster(decodeCluster((ObjectNode) jn.get("cluster")));
        }
        if (jn.get("accessList") != null) {
            lb.setAccessList(decodeAccessList(jn.get("accessList")));
        }
        if (jn.get("nodes") != null) {
            lb.setNodes(decodeNodes(jn.get("nodes")));
        }
        if (jn.get("virtualIps") != null) {
            lb.setVirtualIps(decodeVirtualIps(jn.get("virtualIps")));
        }
        if (jn.get("metadata") != null) {
            lb.setMetadata(decodeMetadata(jn.get("metadata")));
        }
        return lb;
    }

    public static SslTermination decodeSslTermination(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("sslTermination") != null) {
            if (!(jn.get("sslTermination") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("sslTermination").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("sslTermination");
            }
        }
        SslTermination termination = new SslTermination();
        termination.setId(getInt(jn, "id"));
        termination.setSecureTrafficOnly(getBoolean(jn, "secureTrafficOnly"));
        termination.setEnabled(getBoolean(jn, "enabled"));
        termination.setSecurePort(getInt(jn, "securePort"));
        termination.setCertificate(getString(jn, "certificate"));
        termination.setPrivatekey(getString(jn, "privateKey"));
        termination.setIntermediateCertificate(getString(jn, "intermediateCertificate"));
        return termination;
    }

    public static SessionPersistence decodeSessionPersistence(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("sessionPersistence") != null) {
            if (!(jn.get("sessionPersistence") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("sessionPersistence").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("sessionPersistence");
            }
        }
        SessionPersistence persistence = new SessionPersistence();
        persistence.setPersistenceType(getPersistenceType(jn, "persistenceType"));
        return persistence;
    }

    public static HealthMonitor decodeHealthMonitor(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("healthMonitor") != null) {
            if (!(jn.get("healthMonitor") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("healthMonitor").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("healthMonitor");
            }
        }

        HealthMonitor monitor = new HealthMonitor();
        monitor.setAttemptsBeforeDeactivation(getInt(jn, "attemptsBeforeDeactivation"));
        monitor.setTimeout(getInt(jn, "timeout"));
        monitor.setDelay(getInt(jn, "delay"));
        monitor.setId(getInt(jn, "id"));
        monitor.setStatusRegex(getString(jn, "statusRegex"));
        monitor.setHostHeader(getString(jn, "hostHeader"));
        monitor.setBodyRegex(getString(jn, "bodyRegex"));
        monitor.setPath(getString(jn, "path"));
        monitor.setType(getHealthMonitorType(jn, "healthMonitorType"));
        return monitor;
    }

    public static ConnectionLogging decodeConnectionLogging(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("connectionLogging") != null) {
            if (!(jn.get("connectionLogging") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("connectionLogging").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("connectionLogging");
            }
        }
        ConnectionLogging logging = new ConnectionLogging();
        logging.setEnabled(getBoolean(jn, "enabled"));
        return logging;
    }

    public static ContentCaching decodeContentCaching(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("contentCaching") != null) {
            if (!(jn.get("contentCaching") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("contentCaching").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("contentCaching");
            }
        }
        ContentCaching caching = new ContentCaching();
        caching.setEnabled(getBoolean(jn, "enabled"));
        return caching;
    }

    public static ConnectionThrottle decodeConnectionThrottle(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("connectionThrottle") != null) {
            if (!(jn.get("connectionThrottle") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("connectionThrottle").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("connectionThrottle");
            }
        }
        ConnectionThrottle throttle = new ConnectionThrottle();
        throttle.setMaxConnectionRate(getInt(jn, "maxConnectionRate"));
        throttle.setMaxConnections(getInt(jn, "maxConnections"));
        throttle.setMinConnections(getInt(jn, "minConnections"));
        throttle.setRateInterval(getInt(jn, "rateInterval"));
        return throttle;
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
        cluster.setName(getString(jn, "name"));
        return cluster;
    }

    public static SourceAddresses decodeSourceAddresses(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("sourceAddresses") != null) {
            if (!(jn.get("sourceAddresses") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("sourceAddresses").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("sourceAddresses");
            }
        }
        SourceAddresses addresses = new SourceAddresses();
        addresses.setIpv4Public(getString(jn, "ipv4Public"));
        addresses.setIpv6Public(getString(jn, "ipv6Public"));
        addresses.setIpv4Servicenet(getString(jn, "ipv4Servicenet"));
        addresses.setIpv6Servicenet(getString(jn, "ipv6Servicenet"));
        return addresses;
    }

    public static List<AccountBilling> decodeAccountBillings(JsonNode jn) throws JsonParseException {
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
        List<AccountBilling> billings = new ArrayList<AccountBilling>();
        for (i = 0; i < an.size(); i++) {
            JsonNode billingNode = an.get(i);
            if (!(billingNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, billingNode.toString());
                throw new JsonParseException(msg, billingNode.traverse().getTokenLocation());
            }
            AccountBilling billing = decodeAccountBilling((ObjectNode) billingNode);
            billings.add(billing);
        }
        return billings;
    }

    public static AccountBilling decodeAccountBilling(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountBilling") != null) {
            if (!(jn.get("accountBilling") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountBilling").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountBilling");
            }
        }
        AccountBilling billing = new AccountBilling();
        billing.setAccountId(getInt(jn, "accountId"));
        billing.setAccountUsage(decodeAccountUsage((ObjectNode) jn.get("accountUsage")));
        if (jn.get("loadBalancerUsages") != null) {
            billing.getLoadBalancerUsages().addAll(decodeLoadBalancerUsages(jn.get("loadBalancerUsages")));
        }
        return billing;
    }

    public static AccountUsage decodeAccountUsage(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("accountUsage") != null) {
            if (!(jn.get("accountUsage") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("accountUsage").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("accountUsage");
            }
        }
        AccountUsage usage = new AccountUsage();
        if (jn.get("accountUsageRecords") != null) {
            usage.getAccountUsageRecords().addAll(decodeAccountUsageRecords(jn.get("accountUsageRecords")));
        }
        return usage;
    }

    public static List<AccountUsageRecord> decodeAccountUsageRecords(JsonNode jn) throws JsonParseException {
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
        List<AccountUsageRecord> records = new ArrayList<AccountUsageRecord>();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            AccountUsageRecord record = decodeAccountUsageRecord((ObjectNode) recordNode);
            records.add(record);
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
        record.setNumLoadBalancers(getInt(jn, "numLoadBalancers"));
        record.setNumPublicVips(getInt(jn, "numPublicVips"));
        record.setNumServicenetVips(getInt(jn, "numServicenetVips"));
        record.setStartTime(decodeDate(jn, "startTime"));
        return record;
    }

    public static List<LoadBalancerUsage> decodeLoadBalancerUsages(JsonNode jn) throws JsonParseException {
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("loadBalancerUsages") != null
                && (jn.get("loadBalancerUsages") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("loadBalancerUsages");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        List<LoadBalancerUsage> usages = new ArrayList<LoadBalancerUsage>();
        for (i = 0; i < an.size(); i++) {
            JsonNode usageNode = an.get(i);
            if (!(usageNode instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", usageNode.toString());
                throw new JsonParseException(msg, usageNode.traverse().getTokenLocation());
            }
            LoadBalancerUsage usage = decodeLoadBalancerUsage((ObjectNode) usageNode);
            usages.add(usage);
        }
        return usages;
    }

    public static LoadBalancerUsage decodeLoadBalancerUsage(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("loadBalancerUsage") != null) {
            if (!(jn.get("loadBalancerUsage") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("loadBalancerUsage").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("loadBalancerUsage");
            }
        }
        LoadBalancerUsage usage = new LoadBalancerUsage();
        usage.setLoadBalancerId(getInt(jn, "loadBalancerId"));
        usage.setLoadBalancerName(getString(jn, "loadBalancerName"));
        if (jn.get("loadBalancerUsageRecords") != null) {
            usage.getLoadBalancerUsageRecords().addAll(decodeLoadBalancerUsageRecords(jn.get("loadBalancerUsageRecords")));
        }
        return usage;
    }

    public static List<LoadBalancerUsageRecord> decodeLoadBalancerUsageRecords(JsonNode jn) throws JsonParseException {
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
        List<LoadBalancerUsageRecord> records = new ArrayList<LoadBalancerUsageRecord>();
        for (i = 0; i < an.size(); i++) {
            JsonNode recordNode = an.get(i);
            if (!(recordNode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, recordNode.toString());
                throw new JsonParseException(msg, recordNode.traverse().getTokenLocation());
            }
            LoadBalancerUsageRecord record = decodeLoadBalancerUsageRecord((ObjectNode) recordNode);
            records.add(record);
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
        record.setIncomingTransferSsl(getLong(jn, "incomingTransferSsl"));
        record.setOutgoingTransferSsl(getLong(jn, "outgoingTransferSsl"));
        record.setIncomingTransfer(getLong(jn, "incomingTransfer"));
        record.setOutgoingTransfer(getLong(jn, "outgoingTransfer"));
        record.setEntryVersion(getInt(jn, "entryVersion"));
        record.setNeedsPushed(getInt(jn, "needsPushed"));
        record.setEventType(getString(jn, "eventType"));
        record.setStartTime(decodeDate(jn, "startTime"));
        record.setNumPolls(getInt(jn, "numPolls"));
        record.setNumVips(getInt(jn, "numVips"));
        record.setSslMode(getString(jn, "sslMode"));
        record.setVipType(getVipType(jn, "vipType"));
        record.setEndTime(decodeDate(jn, "endTime"));
        record.setUuid(getString(jn, "uuid"));
        record.setId(getInt(jn, "id"));
        return record;
    }

    public static VirtualIps decodeVirtualIps(JsonNode jn) throws JsonParseException {
        VirtualIps virtualIps = new VirtualIps();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("virtualIps") != null
                && (jn.get("virtualIps") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("virtualIps"); // Strip the root node if its there
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode vipNode = an.get(i);
            if (!(vipNode instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", an.get(i).toString());
                throw new JsonParseException(msg, an.get(i).traverse().getTokenLocation());
            }
            VirtualIp virtualIp = decodeVirtualIp((ObjectNode) an.get(i));
            virtualIps.getVirtualIps().add(virtualIp);
            // Links is ignored.
        }
        return virtualIps;
    }

    public static Calendar decodeDate(JsonNode jn, String prop) throws JsonParseException {
        Calendar out;
        if (jn.get(prop) != null && jn.get(prop).isTextual()) {
            String dateString = jn.get(prop).getTextValue();
            try {
                out = DateTimeConverters.isoTocal(dateString);
                return out;
            } catch (ConverterException ex) {
                String msg = String.format("Error converting %s to Date. Value must be in anISO 8601", dateString);
                throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
            }
        }
        return null;
    }

    public static VirtualIp decodeVirtualIp(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIp") != null) {
            if (!(jn.get("virtualIp") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("virtualIp").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("virtualIp");
            }
        }
        VirtualIp virtualIp = new VirtualIp();
        virtualIp.setId(getInt(jn, "id"));
        virtualIp.setAddress(getString(jn, "address"));
        virtualIp.setIpVersion(getIpVersion(jn, "ipVersion"));
        virtualIp.setType(getVipType(jn, "type"));
        return virtualIp;
    }

    public static NetworkItem decodeNetworkItem(ObjectNode jn) throws JsonParseException {
        NetworkItem networkItem = new NetworkItem();
        networkItem.setId(getInt(jn, "id"));
        networkItem.setAddress(getString(jn, "address"));
        networkItem.setIpVersion(getIpVersion(jn, "ipVersion"));
        networkItem.setType(getNetworkItemType(jn, "type"));
        return networkItem;
    }

    public static AccessList decodeAccessList(JsonNode jn) throws JsonParseException {
        AccessList al = new AccessList();
        ArrayNode an;
        if ((jn instanceof ObjectNode) && jn.get("accessList") != null && (jn.get("accessList") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("accessList");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        for (int i = 0; i < an.size(); i++) {
            if (!(an.get(i) instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, an.get(i).toString());
                throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
            }
            ObjectNode networkItemNode = (ObjectNode) an.get(i);
            NetworkItem networkItem = decodeNetworkItem(networkItemNode);
            al.getNetworkItems().add(networkItem);
        }
        return al;
    }

    public static NetworkItemType getNetworkItemType(ObjectNode on, String prop) throws JsonParseException {
        String itemTypeString = getString(on, prop);
        NetworkItemType networkItemType;
        if (itemTypeString == null) {
            return null;
        }
        try {
            networkItemType = NetworkItemType.fromValue(itemTypeString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal NetworkItem type found %s in %s", itemTypeString, on.toString());
            throw new JsonParseException(msg, on.traverse().getCurrentLocation());
        }
        return networkItemType;
    }

    public static Nodes decodeNodes(JsonNode jn) throws JsonParseException {
        Nodes nodes = new Nodes();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("nodes") != null
                && jn.get("nodes").size() > 0) {
            an = (ArrayNode) jn.get("nodes");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode jnode = an.get(i);
            if (!(jnode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jnode.toString());
                throw new JsonParseException(msg, jnode.traverse().getTokenLocation());
            }
            Node node = decodeNode((ObjectNode) jnode);
            nodes.getNodes().add(node);
        }
        return nodes;
    }

    public static Node decodeNode(ObjectNode jnode) throws JsonParseException {
        ObjectNode jn = jnode;
        if (jn.get("node") != null) {
            if (!(jn.get("node") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("node").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("node");
            }
        }
        Node node = new Node();
        node.setId(getInt(jn, "id"));
        node.setAddress(getString(jn, "address"));
        node.setPort(getInt(jn, "port"));
        node.setWeight(getInt(jn, "weight"));

        node.setCondition(getNodeCondition(jn, "condition"));
        node.setStatus(getNodeStatus(jn, "status"));
        node.setType(getNodeType(jn, "type"));
        if (jn.get("metadata") != null) {
            node.setMetadata(decodeMetadata(jn.get("metadata")));
        }
        return node;
    }

    public static Metadata decodeMetadata(JsonNode jn) throws JsonParseException {
        Metadata metadata = new Metadata();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("metadata") != null
                && (jn.get("metadata") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("metadata");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode node = an.get(i);
            if (!(node instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", node.toString());
                throw new JsonParseException(msg, node.traverse().getTokenLocation());
            }
            Meta meta = decodeMeta((ObjectNode) node);
            metadata.getMetas().add(meta);
            // Links is ignored.
        }
        return metadata;
    }

    public static Meta decodeMeta(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("meta") != null) {
            if (!(jn.get("meta") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("meta").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("meta");
            }
        }
        Meta meta = new Meta();
        meta.setId(getInt(jn, "id"));
        meta.setKey(getString(jn, "key"));
        meta.setValue(getString(jn, "value"));
        return meta;
    }

    public static NodeServiceEvents decodeNodeServiceEvents(JsonNode jn) throws JsonParseException {
        ObjectNode objectNode = (ObjectNode) jn;
        NodeServiceEvents events = new NodeServiceEvents();
        events.setLoadbalancerId(getInt(objectNode, "loadbalancerId"));
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("nodeServiceEvents") != null
                && (jn.get("nodeServiceEvents") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("nodeServiceEvents");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode node = an.get(i);
            if (!(node instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, node.toString());
                throw new JsonParseException(msg, node.traverse().getTokenLocation());
            }
            NodeServiceEvent event = decodeNodeServiceEvent((ObjectNode) node);
            events.getNodeServiceEvents().add(event);
        }
        return events;
    }

    public static NodeServiceEvent decodeNodeServiceEvent(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("nodeServiceEvent") != null) {
            if (!(jn.get("nodeServiceEvent") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, jn.get("nodeServiceEvent").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("nodeServiceEvent");
            }
        }
        NodeServiceEvent event = new NodeServiceEvent();
        event.setId(getInt(jn, "id"));
        event.setLoadbalancerId(getInt(jn, "loadbalancerId"));
        event.setNodeId(getInt(jn, "nodeId"));
        event.setType(getString(jn, "type"));
        event.setDescription(getString(jn, "description"));
        event.setDetailedMessage(getString(jn, "detailedMessage"));
        event.setCategory(getString(jn, "category"));
        event.setSeverity(getString(jn, "severity"));
        event.setRelativeUri(getString(jn, "relativeUri"));
        event.setAccountId(getInt(jn, "accountId"));
        event.setTitle(getString(jn, "title"));
        event.setAuthor(getString(jn, "author"));
        event.setCreated(getString(jn, "created"));
        return event;
    }

    public static Protocols decodeProtocols(JsonNode jn) throws JsonParseException {
        Protocols protocols = new Protocols();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("protocols") != null
                && (jn.get("protocols") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("protocols");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode node = an.get(i);
            if (!(node instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, node.toString());
                throw new JsonParseException(msg, node.traverse().getTokenLocation());
            }
            Protocol protocol = new Protocol();
            protocol.setName(getString(node, "name"));
            protocol.setPort(getInt(node, "port"));
            protocols.getProtocols().add(protocol);
        }
        return protocols;
    }

    public static Algorithms decodeAlgorithms(JsonNode jn) throws JsonParseException {
        Algorithms algorithms = new Algorithms();
        ArrayNode an;
        int i;
        if ((jn instanceof ObjectNode)
                && jn.get("algorithms") != null
                && (jn.get("algorithms") instanceof ArrayNode)) {
            an = (ArrayNode) jn.get("algorithms");
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format(NOT_OBJ_OR_ARR, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode node = an.get(i);
            if (!(node instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_NODE, node.toString());
                throw new JsonParseException(msg, node.traverse().getTokenLocation());
            }
            Algorithm algorithm = new Algorithm();
            algorithm.setName(getString(node, "name"));
            algorithms.getAlgorithms().add(algorithm);
        }
        return algorithms;
    }

    public static IpVersion getIpVersion(JsonNode jn, String prop) throws JsonParseException {
        String ipVersionString = getString(jn, prop);
        IpVersion ipVersion;
        if (ipVersionString == null) {
            return null;
        }
        try {
            ipVersion = IpVersion.fromValue(ipVersionString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal IPVersion found %s in %s", ipVersionString, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return ipVersion;
    }

    public static VipType getVipType(JsonNode jn, String prop) throws JsonParseException {
        String vipTypeString = getString(jn, prop);
        VipType vipType;
        if (vipTypeString == null) {
            return null;
        }
        try {
            vipType = VipType.fromValue(vipTypeString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal vipType found %s in %s", vipTypeString, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return vipType;
    }

    public static NodeStatus getNodeStatus(JsonNode jn, String prop) throws JsonParseException {
        String nodeStatus = getString(jn, prop);
        NodeStatus status;
        if (nodeStatus == null) {
            return null;
        }
        try {
            status = NodeStatus.fromValue(nodeStatus);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal nodeStatus found %s in %s", nodeStatus, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return status;

    }

    public static NodeCondition getNodeCondition(JsonNode jn, String prop) throws JsonParseException {
        String nodeCondition = getString(jn, prop);
        NodeCondition condition;
        if (nodeCondition == null) {
            return null;
        }
        try {
            condition = NodeCondition.fromValue(nodeCondition);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal nodeCondition found %s in %s", nodeCondition, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return condition;
    }

    public static NodeType getNodeType(JsonNode jn, String prop) throws JsonParseException {
        String nodeType = getString(jn, prop);
        NodeType type;
        if (nodeType == null) {
            return null;
        }
        try {
            type = NodeType.fromValue(nodeType);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal nodeType found %s in %s", nodeType, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return type;
    }

    public static HealthMonitorType getHealthMonitorType(JsonNode jn, String prop) throws JsonParseException {
        String monitorType = getString(jn, prop);
        HealthMonitorType type;
        if (monitorType == null) {
            return null;
        }
        try {
            type = HealthMonitorType.fromValue(monitorType);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal monitorType found %s in %s", monitorType, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return type;
    }

    public static PersistenceType getPersistenceType(JsonNode jn, String prop) throws JsonParseException {
        String persistenceType = getString(jn, prop);
        PersistenceType type;
        if (persistenceType == null) {
            return null;
        }
        try {
            type = PersistenceType.fromValue(persistenceType);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illegal persistenceType found %s in %s", persistenceType, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return type;
    }

    public static Created getCreated(ObjectNode jsonNodeIn) throws JsonParseException {
        Created created = new Created();
        created.setTime(decodeDate(jsonNodeIn, "time"));
        return created;
    }

    public static Updated getUpdated(ObjectNode jsonNodeIn) throws JsonParseException {
        Updated updated = new Updated();
        updated.setTime(decodeDate(jsonNodeIn, "time"));
        return updated;
    }

    public static Integer getInt(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isInt()) {
            return new Integer(jn.get(prop).getValueAsInt());
        }
        return null;
    }

    public static Double getDouble(JsonNode jn, String prop) {
        if (jn.get(prop) != null) {
            return new Double(jn.get(prop).getValueAsDouble());
        }
        return null;
    }

    public static Long getLong(JsonNode jn, String prop) {
        if (jn.get(prop) != null) {
            return new Long(jn.get(prop).getValueAsLong());
        }
        return null;
    }

    public static String getString(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isTextual()) {
            return jn.get(prop).getValueAsText();
        }
        return null;
    }

    public static Boolean getBoolean(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isBoolean()) {
            return jn.get(prop).getBooleanValue();
        }
        return null;
    }
}
