package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;

import java.util.ArrayList;
import java.util.List;

public class SharedOrNewVipVerifier implements Verifier<List<VirtualIp>> {

    @Override
    public VerifierResult verify(List<VirtualIp> virtualIps) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        if (virtualIps == null || virtualIps.size() > 2) {
            validationResults.add(new ValidationResult(false, "Must have exactly one virtual ip"));
            return new VerifierResult(false, validationResults);
        }

        for (VirtualIp virtualIp : virtualIps) {
            if (virtualIp.getType() != null) {
                if (virtualIp.getId() != null) {
                    validationResults.add(new ValidationResult(false, "Must specify either a shared or new virtual ip."));
                    return new VerifierResult(false, validationResults);
                }
            } else {
                if (virtualIp.getId() == null) {
                    validationResults.add(new ValidationResult(false, "Must specify either a shared or new virtual ip."));
                    return new VerifierResult(false, validationResults);
                }
            }
        }

        if (virtualIps.size() >= 2) {
            for (VirtualIp virtualIp : virtualIps) {
                if (virtualIp.getIpVersion() != null || virtualIp.getType() != null) {
                    validationResults.add(new ValidationResult(false, "Must supply one virtual ip for a non-shared virtual ip request."));
                    return new VerifierResult(false, validationResults);
                }
            }
        }

        return new VerifierResult(true);
    }
}
