package org.openstack.atlas.service.domain.services.helpers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openstack.atlas.service.domain.entities.Node;
import org.openstack.atlas.service.domain.entities.NodeType;

public class NodesPrioritiesContainer {
    private Set<Node> primary;
    private Set<Node> failover;
    private Set<Node> unknown;
    public NodesPrioritiesContainer(Collection <Node> nodes){
         primary = new HashSet<Node>();
         failover = new HashSet<Node>();
         unknown = new HashSet<Node>();

         for(Node node : nodes){
             if(node.getType() == NodeType.PRIMARY){
                 primary.add(node);
             }else if(node.getType() == NodeType.FAILOVER){
                 failover.add(node);
             }else{
                 unknown.add(node);
             }
         }
    }

    public Set<Node> getPrimary() {
        return primary;
    }

    public void setPrimary(Set<Node> primary) {
        this.primary = primary;
    }

    public Set<Node> getFailover() {
        return failover;
    }

    public void setFailover(Set<Node> failover) {
        this.failover = failover;
    }

    public Set<Node> getUnknown() {
        return unknown;
    }

    public void setUnknown(Set<Node> unknown) {
        this.unknown = unknown;
    }
}
