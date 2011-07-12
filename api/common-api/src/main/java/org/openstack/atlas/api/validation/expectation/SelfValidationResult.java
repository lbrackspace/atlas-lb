/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstack.atlas.api.validation.expectation;

/**
 *
 * @author zinic
 */
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
