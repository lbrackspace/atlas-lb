package org.rackspace.capman.tools.ca.zeus.primitives;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.rackspace.capman.tools.ca.primitives.RsaConst;

public class ZeusCrtFile {

    private List<ErrorEntry> errors;
    private String public_cert;
    private String private_key;

    static{
        RsaConst.init();
    }

    public ZeusCrtFile() {
        errors = new ArrayList<ErrorEntry>();
        public_cert = "";
        private_key = "";
    }

    public boolean hasFatalErrors() {
        return getFatalErrors().size() > 0;
    }

    // Get errors that can't be ignored
    public List<ErrorEntry> getFatalErrors() {
        List<ErrorEntry> fatalErrors = new ArrayList<ErrorEntry>();
        for (ErrorEntry errorEntry : errors) {
            if (errorEntry.isFatal()) {
                fatalErrors.add(errorEntry);
            }
        }
        return fatalErrors;
    }

    public List<ErrorEntry> getExceptionErrors() {
        List<ErrorEntry> exceptionErrors = new ArrayList<ErrorEntry>();
        for (ErrorEntry errorEntry : errors) {
            if (errorEntry.getException() != null) {
                exceptionErrors.add(errorEntry);
            }
        }
        return errors;
    }

    public List<ErrorEntry> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorEntry> errors) {
        this.errors = errors;
    }

    public String getPublic_cert() {
        return public_cert;
    }

    public void setPublic_cert(String public_cert) {
        this.public_cert = public_cert;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public List<ErrorEntry> getErrorsMatchingTypes(ErrorType... eTypes) {
        return ErrorEntry.matchErrorTypes(errors, eTypes);
    }

    public boolean containsErrorTypes(ErrorType... eTypes) {
        List<ErrorEntry> errorEntries = getErrorsMatchingTypes(eTypes);
        return !errorEntries.isEmpty();
    }

    public String errorStrings(boolean showException) {
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
        for (ErrorEntry errorEntry : errors) {
            sb.append(errorEntry.toString(showException));
        }
        return sb.toString();
    }
}
