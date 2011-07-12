package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

public class HealthMonitorPathVerifier implements Verifier {

    @Override
    public VerifierResult verify(Object obj) {
        char firstChar = obj.toString().charAt(0);
        String slashString = "/";
        char slash = slashString.charAt(0);
        if (firstChar == slash) return new VerifierResult(true);
        return new VerifierResult(false);    }
}