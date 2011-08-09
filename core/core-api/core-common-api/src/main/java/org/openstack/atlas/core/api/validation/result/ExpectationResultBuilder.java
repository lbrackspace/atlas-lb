package org.openstack.atlas.core.api.validation.result;

import org.openstack.atlas.core.api.validation.exception.ValidationException;

public class ExpectationResultBuilder {

    private ValidationException error;
    private String message, targetName;
    private boolean passed;

    public ExpectationResultBuilder(String targetName) {
        this.targetName = targetName;

        error = null;
        passed = true;
        message = "";
    }

    public ExpectationResult toResult() {
        return new ExpectationResult(error, message, targetName, passed);
    }

    public void setError(ValidationException error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
