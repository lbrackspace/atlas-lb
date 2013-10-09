package org.openstack.atlas.api.helpers.JsonUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.BigIntegerNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.w3.atom.Link;

public class JsonPublicSerializers {

    public static ObjectNode encodeVirtualIp(VirtualIp virtualIp, boolean addRoot) {
        ObjectNode jsonVipNode;
        ObjectNode topNode;

        if (addRoot) {
            jsonVipNode = JsonParserUtils.newObjectNode();
            topNode = jsonVipNode;
        } else {
            topNode = JsonParserUtils.newObjectNode();
            jsonVipNode = topNode.putObject("virtualIp");
        }

        if (virtualIp.getId() != null) {
            jsonVipNode.put("id", virtualIp.getId().intValue());
        }
        if (virtualIp.getAddress() != null) {
            jsonVipNode.put("address", virtualIp.getAddress());
        }
        if (virtualIp.getIpVersion() != null) {
            jsonVipNode.put("ipVersion", virtualIp.getIpVersion().value());
        }
        if (virtualIp.getType() != null) {
            jsonVipNode.put("type", virtualIp.getType().value());
        }
        return topNode;
    }

    

    public static ObjectNode encodeAtomLink(Link atomLink, boolean addRoot) {
        ObjectNode topNode;
        ObjectNode jsonAtomLink;
        if (addRoot) {
            jsonAtomLink = JsonParserUtils.newObjectNode();
            topNode = jsonAtomLink;
        } else {
            topNode = JsonParserUtils.newObjectNode();
            jsonAtomLink = topNode.putObject("link");
        }

        if (atomLink.getBase() != null) {
            jsonAtomLink.put("base", atomLink.getBase());
        }
        if (atomLink.getContent() != null) {
            jsonAtomLink.put("content", atomLink.getContent());
        }
        if (atomLink.getHref() != null) {
            jsonAtomLink.put("href", atomLink.getHref());
        }
        if (atomLink.getHreflang() != null) {
            jsonAtomLink.put("hreflang", atomLink.getHreflang());
        }
        if (atomLink.getLang() != null) {
            jsonAtomLink.put("lang", atomLink.getLang());
        }
        if (atomLink.getLength() != null) {
            jsonAtomLink.put("length", new BigIntegerNode(atomLink.getLength()));
        }
        if (atomLink.getOtherAttributes() != null && !atomLink.getOtherAttributes().isEmpty()) {
            ObjectNode oa = jsonAtomLink.putObject("otherAttributes");
            for (Entry<QName, String> ent : atomLink.getOtherAttributes().entrySet()) {
                String nsUri = ent.getKey().getNamespaceURI();
                String localPart = ent.getKey().getLocalPart();
                String prefix = ent.getKey().getPrefix();
                oa.put("{" + nsUri + "}" + localPart, ent.getValue());
            }
        }
        if (atomLink.getRel() != null) {
            jsonAtomLink.put("rel", atomLink.getRel());
        }
        if (atomLink.getTitle() != null) {
            jsonAtomLink.put("title", atomLink.getTitle());
        }
        if (atomLink.getType() != null) {
            jsonAtomLink.put("type", atomLink.getType());
        }
        return topNode;
    }
}
