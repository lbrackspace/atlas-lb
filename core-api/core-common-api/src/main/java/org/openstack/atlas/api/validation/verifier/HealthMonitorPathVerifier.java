package org.openstack.atlas.api.validation.verifier;

public class HealthMonitorPathVerifier implements Verifier {

    @Override
    public VerifierResult verify(Object obj) {
        char firstChar = obj.toString().charAt(0);
        String slashString = "/";
        char slash = slashString.charAt(0);
        if (firstChar == slash) return new VerifierResult(true);
        return new VerifierResult(false);    }
}