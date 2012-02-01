package org.openstack.atlas.service.domain.services.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.NodeType;
import org.openstack.atlas.util.converters.StringConverter;

public class NodesPrioritiesContainer {

    private static final int SB_INIT_SIZE = 4096;
    private Set<Node> primary;
    private Set<Node> secondary;
    private Set<Node> unknown;
    private boolean containsPrimary;
    private boolean containsSecondary;

    public NodesPrioritiesContainer(Collection<Node> nodes) {
        primary = new HashSet<Node>();
        secondary = new HashSet<Node>();
        unknown = new HashSet<Node>();
        containsPrimary = false;
        containsSecondary = false;
        for (Node node : nodes) {
            if(node.getCondition() != NodeCondition.ENABLED) {
                continue; // This node doesn't count; It shoulden't be registered in Zeus
            }
            if (node.getType() == NodeType.PRIMARY) {
                primary.add(node);
                containsPrimary = true;
            } else if (node.getType() == NodeType.SECONDARY) {
                secondary.add(node);
                containsSecondary = true;
            } else {
                unknown.add(node);
                containsSecondary = true;
            }
        }
    }

    @Override
    public String toString() {
        List<String> nodeList;
        StringBuilder sb = new StringBuilder(SB_INIT_SIZE);
        sb.append("{primary: [");
        nodeList = new ArrayList<String>();
        for (Node node : getPrimary()) {
            try {
                nodeList.add(node.getId().toString());
            } catch (NullPointerException ex) {
                nodeList.add("Null");
            }
        }
        sb.append(StringConverter.commaSeperatedStringList(nodeList));
        sb.append("],\nsecondary: [");
        nodeList = new ArrayList<String>();
        for (Node node : getSecondary()) {
            try {
                nodeList.add(node.getId().toString());
            } catch (NullPointerException ex) {
                nodeList.add("Null");
            }
        }
        sb.append(StringConverter.commaSeperatedStringList(nodeList));
        sb.append("],\nunknown: [");
        nodeList = new ArrayList<String>();
        for (Node node : getUnknown()) {
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

    public Set<Node> getSecondary() {
        return secondary;
    }

    public Set<Node> getUnknown() {
        return unknown;
    }

    public boolean hasPrimary(){
        return containsPrimary;
    }

    public boolean hasSecondary(){
        return containsSecondary;
    }
}
