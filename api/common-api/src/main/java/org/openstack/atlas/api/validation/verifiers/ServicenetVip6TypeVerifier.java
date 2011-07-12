package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;

public class ServicenetVip6TypeVerifier implements Verifier<VirtualIp> {

    @Override
    public VerifierResult verify(VirtualIp virtualIp) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        if (virtualIp.getType() == VipType.SERVICENET && virtualIp.getIpVersion() == IpVersion.IPV6) {
            validationResults.add(new ValidationResult(false, "IPv6 virtual Ip type must be PUBLIC"));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}
