package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;

import static org.openstack.atlas.docs.loadbalancers.api.v1.VipType.PUBLIC;

public class VipTypeVerifier implements Verifier<VirtualIp> {

    @Override
    public VerifierResult verify(VirtualIp vip) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        if (vip.getType() != null && vip.getIpVersion() == IpVersion.IPV6) {
            if (vip.getType() != PUBLIC) {
                validationResults.add(new ValidationResult(false, "Ip must be of PUBLIC type for IPV6 version."));
                return new VerifierResult(false, validationResults);
            }
        }
        
        return new VerifierResult(true);
    }
}
