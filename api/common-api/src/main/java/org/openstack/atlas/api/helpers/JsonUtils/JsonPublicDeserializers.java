package org.openstack.atlas.api.helpers.JsonUtils;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.openstack.atlas.api.helpers.JsonUtils.JsonParserUtils;
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
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.util.converters.DateTimeConverters;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;

public class JsonPublicDeserializers {

    private static final String NOT_OBJ_OR_ARRAY = "Error was expecting an ObjectNode({}) but instead found %s";

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
                String msg = String.format(NOT_OBJ_OR_ARRAY, jn.get("virtualIp").toString());
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
            String msg = String.format(NOT_OBJ_OR_ARRAY, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        for (int i = 0; i < an.size(); i++) {
            if (!(an.get(i) instanceof ObjectNode)) {
                String msg = String.format("Error was expecting Object({}) but instead found %s", an.get(i).toString());
                throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
            }
            ObjectNode networkItemNode = (ObjectNode) an.get(i);
            NetworkItem networkItem = decodeNetworkItem(networkItemNode);
            al.getNetworkItems().add(networkItem);
        }
        return al;
    }

    public static VipType getVipType(ObjectNode jn, String prop) throws JsonParseException {
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

    public static NetworkItemType getNetworkItemType(ObjectNode on, String prop) throws JsonParseException {
        String itemTypeString = getString(on, prop);
        NetworkItemType networkItemType;
        if (itemTypeString == null) {
            return null;
        }
        try {
            networkItemType = NetworkItemType.fromValue(itemTypeString);
        } catch (IllegalStateException ex) {
            String msg = String.format("Illega NetworkItem type found %s in %s", itemTypeString, on.toString());
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
            String msg = String.format(NOT_OBJ_OR_ARRAY, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getTokenLocation());
        }
        for (i = 0; i < an.size(); i++) {
            JsonNode jnode = an.get(i);
            if (!(jnode instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_OR_ARRAY, jnode.toString());
                throw new JsonParseException(msg, jnode.traverse().getTokenLocation());
            }
            Node node = decodeNode((ObjectNode) jnode);
            nodes.getNodes().add(node);
        }
        return nodes;
    }

    public static Node decodeNode(ObjectNode jnode) throws JsonParseException {
        Node node = new Node();
        ObjectNode jn = jnode;
        if (jn.get("node") != null) {
            if (!(jn.get("node") instanceof ObjectNode)) {
                String msg = String.format("Error was expecting an ObjectNode({}) but instead found %s", jn.get("node").toString());
                throw new JsonParseException(msg, jn.traverse().getTokenLocation());
            } else {
                jn = (ObjectNode) jn.get("node");
            }
        }
        node.setId(getInt(jn, "id"));
        node.setAddress(getString(jn, "address"));
        node.setPort(getInt(jn, "port"));
        node.setWeight(getInt(jn, "port"));

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
            String msg = String.format(NOT_OBJ_OR_ARRAY, jn.toString());
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
        Meta meta = new Meta();
        ObjectNode jn = jsonNodeIn;
        if (jn.get("meta") != null) {
            if (!(jn.get("meta") instanceof ObjectNode)) {
                String msg = String.format(NOT_OBJ_OR_ARRAY, jn.get("meta").toString());
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
            String msg = String.format("Illegal vipType found %s in %s", nodeStatus, jn.toString());
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
            String msg = String.format("Illegal vipType found %s in %s", nodeCondition, jn.toString());
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
            String msg = String.format("Illegal vipType found %s in %s", nodeType, jn.toString());
            throw new JsonParseException(msg, jn.traverse().getCurrentLocation());
        }
        return type;
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
