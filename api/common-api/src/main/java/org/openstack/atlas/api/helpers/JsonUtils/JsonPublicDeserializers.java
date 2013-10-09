package org.openstack.atlas.api.helpers.JsonUtils;

import java.io.IOException;
import java.util.List;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.openstack.atlas.api.helpers.JsonUtils.JsonParserUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
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

public class JsonPublicDeserializers {

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
            String msg = String.format("Error was expecting an ObjectNode({}) or an ArrayNode([]) but found %s", jn.toString());
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

    public static VirtualIp decodeVirtualIp(ObjectNode jsonNodeIn) throws JsonParseException {
        ObjectNode jn = jsonNodeIn;
        if (jn.get("virtualIp") != null) {
            if (!(jn.get("virtualIp") instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but instead found %s", jn.get("virtualIp").toString());
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
            String msg = String.format("Error was expecting an ObjectNode({}) or an ArrayNode([]) but found %s", jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode node = an.get(i);
            if (!(node instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but found %s instead", an.get(i).toString());
                throw new JsonParseException(msg, an.get(i).traverse().getTokenLocation());
            }
            Meta meta = decodeMeta((ObjectNode) node);
            metadata.getMetas().add(meta);
            // Links is ignored.
        }
        return metadata;
    }

    public static Meta decodeMeta(ObjectNode jsonNodeIn) throws JsonParseException {
        Meta meta = new Meta();
        ObjectNode jn = jsonNodeIn;
        if (jn.get("meta") != null) {
            if (!(jn.get("meta") instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but instead found %s", jn.get("meta").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("meta");
            }
        }
        meta.setId(getInt(jn, "id"));
        meta.setKey(getString(jn, "key"));
        meta.setValue(getString(jn, "value"));
        return meta;
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

    public static Integer getInt(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isInt()) {
            return new Integer(jn.get(prop).getValueAsInt());
        }
        return null;
    }

    public static String getString(JsonNode jn, String prop) {
        if (jn.get(prop) != null && jn.get(prop).isTextual()) {
            return jn.get(prop).getValueAsText();
        }
        return null;
    }
}
