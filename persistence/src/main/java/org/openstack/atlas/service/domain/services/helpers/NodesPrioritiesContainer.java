package org.openstack.atlas.service.domain.services.helpers;

import java.util.Collection;
import java.util.Set;
import org.openstack.atlas.service.domain.entities.Node;

public class NodesPrioritiesContainer {
    Set<Node> primary;
    Set<Node> secondary;

    public NodesPrioritiesContainer(Collection <Node> nodes){

    }
}
