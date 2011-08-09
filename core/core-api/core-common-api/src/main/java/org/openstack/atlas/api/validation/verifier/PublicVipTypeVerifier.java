package org.openstack.atlas.api.validation.verifier;


import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.api.validation.expectation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class PublicVipTypeVerifier implements Verifier<VipType> {

    @Override
    public VerifierResult verify(VipType type) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        if (type != VipType.PUBLIC) {
            validationResults.add(new ValidationResult(false, "Virtual Ip type must be PUBLIC"));
            return new VerifierResult(false, validationResults);
        }

        return new VerifierResult(true);
    }
}
