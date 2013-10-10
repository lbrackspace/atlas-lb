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
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;
import org.w3.atom.Link;

public class JsonPublicSerializers {

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

    public void attachDateTime(ObjectNode on, String prop, Calendar cal) throws JsonSerializeException {
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
