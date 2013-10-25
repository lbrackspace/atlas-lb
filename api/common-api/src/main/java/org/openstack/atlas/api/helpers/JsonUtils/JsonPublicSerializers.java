package org.openstack.atlas.api.helpers.JsonUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BigIntegerNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.openstack.atlas.api.helpers.JsonSerializeException;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;
import org.openstack.docs.common.api.v1.AbsoluteLimit;
import org.openstack.docs.common.api.v1.AbsoluteLimitList;
import org.w3.atom.Link;

public class JsonPublicSerializers {

    public static void attachLoadBalancers(ObjectNode objectNode, LoadBalancers loadBalancers, boolean includeLinks) throws JsonSerializeException {
        List<LoadBalancer> loadBalancerList = loadBalancers.getLoadBalancers();
        List<Link> atomLinks = loadBalancers.getLinks();
        if (loadBalancerList != null && loadBalancerList.size() > 0) {
            ArrayNode an = objectNode.putArray("loadBalancers");
            for (LoadBalancer lb : loadBalancerList) {
                ObjectNode lbNode = an.addObject();
                attachLoadBalancer(lbNode, lb, false);
            }
        }
        if (includeLinks && atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }



    public static void attachLoadBalancer(ObjectNode objectNode, LoadBalancer loadBalancer, boolean includeName) throws JsonSerializeException {
        ObjectNode node;
        if (includeName) {
            objectNode.putObject("loadBalancer");
            node = (ObjectNode) objectNode.get("loadBalancer");
        } else {
            node = objectNode;
        }
        if (loadBalancer.getNodes() != null) {
            attachNodes(node, loadBalancer.getNodes(), false);
        }
        if (loadBalancer.getAccessList() != null) {
            attachAccessList(node, loadBalancer.getAccessList(), false);
        }
        if (loadBalancer.getVirtualIps() != null) {
            attachVirtualIps(node, loadBalancer.getVirtualIps(), false);
        }
        if (loadBalancer.getMetadata() != null) {
            attachMetadata(node, loadBalancer.getMetadata(), false);
        }
        if (loadBalancer.getLoadBalancerUsage() != null) {
            node.putObject("loadBalancerUsage");
            attachLoadBalancerUsage((ObjectNode) node.get("loadBalancerUsage"), loadBalancer.getLoadBalancerUsage(), false);
        }
        if (loadBalancer.getSessionPersistence() != null) {
            node.putObject("sessionPersistence");
            attachSessionPersistence((ObjectNode) node.get("sessionPersistence"), loadBalancer.getSessionPersistence());
        }
        if (loadBalancer.getHealthMonitor() != null) {
            node.putObject("healthMonitor");
            attachHealthMonitor((ObjectNode) node.get("healthMonitor"), loadBalancer.getHealthMonitor());
        }
        if (loadBalancer.getConnectionThrottle() != null) {
            node.putObject("connectionThrottle");
            attachConnectionThrottle((ObjectNode) node.get("connectionThrottle"), loadBalancer.getConnectionThrottle());
        }
        if (loadBalancer.getCluster() != null) {
            node.putObject("cluster");
            attachCluster((ObjectNode) node.get("cluster"), loadBalancer.getCluster());
        }
        if (loadBalancer.getCreated() != null) {
            node.putObject("created");
            attachCreated((ObjectNode) node.get("created"), loadBalancer.getCreated());
        }
        if (loadBalancer.getUpdated() != null) {
            node.putObject("updated");
            attachUpdated((ObjectNode) node.get("updated"), loadBalancer.getUpdated());
        }
        if (loadBalancer.getConnectionLogging() != null) {
            node.putObject("connectionLogging");
            attachConnectionLogging((ObjectNode) node.get("connectionLogging"), loadBalancer.getConnectionLogging());
        }
        if (loadBalancer.getContentCaching() != null) {
            node.putObject("contentCaching");
            attachContentCaching((ObjectNode) node.get("contentCaching"), loadBalancer.getContentCaching());
        }
        if (loadBalancer.getSslTermination() != null) {
            node.putObject("sslTermination");
            attachSslTermination((ObjectNode) node.get("sslTermination"), loadBalancer.getSslTermination());
        }
        if (loadBalancer.getSourceAddresses() != null) {
            node.putObject("sourceAddresses");
            attachSourceAddresses((ObjectNode) node.get("sourceAddresses"), loadBalancer.getSourceAddresses());
        }
        if (loadBalancer.getAlgorithm() != null) {
            node.put("algorithm", loadBalancer.getAlgorithm());
        }
        if (loadBalancer.getId() != null) {
            node.put("id", loadBalancer.getId());
        }
        if (loadBalancer.getName() != null) {
            node.put("name", loadBalancer.getName());
        }
        if (loadBalancer.getNodeCount() != null) {
            node.put("nodeCount", loadBalancer.getNodeCount());
        }
        if (loadBalancer.getPort() != null) {
            node.put("port", loadBalancer.getPort());
        }
        if (loadBalancer.getProtocol() != null) {
            node.put("protocol", loadBalancer.getProtocol());
        }
        if (loadBalancer.getStatus() != null) {
            node.put("status", loadBalancer.getStatus());
        }
        if (loadBalancer.getTimeout() != null) {
            node.put("timeout", loadBalancer.getTimeout());
        }
    }

    public static void attachSslTermination(ObjectNode objectNode, SslTermination termination) {
        if (termination.getCertificate() != null) {
            objectNode.put("certificate", termination.getCertificate());
        }
        if (termination.getId() != null) {
            objectNode.put("id", termination.getId());
        }
        if (termination.getIntermediateCertificate() != null) {
            objectNode.put("intermediateCertificate", termination.getIntermediateCertificate());
        }
        if (termination.getPrivatekey() != null) {
            objectNode.put("privateKey", termination.getPrivatekey());
        }
        if (termination.getSecurePort() != null) {
            objectNode.put("securePort", termination.getSecurePort());
        }
    }

    public static void attachSourceAddresses(ObjectNode objectNode, SourceAddresses addresses) {
        if (addresses.getIpv4Public() != null) {
            objectNode.put("ipv4PUblic", addresses.getIpv4Public());
        }
        if (addresses.getIpv6Public() != null) {
            objectNode.put("ipv6Public", addresses.getIpv6Public());
        }
        if (addresses.getIpv4Servicenet() != null) {
            objectNode.put("ipv4Servicenet", addresses.getIpv4Servicenet());
        }
        if (addresses.getIpv6Servicenet() != null) {
            objectNode.put("ipv6Servicenet", addresses.getIpv6Servicenet());
        }
    }

    public static void attachAccountBillings(ObjectNode objectNode, List<AccountBilling> accountBillings) throws JsonSerializeException {
        ArrayNode an = objectNode.putArray("accountBillings");
        for (AccountBilling billing : accountBillings) {
            ObjectNode billingNode = an.addObject();
            attachAccountBilling(billingNode, billing);
        }
    }

    public static void attachAccountBilling(ObjectNode objectNode, AccountBilling billing) throws JsonSerializeException {
        if (billing.getAccountId() != null) {
            objectNode.put("accountId", billing.getAccountId());
        }
        if (billing.getAccountUsage() != null) {
            objectNode.putObject("accountUsage");
            attachAccountUsage((ObjectNode) objectNode.get("accountUsage"), billing.getAccountUsage());
        }
        if (billing.getLoadBalancerUsages() != null) {
            attachLoadBalancerUsages(objectNode, billing.getLoadBalancerUsages());
        }
        List<Link> atomLinks = billing.getLinks();
        if (atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachAccountUsage(ObjectNode objectNode, AccountUsage usage) throws JsonSerializeException{
        if (usage.getAccountUsageRecords() != null && usage.getAccountUsageRecords().size()> 0) {
            attachAccountUsageRecords(objectNode, usage.getAccountUsageRecords());
        }
        List<Link> atomLinks = usage.getLinks();
        if (atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachAccountUsageRecords(ObjectNode objectNode, List<AccountUsageRecord> records) throws JsonSerializeException {
        List<AccountUsageRecord> recordList = records;
        if (recordList != null && recordList.size() > 0) {
            ArrayNode an = objectNode.putArray("accountUsageRecords");
            for (AccountUsageRecord record : recordList) {
                ObjectNode recordNode = an.addObject();
                attachAccountUsageRecord(recordNode, record);
            }
        }
    }

    public static void attachAccountUsageRecord(ObjectNode objectNode, AccountUsageRecord record) throws JsonSerializeException {
        if (record.getNumLoadBalancers() != null) {
            objectNode.put("numLoadBalancers", record.getNumLoadBalancers());
        }
        if (record.getNumPublicVips() != null) {
            objectNode.put("numPublicVips", record.getNumPublicVips());
        }
        if (record.getNumServicenetVips() != null) {
            objectNode.put("numServicenetVips", record.getNumServicenetVips());
        }
        if (record.getStartTime() != null) {
            attachDateTime(objectNode, "startTime", record.getStartTime());
        }
    }

    public static void attachLoadBalancerUsages(ObjectNode objectNode, List<LoadBalancerUsage> usages) throws JsonSerializeException {
        List<LoadBalancerUsage> usageList = usages;
        if (usageList != null && usageList.size() > 0) {
            ArrayNode an = objectNode.putArray("LoadBalancerUsages");
            for (LoadBalancerUsage usage : usageList) {
                ObjectNode usageNode = an.addObject();
                attachLoadBalancerUsage(usageNode, usage, false);
            }
        }
    }

    public static void attachLoadBalancerUsage(ObjectNode objectNode, LoadBalancerUsage usage, boolean includeLinks) throws JsonSerializeException {
        if (usage.getLoadBalancerUsageRecords() != null && usage.getLoadBalancerUsageRecords().size() > 0) {
            attachLoadBalancerUsageRecords(objectNode, usage.getLoadBalancerUsageRecords());
        }
        if (usage.getLoadBalancerId() != null) {
            objectNode.put("loadBalancerId", usage.getLoadBalancerId());
        }
        if (usage.getLoadBalancerName() != null) {
            objectNode.put("loadBalancerName", usage.getLoadBalancerName());
        }
        if (includeLinks && usage.getLinks() != null && usage.getLinks().size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            List<Link> atomLinks = usage.getLinks();
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachNodeServiceEvents(ObjectNode objectNode, NodeServiceEvents events, boolean includeLinks) {
        if (events.getLoadbalancerId() != null) {
            objectNode.put("loadbalancerId", events.getLoadbalancerId());
        }
        if (events.getNodeServiceEvents() != null && events.getNodeServiceEvents().size() > 0) {
            ArrayNode an = objectNode.putArray("nodeServiceEvents");
            List<NodeServiceEvent> eventList = events.getNodeServiceEvents();
            for (NodeServiceEvent event : eventList) {
                ObjectNode eventNode = an.addObject();
                attachNodeServiceEvent(eventNode, event);
            }
        }
        if (includeLinks && events.getLinks() != null && events.getLinks().size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            List<Link> atomLinks = events.getLinks();
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachNodeServiceEvent(ObjectNode objectNode, NodeServiceEvent event) {
        if (event.getLoadbalancerId() != null) {
            objectNode.put("loadbalancerId", event.getLoadbalancerId());
        }
        if (event.getDetailedMessage() != null) {
            objectNode.put("detailedMessage", event.getDetailedMessage());
        }
        if (event.getNodeId() != null) {
            objectNode.put("nodeId", event.getNodeId());
        }
        if (event.getAccountId() != null) {
            objectNode.put("accountId", event.getAccountId());
        }
        if (event.getAuthor() != null) {
            objectNode.put("author", event.getAuthor());
        }
        if (event.getCategory() != null) {
            objectNode.put("category", event.getCategory());
        }
        if (event.getCreated() != null) {
            objectNode.put("created", event.getCreated());
        }
        if (event.getDescription() != null) {
            objectNode.put("description", event.getDescription());
        }
        if (event.getId() != null) {
            objectNode.put("id", event.getId());
        }
        if (event.getRelativeUri() != null) {
            objectNode.put("relativeUri", event.getRelativeUri());
        }
        if (event.getSeverity() != null) {
            objectNode.put("severity", event.getSeverity());
        }
        if (event.getTitle() != null) {
            objectNode.put("title", event.getTitle());
        }
        if (event.getType() != null) {
            objectNode.put("type", event.getType());
        }
    }

    public static void attachLoadBalancerUsageRecords(ObjectNode objectNode, List<LoadBalancerUsageRecord> recordList) throws JsonSerializeException {
        List<LoadBalancerUsageRecord> records = recordList;
        if (records != null && records.size() > 0) {
            ArrayNode an = objectNode.putArray("loadBalancerUsageRecords");
            for (LoadBalancerUsageRecord record : records) {
                ObjectNode recordNode = an.addObject();
                attachLoadBalancerUsageRecord(recordNode, record);
            }
        }
    }

    public static void attachLoadBalancerUsageRecord(ObjectNode objectNode, LoadBalancerUsageRecord record) throws JsonSerializeException {
        if (record.getAverageNumConnections() != null) {
            objectNode.put("averageNumConnections", record.getAverageNumConnections());
        }
        if (record.getAverageNumConnectionsSsl() != null) {
            objectNode.put("averageNumConnectionsSsl", record.getAverageNumConnectionsSsl());
        }
        if (record.getIncomingTransfer() != null) {
            objectNode.put("incomingTransfer", record.getIncomingTransfer());
        }
        if (record.getIncomingTransferSsl() != null) {
            objectNode.put("incomingTransferSsl", record.getIncomingTransferSsl());
        }
        if (record.getOutgoingTransfer() != null) {
            objectNode.put("outgoingTransfer", record.getOutgoingTransfer());
        }
        if (record.getOutgoingTransferSsl() != null) {
            objectNode.put("outgoingTransferSsl", record.getOutgoingTransferSsl());
        }
        if (record.getNeedsPushed() != null) {
            objectNode.put("needsPush", record.getNeedsPushed());
        }
        if (record.getNumPolls() != null) {
            objectNode.put("numPolls", record.getNumPolls());
        }
        if (record.getNumVips() != null) {
            objectNode.put("numVips", record.getNumVips());
        }
        if (record.getEntryVersion() != null) {
            objectNode.put("entryVersion", record.getEntryVersion());
        }
        if (record.getEventType() != null) {
            objectNode.put("eventType", record.getEventType());
        }
        if (record.getId() != null) {
            objectNode.put("id", record.getId());
        }
        if (record.getSslMode() != null) {
            objectNode.put("sslMode", record.getSslMode());
        }
        if (record.getUuid() != null) {
            objectNode.put("uuid", record.getUuid());
        }
        if (record.getVipType() != null) {
            objectNode.put("vipType", record.getVipType().value());
        }
        if (record.getStartTime() != null) {
            attachDateTime(objectNode, "startTime", record.getStartTime());
        }
        if (record.getEndTime() != null) {
            attachDateTime(objectNode, "endTime", record.getEndTime());
        }
    }

    public static void attachHealthMonitor(ObjectNode objectNode, HealthMonitor monitor) {
        if (monitor.getId() != null) {
            objectNode.put("id", monitor.getId());
        }
        if (monitor.getAttemptsBeforeDeactivation() != null) {
            objectNode.put("attemptsBeforeDeactivation", monitor.getAttemptsBeforeDeactivation());
        }
        if (monitor.getBodyRegex() != null) {
            objectNode.put("bodyRegex", monitor.getBodyRegex());
        }
        if (monitor.getDelay() != null) {
            objectNode.put("delay", monitor.getDelay());
        }
        if (monitor.getHostHeader() != null) {
            objectNode.put("hostHeader", monitor.getHostHeader());
        }
        if (monitor.getPath() != null) {
            objectNode.put("path", monitor.getPath());
        }
        if (monitor.getStatusRegex() != null) {
            objectNode.put("statusRegex", monitor.getStatusRegex());
        }
        if (monitor.getTimeout() != null) {
            objectNode.put("timeout", monitor.getTimeout());
        }
        if (monitor.getType() != null) {
            objectNode.put("type", monitor.getType().value());
        }
    }

    public static void attachSessionPersistence(ObjectNode objectNode, SessionPersistence persistence) {
        if (persistence.getPersistenceType() != null) {
            objectNode.put("persistenceType", persistence.getPersistenceType().value());
        }
    }

    public static void attachConnectionThrottle(ObjectNode objectNode, ConnectionThrottle throttle) {
        if (throttle.getMaxConnectionRate() != null) {
            objectNode.put("maxConnectionRate", throttle.getMaxConnectionRate());
        }
        if (throttle.getMaxConnections() != null) {
            objectNode.put("maxConnections", throttle.getMaxConnections());
        }
        if (throttle.getMinConnections() != null) {
            objectNode.put("minConnections", throttle.getMinConnections());
        }
        if (throttle.getRateInterval() != null) {
            objectNode.put("rateInterval", throttle.getRateInterval());
        }
    }

    public static void attachConnectionLogging(ObjectNode objectNode, ConnectionLogging logging) {
        if (logging.isEnabled()) {
            objectNode.put("enabled", logging.isEnabled());
        }
    }

    public static void attachContentCaching(ObjectNode objectNode, ContentCaching caching) {
        if (caching.isEnabled()) {
            objectNode.put("enabled", caching.isEnabled());
        }
    }

    public static void attachCluster(ObjectNode objectNode, Cluster cluster) {
        if (cluster.getName() != null) {
            objectNode.put("name", cluster.getName());
        }
    }

    public static void attachCreated(ObjectNode objectNode, Created created) throws JsonSerializeException {
        if (created.getTime() != null) {
            attachDateTime(objectNode, "time", created.getTime());
        }
    }

    public static void attachUpdated(ObjectNode objectNode, Updated updated) throws JsonSerializeException {
        if (updated.getTime() != null) {
            attachDateTime(objectNode, "time", updated.getTime());
        }
    }

    public static void attachVirtualIps(ObjectNode objectNode, VirtualIps virtualIps, boolean includeLinks) {
        List<VirtualIp> virtualIpList = virtualIps.getVirtualIps();
        List<Link> atomLinks = virtualIps.getLinks();

        if (virtualIpList != null && virtualIpList.size() > 0) {
            ArrayNode an = objectNode.putArray("virtualIps");
            for (VirtualIp virtualIp : virtualIpList) {
                ObjectNode vipNode = an.addObject();
                attachVirtualIp(vipNode, virtualIp);
            }
        }
        if (includeLinks && atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachVirtualIp(ObjectNode objectNode, VirtualIp virtualIp) {

        if (virtualIp.getId() != null) {
            objectNode.put("id", virtualIp.getId().intValue());
        }
        if (virtualIp.getAddress() != null) {
            objectNode.put("address", virtualIp.getAddress());
        }
        if (virtualIp.getIpVersion() != null) {
            objectNode.put("ipVersion", virtualIp.getIpVersion().value());
        }
        if (virtualIp.getType() != null) {
            objectNode.put("type", virtualIp.getType().value());
        }
    }

    public static void attachNodes(ObjectNode objectNode, Nodes nodes, boolean includeLinks) {
        List<Node> nodeList = nodes.getNodes();
        List<Link> atomLinks = nodes.getLinks();

        if (nodeList != null && nodeList.size() > 0) {
            ArrayNode an = objectNode.putArray("nodes");
            for (Node node : nodeList) {
                ObjectNode nodeNode = an.addObject();
                attachNode(nodeNode, node);
            }
        }
        if (includeLinks && atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachAccessList(ObjectNode objectNode, AccessList accessList, boolean includeLinks) {
        List<NetworkItem> networkItemsList = accessList.getNetworkItems();
        List<Link> atomLinks = accessList.getLinks();
        if (networkItemsList != null && networkItemsList.size() > 0) {
            ArrayNode an = objectNode.putArray("accessList");
            for (NetworkItem networkItem : networkItemsList) {
                ObjectNode networkItemNode = an.addObject();
                attachNetworkItem(networkItemNode, networkItem);
            }
        }
        if (includeLinks && accessList.getLinks() != null && accessList.getLinks().size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }

    }

    public static void attachNode(ObjectNode objectNode, Node node) {
        if (node.getId() != null) {
            objectNode.put("id", node.getId().intValue());
        }
        if (node.getAddress() != null) {
            objectNode.put("address", node.getAddress());
        }
        if (node.getCondition() != null) {
            objectNode.put("condition", node.getCondition().value());
        }
        if (node.getMetadata() != null) {
            attachMetadata(objectNode, node.getMetadata(), false);
        }
        if (node.getPort() != null) {
            objectNode.put("port", node.getPort().intValue());
        }
        if (node.getStatus() != null) {
            objectNode.put("status", node.getStatus().value());
        }
        if (node.getType() != null) {
            objectNode.put("type", node.getType().value());
        }
        if (node.getWeight() != null) {
            objectNode.put("weight", node.getWeight().intValue());
        }
    }

    public static void attachMetadata(ObjectNode objectNode, Metadata metadata, boolean includeLinks) {
        List<Meta> metaList = metadata.getMetas();
        List<Link> atomLinks = metadata.getLinks();

        if (metaList != null && metaList.size() > 0) {
            ArrayNode an = objectNode.putArray("metadata");
            for (Meta meta : metaList) {
                ObjectNode metaNode = an.addObject();
                attachMeta(metaNode, meta);
            }
        }
        if (includeLinks && atomLinks != null && atomLinks.size() > 0) {
            ArrayNode an = objectNode.putArray("links");
            for (Link atomLink : atomLinks) {
                ObjectNode atomNode = an.addObject();
                attachAtomLink(atomNode, atomLink);
            }
        }
    }

    public static void attachNetworkItem(ObjectNode objectNode, NetworkItem networkItem) {
        if (networkItem.getId() != null) {
            objectNode.put("id", networkItem.getId().intValue());
        }
        if (networkItem.getAddress() != null) {
            objectNode.put("address", networkItem.getAddress());
        }
        if (networkItem.getIpVersion() != null) {
            objectNode.put("ipVersion", networkItem.getIpVersion().value());
        }
        if (networkItem.getType() != null) {
            objectNode.put("type", networkItem.getType().value());
        }
    }

    public static void attachMeta(ObjectNode objectNode, Meta meta) {
        if (meta.getId() != null) {
            objectNode.put("id", meta.getId().intValue());
        }
        if (meta.getKey() != null) {
            objectNode.put("key", meta.getKey());
        }
        if (meta.getValue() != null) {
            objectNode.put("value", meta.getValue());
        }
    }

    public static void attachAlgorithms(ObjectNode objectNode, Algorithms algorithms) {
        List<Algorithm> algorithmList = algorithms.getAlgorithms();

        if (algorithmList != null && algorithmList.size() > 0) {
            ArrayNode an = objectNode.putArray("algorithms");
            for (Algorithm algorithm : algorithmList) {
                ObjectNode node = an.addObject();
                node.put("name", algorithm.getName());
            }
        }
    }

    public static void attachProtocols(ObjectNode objectNode, Protocols protocols) {
        List<Protocol> protocolList = protocols.getProtocols();

        if (protocolList != null && protocolList.size() > 0) {
            ArrayNode an = objectNode.putArray("algorithms");
            for (Protocol protocol : protocolList) {
                ObjectNode node = an.addObject();
                node.put("name", protocol.getName());
                node.put("port", protocol.getPort());
            }
        }
    }

    public static void attachOperationsuccess(ObjectNode objectNode, Operationsuccess op) {
        if (op.getMessage() != null) {
            objectNode.put("message", op.getMessage());
        }
        if (op.getStatus() != null) {
            objectNode.put("status", op.getStatus());
        }
    }

    public static void attachAllowedDomains(ObjectNode objectNode, AllowedDomains domains) {
        List<AllowedDomain> domainList = domains.getAllowedDomains();
        if (domainList != null && domainList.size() > 0) {
            ArrayNode an = objectNode.putArray("allowedDomain");
            for (AllowedDomain domain : domainList) {
                ObjectNode node = an.addObject();
                attachAllowedDomain(node, domain);
            }
        }
    }

    public static void attachAllowedDomain(ObjectNode objectNode, AllowedDomain domain) {
        if (domain.getName() != null) {
            objectNode.put("name", domain.getName());
        }
    }

    public static void attachErrorPage(ObjectNode objectNode, Errorpage page) {
        ObjectNode node = objectNode.putObject("errorpage");
        if (page.getContent() != null) {
            node.put("content", page.getContent());
        }
    }

    public static void attachAbsoluteLimits(ObjectNode objectNode, Absolute limits) {
        List<Limit> limitList = limits.getLimits();
        if (limitList != null && limitList.size() > 0) {
            ArrayNode an = objectNode.putArray("absolute");
            for (Limit limit : limitList) {
                ObjectNode node = an.addObject();
                attachAbsoluteLimit(node, limit);
            }
        }
    }

    public static void attachAbsoluteLimit(ObjectNode objectNode, Limit limit) {
        if (limit.getName() != null) {
            objectNode.put("name", limit.getName());
        }
        if (limit.getValue() != null) {
            objectNode.put("value", limit.getValue());
        }
    }

    public static void attachAtomLink(ObjectNode objectNode, Link atomLink) {
        if (atomLink.getBase() != null) {
            objectNode.put("base", atomLink.getBase());
        }
        if (atomLink.getContent() != null) {
            objectNode.put("content", atomLink.getContent());
        }
        if (atomLink.getHref() != null) {
            objectNode.put("href", atomLink.getHref());
        }
        if (atomLink.getHreflang() != null) {
            objectNode.put("hreflang", atomLink.getHreflang());
        }
        if (atomLink.getLang() != null) {
            objectNode.put("lang", atomLink.getLang());
        }
        if (atomLink.getLength() != null) {
            objectNode.put("length", new BigIntegerNode(atomLink.getLength()));
        }
        if (atomLink.getOtherAttributes() != null && !atomLink.getOtherAttributes().isEmpty()) {
            ObjectNode oa = objectNode.putObject("otherAttributes");
            for (Entry<QName, String> ent : atomLink.getOtherAttributes().entrySet()) {
                String nsUri = ent.getKey().getNamespaceURI();
                String localPart = ent.getKey().getLocalPart();
                String prefix = ent.getKey().getPrefix();
                oa.put("{" + nsUri + "}" + localPart, ent.getValue());
            }
        }
        if (atomLink.getRel() != null) {
            objectNode.put("rel", atomLink.getRel());
        }
        if (atomLink.getTitle() != null) {
            objectNode.put("title", atomLink.getTitle());
        }
        if (atomLink.getType() != null) {
            objectNode.put("type", atomLink.getType());
        }
    }

    public static void attachDateTime(ObjectNode on, String prop, Calendar cal) throws JsonSerializeException {
        String isoString;
        try {
            isoString = DateTimeConverters.calToiso(cal);
        } catch (ConverterException ex) {
            String msg = String.format("Error converting Calendar %s to iso8601 format", cal.toString());
            throw new JsonSerializeException(msg, ex);
        }
        on.put(prop, isoString);
    }
}
