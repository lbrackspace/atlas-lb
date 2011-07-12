package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.api.validation.results.ExpectationResult;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MustDelegateTo implements Verifier<Object> {

    private final Validator delegateValidator;
    private final Object delegateContext;

    public MustDelegateTo(Validator validator, Object delegateContext) {
        this.delegateValidator = validator;
        this.delegateContext = delegateContext;
    }

    @Override
    public VerifierResult verify(Object obj) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        try {
            if (obj instanceof Collection) {
                for (Object o : (Collection) obj) {
                    validationResults.addAll(fetchValidationResults(o));
                }
            } else {
                validationResults.addAll(fetchValidationResults(obj));
            }
        } catch (Exception ex) {
            //TODO: Catch actual exception
            ex.printStackTrace();
            validationResults.add(new ValidationResult(false, "Delegate context doesn't match."));
        }

        return new VerifierResult(validationResults.isEmpty(), validationResults);
    }

    private List<ValidationResult> fetchValidationResults(Object o) {
        List<ValidationResult> validationResults = new ArrayList<ValidationResult>();

        ValidatorResult validatorResult;
        validatorResult = delegateValidator.validate(o, delegateContext);

        if (!validatorResult.passedValidation()) {
            for (ExpectationResult expectationResult : validatorResult.getValidationResults()) {
                if (!expectationResult.expectationPassedValidation())
                    validationResults.add(new ValidationResult(false, expectationResult.getMessage()));
            }
        }

        return validationResults;
    }
}
