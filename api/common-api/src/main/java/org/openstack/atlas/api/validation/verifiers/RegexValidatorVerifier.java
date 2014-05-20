package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.expectation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class RegexValidatorVerifier implements Verifier<Object> {
    @Override
    public VerifierResult verify(Object obj) {
        return jValidate(obj);
    }

    private VerifierResult jValidate(Object obj) {
        try {
            new jregex.Pattern(obj.toString());
        } catch (jregex.PatternSyntaxException exception) {
            List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
            validationResults.add(new ValidationResult(false, exception.getMessage()));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}