package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;

public class Ipv6VipVersionVerifier implements Verifier<IpVersion> {

    @Override
    public VerifierResult verify(IpVersion version) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        if (version != IpVersion.IPV6) {
            validationResults.add(new ValidationResult(false, "Ip version must be IPv6"));
            return new VerifierResult(false, validationResults);
        }

        return new VerifierResult(true);
    }

}
