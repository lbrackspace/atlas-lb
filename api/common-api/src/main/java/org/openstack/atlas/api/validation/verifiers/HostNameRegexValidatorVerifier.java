package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.expectation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class HostNameRegexValidatorVerifier implements Verifier<Object> {
    @Override
    public VerifierResult verify(Object obj) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();
        String hostNameRegex = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";

        try {
            //Input the string for validation
            Pattern p = Pattern.compile(hostNameRegex);
            //Match the given string with the pattern
            Matcher m = p.matcher(obj.toString());
            //check whether match is found
            boolean matchFound = m.matches();
            if (matchFound) {
                return new VerifierResult(true);
            } else if (!matchFound) {
                validationResults.add(new ValidationResult(false, "Must provide a valid host name"));
                return new VerifierResult(false, validationResults);
            }

        } catch (PatternSyntaxException exception) {
            validationResults.add(new ValidationResult(false, "Must provide a valid host name"));
            return new VerifierResult(false, validationResults);
        }
        return new VerifierResult(true, validationResults);
    }
}