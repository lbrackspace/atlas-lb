package org.openstack.atlas.api.validation.results;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ValidatorResult {
    private final List<ExpectationResult> expectationResultList;

    public ValidatorResult(List<ExpectationResult> expectationResultList) {
        this.expectationResultList = expectationResultList;
    }

    public boolean passedValidation() {
        return expectationResultList.isEmpty();
    }

    public List<ExpectationResult> getValidationResults() {
        return (List<ExpectationResult>) ((LinkedList<ExpectationResult>) expectationResultList).clone();
    }

    public List<String> getValidationErrorMessages() {
        List<String> validationErrorMessages = new ArrayList<String>();

        for (ExpectationResult expectationResult : expectationResultList) {
            if (!expectationResult.expectationPassedValidation() && expectationResult.getMessage() != null) {
                validationErrorMessages.add(expectationResult.getMessage());
            }
        }

        return validationErrorMessages;
    }
}
