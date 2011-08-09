package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.datamodel.NodeCondition;

import java.util.List;

public class ActiveNodeVerifier implements Verifier<List<Node>> {

    public VerifierResult verify(List<Node> nodes) {
        if (nodes == null) return new VerifierResult(true);
        for (Node node : nodes) {
            if(NodeCondition.ENABLED.name().equals(node.getCondition())) {
                return new VerifierResult(true);
            }
        }
        return new VerifierResult(false);
    }
}

