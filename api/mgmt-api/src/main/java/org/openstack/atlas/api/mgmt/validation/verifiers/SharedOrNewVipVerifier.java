package org.openstack.atlas.api.mgmt.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;

public class SharedOrNewVipVerifier implements Verifier<VirtualIp> {

    @Override
    public VerifierResult verify(VirtualIp virtualIp) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

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

        return new VerifierResult(true);
    }

}
