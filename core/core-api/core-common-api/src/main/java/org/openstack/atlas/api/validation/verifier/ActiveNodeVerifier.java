package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.core.api.v1.Node;

import java.util.List;

public class ActiveNodeVerifier implements Verifier<List<Node>> {

    public VerifierResult verify(List<Node> nodes) {
        if (nodes == null) return new VerifierResult(true);
        for (Node node : nodes) {
            // If node condition is null it is defaulted to ENABLED
            if(node.isEnabled() == null || node.isEnabled()) {
                return new VerifierResult(true);
            }
        }
        return new VerifierResult(false);
    }
}

