package org.openstack.atlas.api.validation.verifier;

import org.openstack.atlas.api.validation.expectation.ValidationResult;

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