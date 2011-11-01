package org.openstack.atlas.api.validation.verifier;

import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.api.validation.expectation.ValidationResult;

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
