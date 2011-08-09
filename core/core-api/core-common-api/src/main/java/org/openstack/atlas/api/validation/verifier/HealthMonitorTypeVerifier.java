package org.openstack.atlas.api.validation.verifier;

import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.datamodel.HealthMonitorType;

public class HealthMonitorTypeVerifier implements Verifier<HealthMonitor> {
    private final HealthMonitorType type;

    public HealthMonitorTypeVerifier(HealthMonitorType type) {
        this.type = type;
    }

    @Override
    public VerifierResult verify(HealthMonitor monitor) {
        return new VerifierResult(monitor != null && type.name().equals(monitor.getType()));
    }
}
