package org.openstack.atlas.api.helpers.JsonUtils;

import java.util.List;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.openstack.atlas.api.helpers.JsonUtils.JsonParserUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.IntNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.TextNode;
import org.codehaus.jackson.node.DoubleNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.LongNode;
import org.codehaus.jackson.node.BigIntegerNode;
import org.codehaus.jackson.node.BinaryNode;

import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;

public class JsonPublicPojoParser {

    public static VirtualIps decodeVirtualIps(JsonNode jn) throws JsonParseException {
        VirtualIps virtualIps = new VirtualIps();
        ArrayNode an;
        int i;
        if (jn instanceof ObjectNode) {
            an = (ArrayNode) jn.get("virtualIps"); // Strip the root node if its there
        } else if (jn instanceof ArrayNode) {
            an = (ArrayNode) jn;
        } else {
            String msg = String.format("Error was expecting an objectNode({}) or an ArrayNode([]) but found %s", jn.toString());
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
        }
        return virtualIps;
    }

    public static VirtualIp decodeVirtualIp(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIp") != null) {
            if (!(jn.get("virtualIp") instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but instead found %s", jn.get("virtualIp").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            }
        }
        VirtualIp virtualIp = new VirtualIp();
        if (jn.get("id") != null) {
            virtualIp.setId(jn.get("id").getIntValue());
        }
        if (jn.get("address") != null) {
            virtualIp.setAddress(jn.get("id").getTextValue());
        }

        if (jn.get("ipVersion") != null) {
            virtualIp.setType(VipType.fromValue(jn.get("ipVersion").getTextValue()));
        }

        if (jn.get("type") != null) {
            virtualIp.setType(VipType.fromValue(jn.get("type").getTextValue()));
        }
        return virtualIp;
    }
}
