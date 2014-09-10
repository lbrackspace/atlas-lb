package org.openstack.atlas.api.validation.verifiers;

import org.openstack.atlas.api.validation.expectation.ValidationResult;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

import java.util.ArrayList;
import java.util.List;

public class CertificateVerifier implements Verifier<String> {
    @Override
    public VerifierResult verify(String certificate) {
        if (certificate == null) {
            return new VerifierResult(false);
        }

        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        ZeusUtils.parseCert(certificate, errors);

        if (!errors.isEmpty()) {
            List<ValidationResult> errorList = new ArrayList<ValidationResult>();

            for (ErrorEntry errorEntry : errors) {
                ValidationResult result;

                switch (errorEntry.getErrorType()) {
                    case UNREADABLE_CERT:
                        result = new ValidationResult(false, "Certificate is unreadable.");
                        break;
                    case COULDENT_ENCODE_CERT:
                        result = new ValidationResult(false, "Could not properly encode certificate.");
                        break;
                    default:
                        result = new ValidationResult(false, "Certificate is unreadable.");
                }

                errorList.add(result);
            }

            return new VerifierResult(false, errorList);
        }

        return new VerifierResult(true);
    }
}
