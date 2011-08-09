package org.openstack.atlas.core.api.validation.verifier;


import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.openstack.atlas.core.api.validation.expectation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class ServicenetVip6TypeVerifier implements Verifier<VirtualIp> {

    @Override
    public VerifierResult verify(VirtualIp virtualIp) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        if (virtualIp.getType() == VipType.SERVICE_NET && virtualIp.getIpVersion() == IpVersion.IPV6) {
            validationResults.add(new ValidationResult(false, "IPv6 virtual Ip type must be PUBLIC"));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}
