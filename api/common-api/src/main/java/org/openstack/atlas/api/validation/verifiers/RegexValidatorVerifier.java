package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidatorVerifier implements Verifier<Object> {
    @Override
    public VerifierResult verify(Object obj) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        try {
            Pattern.compile(obj.toString());
        } catch (PatternSyntaxException exception) {
            validationResults.add(new ValidationResult(false, "Must provide a valid regex"));
                    return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}