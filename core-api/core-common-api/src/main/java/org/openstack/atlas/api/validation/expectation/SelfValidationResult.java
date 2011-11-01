package org.openstack.atlas.api.validation.expectation;

public class SelfValidationResult {
    private final StringBuilder errorBuffer;
    private final boolean valid;

    public SelfValidationResult(StringBuilder errorBuffer, boolean valid) {
        this.errorBuffer = errorBuffer;
        this.valid = valid;
    }

    public StringBuilder getErrorBuffer() {
        return errorBuffer;
    }

    public boolean isValid() {
        return valid;
    }
}
