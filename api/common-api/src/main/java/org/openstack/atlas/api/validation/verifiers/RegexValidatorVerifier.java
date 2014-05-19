package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.expectation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class RegexValidatorVerifier implements Verifier<Object> {
    @Override
    public VerifierResult verify(Object obj) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        return jValidate(obj, validationResults);
    }

    private VerifierResult jValidate(Object obj, List<ValidationResult> validationResults) {
        try {
            new jregex.Pattern(obj.toString());
        } catch (jregex.PatternSyntaxException exception) {
            validationResults.add(new ValidationResult(false,
                    "Must provide a valid Perl regex: " + exception.getMessage()));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true);
    }
}