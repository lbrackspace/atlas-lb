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
import org.w3.atom.Link;

public class JsonPublicSerializers {

    public static void attachLoadBalancers(ObjectNode objectNode, LoadBalancers loadBalancers, boolean includeLinks) throws JsonSerializeException {
        List<LoadBalancer> loadBalancerList = loadBalancers.getLoadBalancers();
        List<Link> atomLinks = loadBalancers.getLinks();
        if (loadBalancerList != null && loadBalancerList.size() > 0) {
            ArrayNode an = objectNode.putArray("loadBalancers");
            for (LoadBalancer lb : loadBalancerList) {
                ObjectNode lbNode = an.addObject();
                attachLoadBalancer(lbNode, lb);
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

    public static void attachLoadBalancer(ObjectNode objectNode, LoadBalancer loadBalancer) throws JsonSerializeException {
        if (loadBalancer.getNodes() != null) {
            attachNodes(objectNode, loadBalancer.getNodes(), false);
        }
        if (loadBalancer.getAccessList() != null) {
            attachAccessList(objectNode, loadBalancer.getAccessList(), false);
        }
        if (loadBalancer.getVirtualIps() != null) {
            attachVirtualIps(objectNode, loadBalancer.getVirtualIps(), false);
        }
        if (loadBalancer.getMetadata() != null) {
            attachMetadata(objectNode, loadBalancer.getMetadata(), false);
        }
        if (loadBalancer.getLoadBalancerUsage() != null) {
            objectNode.putObject("loadBalancerUsage");
            attachLoadBalancerUsage((ObjectNode) objectNode.get("loadBalancerUsage"), loadBalancer.getLoadBalancerUsage(), false);
        }
        if (loadBalancer.getSessionPersistence() != null) {
            objectNode.putObject("sessionPersistence");
            attachSessionPersistence((ObjectNode) objectNode.get("sessionPersistence"), loadBalancer.getSessionPersistence());
        }
        if (loadBalancer.getHealthMonitor() != null) {
            objectNode.putObject("healthMonitor");
            attachHealthMonitor((ObjectNode) objectNode.get("healthMonitor"), loadBalancer.getHealthMonitor());
        }
        if (loadBalancer.getConnectionThrottle() != null) {
            objectNode.putObject("connectionThrottle");
            attachConnectionThrottle((ObjectNode) objectNode.get("connectionThrottle"), loadBalancer.getConnectionThrottle());
        }
        if (loadBalancer.getCluster() != null) {
            objectNode.putObject("cluster");
            attachCluster((ObjectNode) objectNode.get("cluster"), loadBalancer.getCluster());
        }
        if (loadBalancer.getCreated() != null) {
            objectNode.putObject("created");
            attachCreated((ObjectNode) objectNode.get("created"), loadBalancer.getCreated());
        }
        if (loadBalancer.getUpdated() != null) {
            objectNode.putObject("updated");
            attachUpdated((ObjectNode) objectNode.get("updated"), loadBalancer.getUpdated());
        }
        if (loadBalancer.getConnectionLogging() != null) {
            objectNode.putObject("connectionLogging");
            attachConnectionLogging((ObjectNode) objectNode.get("connectionLogging"), loadBalancer.getConnectionLogging());
        }
        if (loadBalancer.getContentCaching() != null) {
            objectNode.putObject("contentCaching");
            attachContentCaching((ObjectNode) objectNode.get("contentCaching"), loadBalancer.getContentCaching());
        }
        if (loadBalancer.getSslTermination() != null) {
            objectNode.putObject("sslTermination");
            attachSslTermination((ObjectNode) objectNode.get("sslTermination"), loadBalancer.getSslTermination());
        }
        if (loadBalancer.getSourceAddresses() != null) {
            objectNode.putObject("sourceAddresses");
            attachSourceAddresses((ObjectNode) objectNode.get("sourceAddresses"), loadBalancer.getSourceAddresses());
        }
        if (loadBalancer.getAlgorithm() != null) {
            objectNode.put("algorithm", loadBalancer.getAlgorithm());
        }
        if (loadBalancer.getId() != null) {
            objectNode.put("id", loadBalancer.getId());
        }
        if (loadBalancer.getName() != null) {
            objectNode.put("name", loadBalancer.getName());
        }
        if (loadBalancer.getNodeCount() != null) {
            objectNode.put("nodeCount", loadBalancer.getNodeCount());
        }
        if (loadBalancer.getPort() != null) {
            objectNode.put("port", loadBalancer.getPort());
        }
        if (loadBalancer.getProtocol() != null) {
            objectNode.put("protocol", loadBalancer.getProtocol());
        }
        if (loadBalancer.getStatus() != null) {
            objectNode.put("status", loadBalancer.getStatus());
        }
        if (loadBalancer.getTimeout() != null) {
            objectNode.put("timeout", loadBalancer.getTimeout());
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
