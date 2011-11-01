package org.openstack.atlas.api.validation.verifier;

import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;

public class HealthMonitorTypeVerifier implements Verifier<HealthMonitor> {
    private final CoreHealthMonitorType healthMonitorType;

    public HealthMonitorTypeVerifier(CoreHealthMonitorType healthMonitorType) {
        this.healthMonitorType = healthMonitorType;
    }

    @Override
    public VerifierResult verify(HealthMonitor monitor) {
        return new VerifierResult(monitor != null && healthMonitorType.getType().equals(monitor.getType()));
    }
}
