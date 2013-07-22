package org.rackspace.capman.tools.ca.zeus.primitives;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.rackspace.capman.tools.ca.StringUtils;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.util.StaticHelpers;

public class ErrorEntry {

    private ErrorType errorType;
    private String errorDetail;
    private boolean fatal; // Is this an unignorable error
    private Throwable exception;

    public ErrorEntry(ErrorType errorType, String errorDetail, boolean fatal, Throwable exception) {
        this.errorType = errorType;
        this.errorDetail = errorDetail;
        this.fatal = fatal;
        this.exception = exception;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean showException) {
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
        sb.append(String.format("{%s,%s,", errorType.toString(), errorDetail));
        sb.append(fatal ? "Fatal}" : "NotFatal}");
        if (!showException) {
            return sb.toString();
        }
        sb.append(" Exceptions:\n");
        List<Throwable> exceptions = StaticHelpers.getExceptionCausesList(exception);
        for (Throwable ex : exceptions) {
            sb.append(StringUtils.getEST(ex));
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static List<ErrorEntry> filterErrorTypes(List<ErrorEntry> errorsIn, ErrorType... errorTypes) {
        List<ErrorEntry> errorsOut = new ArrayList<ErrorEntry>();
        Set<ErrorType> errorTypeSet = getErrorTypeSet(errorTypes);

        for (ErrorEntry errorEntry : errorsIn) {
            if (errorTypeSet.contains(errorEntry.getErrorType())) {
                continue;
            }
            errorsOut.add(errorEntry);
        }
        return errorsOut;
    }

    public static List<ErrorEntry> matchErrorTypes(List<ErrorEntry> errorsIn, ErrorType... errorTypes) {
        List<ErrorEntry> errorsOut = new ArrayList<ErrorEntry>();
        Set<ErrorType> errorTypeSet = getErrorTypeSet(errorTypes);

        for (ErrorEntry errorEntry : errorsIn) {
            if (errorTypeSet.contains(errorEntry.getErrorType())) {
                errorsOut.add(errorEntry);
            }
        }
        return errorsOut;
    }

    public static Set<ErrorType> getErrorTypeSet(ErrorType... errorTypes) {
        List<ErrorEntry> errorsOut = new ArrayList<ErrorEntry>();
        Set<ErrorType> errorTypeSet = new HashSet<ErrorType>();
        for (ErrorType errorType : errorTypes) {
            errorTypeSet.add(errorType);
        }
        return errorTypeSet;
    }

    public static boolean hasFatal(List<ErrorEntry> errorEntries) {
        for (ErrorEntry errorEntry : errorEntries) {
            boolean isFatal = errorEntry.isFatal();
            if (isFatal) {
                return true;
            }
        }
        return false;
    }
}
