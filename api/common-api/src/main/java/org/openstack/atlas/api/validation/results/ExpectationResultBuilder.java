package org.openstack.atlas.api.validation.results;

import org.openstack.atlas.api.validation.exceptions.ValidationException;

/**
 *
 * @author John Hopper
 */
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
