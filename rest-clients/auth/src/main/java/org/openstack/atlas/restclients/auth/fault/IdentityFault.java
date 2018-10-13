package org.openstack.atlas.restclients.auth.fault;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://docs.rackspacecloud.com/auth/api/v1.1")
public class IdentityFault extends Exception {
    public String message;
    public String details;
    public int code;

    public IdentityFault() {
    }

    /**
     * Create a new identityException
     *
     * @param message
     * @param details
     * @param code
     */
    public IdentityFault(String message, String details, int code) {
        this.message = message;
        this.details = details;
        this.code = code;
    }

    /**
     * Get the message of the KetStoneException
     *
     * @return the message for this exception
     */
    @XmlAttribute(name = "message")
    public String getMessage() {
        return message;
    }

    /**
     * Set the message for identityException
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the details of the KetStoneException
     *
     * @return the details for this exception
     */
    @XmlAttribute(name = "details")
    public String getDetails() {
        return details;
    }

    /**
     * Set the details for identityException
     *
     * @param details
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Get the code of the KetStoneException
     *
     * @return the code for this exception
     */
    @XmlAttribute(name = "code")
    public int getCode() {
        return code;
    }

    /**
     * Set the code for identityException
     *
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }
}
