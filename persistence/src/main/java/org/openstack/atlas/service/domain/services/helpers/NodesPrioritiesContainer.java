package org.openstack.atlas.service.domain.services.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.openstack.atlas.util.converters.StringConverter;

public class NodesPrioritiesContainer {

    private static final int SB_INIT_SIZE = 4096;
    private Set<Node> primary;
    private Set<Node> secondary;
    private Set<Node> unknown;

    public NodesPrioritiesContainer(Collection<Node> nodes) {
        primary = new HashSet<Node>();
        secondary = new HashSet<Node>();
        unknown = new HashSet<Node>();

        for (Node node : nodes) {
            if (node.getType() == NodeType.PRIMARY) {
                primary.add(node);
            } else if (node.getType() == NodeType.SECONDARY) {
                secondary.add(node);
            } else {
                unknown.add(node);
            }
        }
    }

    @Override
    public String toString() {
        List<String> nodeList;
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        sb.append("{primary: [");
        nodeList = new ArrayList<String>();
        for (Node node : primary) {
            try {
                nodeList.add(node.getId().toString());
            } catch (NullPointerException ex) {
                nodeList.add("Null");
            }
        }
        sb.append(StringConverter.commaSeperatedStringList(nodeList));
        sb.append("],\nsecondary: [");
        nodeList = new ArrayList<String>();
        for (Node node : secondary) {
            try {
                nodeList.add(node.getId().toString());
            } catch (NullPointerException ex) {
                nodeList.add("Null");
            }
        }
        sb.append(StringConverter.commaSeperatedStringList(nodeList));
        sb.append("],\nunknown: [");
        nodeList = new ArrayList<String>();
        for (Node node : unknown) {
            try {
                nodeList.add(node.getId().toString());
            } catch (NullPointerException ex) {
                nodeList.add("Null");
            }
        }
        sb.append(StringConverter.commaSeperatedStringList(nodeList));
        sb.append("]\n}\n");
        nodeList = new ArrayList<String>();
        return sb.toString();
    }

    public Set<Node> getPrimary() {
        return primary;
    }

    public void setPrimary(Set<Node> primary) {
        this.primary = primary;
    }

    public Set<Node> getSecondary() {
        return secondary;
    }

    public void setSecondary(Set<Node> secondary) {
        this.secondary = secondary;
    }

    public Set<Node> getUnknown() {
        return unknown;
    }

    public void setUnknown(Set<Node> unknown) {
        this.unknown = unknown;
    }
}
