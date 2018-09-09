package org.openstack.atlas.api.mgmt.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.HostStatus;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;

public class HostStatusVerifier implements Verifier<HostStatus> {

    @Override
    public VerifierResult verify(HostStatus hostStatus) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        if (hostStatus != null) {
             validationResults.add(new ValidationResult(false, "Must not provide the host status."));
                return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }

}