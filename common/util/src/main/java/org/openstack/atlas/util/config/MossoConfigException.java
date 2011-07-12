package org.openstack.atlas.util.config;

/**
 * Thrown if an error occurs while setting up or accessing data from Mosso configuration.
 */
public class MossoConfigException extends Exception {

    private static final long serialVersionUID = 9063217891914822042L;

    public MossoConfigException(String s) {
        super(s);
    }

    public MossoConfigException(String s, Throwable cause) {
        super(s);
    }

    public MossoConfigException(Throwable cause) {
        super(cause);
    }
}
