package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

public class HealthMonitorTypeVerifier implements Verifier<HealthMonitor> {
    private final HealthMonitorType type;

    public HealthMonitorTypeVerifier(HealthMonitorType type) {
        this.type = type;
    }

    @Override
    public VerifierResult verify(HealthMonitor monitor) {
        return new VerifierResult(monitor != null && type.equals(monitor.getType()));
    }
}
