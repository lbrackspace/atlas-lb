
package org.rackspace.capman.tools.ca.exceptions;

public class X509PathBuildException extends CapManUtilException{

    public X509PathBuildException(Throwable cause) {
        super(cause);
    }

    public X509PathBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public X509PathBuildException(String message) {
        super(message);
    }

    public X509PathBuildException() {
    }
}
