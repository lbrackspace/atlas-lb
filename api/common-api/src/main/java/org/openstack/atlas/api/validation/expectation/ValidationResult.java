
package org.openstack.atlas.api.validation.expectation;

public class ValidationResult {
    private final boolean expectationWasMet;
    private final String message;

    public ValidationResult(boolean expectationWasMet, String message) {
        this.expectationWasMet = expectationWasMet;
        this.message = message;
    }

    public boolean expectationWasMet() {
        return expectationWasMet;
    }

    public String getMessage() {
        return message;
    }
}
