package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;

import java.util.List;

public class ActiveNodeVerifier implements Verifier<Nodes> {

    public VerifierResult verify(Nodes nodes) {
        if (nodes == null) return new VerifierResult(true);
        for (Node node : nodes.getNodes()) {
            if (NodeCondition.ENABLED.equals(node.getCondition())) {
                return new VerifierResult(true);
            }
        }
        return new VerifierResult(false);
    }
}

