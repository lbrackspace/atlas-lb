package org.openstack.atlas.api.helpers.JsonUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BigIntegerNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
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
}
