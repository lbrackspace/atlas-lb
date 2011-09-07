package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.core.api.v1.Node;
import org.openstack.atlas.datamodel.CoreNodeCondition;

import java.util.List;

public class ActiveNodeVerifier implements Verifier<List<Node>> {

    public VerifierResult verify(List<Node> nodes) {
        if (nodes == null) return new VerifierResult(true);
        for (Node node : nodes) {
            // If node condition is null or "" it is defaulted to ENABLED
            if(CoreNodeCondition.ENABLED.equals(node.getCondition()) || node.getCondition() == null || node.getCondition().equals("")) {
                return new VerifierResult(true);
            }
        }
        return new VerifierResult(false);
    }
}

