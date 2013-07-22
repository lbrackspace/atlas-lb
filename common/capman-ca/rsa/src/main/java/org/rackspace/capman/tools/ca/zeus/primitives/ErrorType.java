package org.rackspace.capman.tools.ca.zeus.primitives;

public enum ErrorType {

    NO_PATH_TO_ROOT,
    EXPIRED_CERT,
    PREMATURE_CERT,
    KEY_CERT_MISMATCH,
    SIGNATURE_ERROR,
    UNREADABLE_CERT,
    UNREADABLE_KEY,
    COULDENT_ENCODE_CERT,
    COULDENT_ENCODE_KEY,
    UNKNOWN;
}
