package org.openstack.atlas.restclients.auth.wrapper;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.restclients.auth.common.IdentityConstants;
import org.openstack.atlas.restclients.auth.fault.IdentityFault;
import org.openstack.identity.client.faults.*;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

public class IdentityResponseWrapper {
    private static final Log logger = LogFactory.getLog(IdentityResponseWrapper.class);

    public static IdentityFault buildFaultMessage(Response response) throws IdentityFault {
        //TODO: this can be better
        if (response != null) {
            logger.info("ResponseWrapper, response status code is: " + response.getStatus());
            try {
                if (response.getStatus() == IdentityConstants.BAD_REQUEST) {
                    BadRequestFault e = response.readEntity(BadRequestFault.class);
                        return processIdentityFault(e);
                }
                if (response.getStatus() == IdentityConstants.UNAUTHORIZED) {
                    UnauthorizedFault e = response.readEntity(UnauthorizedFault.class);
                    return new IdentityFault(e.getMessage(), e.getDetails(), e.getCode());
                }
                if (response.getStatus() == IdentityConstants.FORBIDDEN) {
                    UserDisabledFault udf = response.readEntity(UserDisabledFault.class);
                    if (udf != null) {
                        return processIdentityFault(udf);
                    } else {
                        ForbiddenFault e = response.readEntity(ForbiddenFault.class);
                        return processIdentityFault(e);
                    }
                }
                if (response.getStatus() == IdentityConstants.NOT_FOUND) {
                    ItemNotFoundFault e = response.readEntity(ItemNotFoundFault.class);
                    return processIdentityFault(e);
                }
                if (response.getStatus() == IdentityConstants.NOT_PERMITTED) {
                    return new IdentityFault("Operation not allowed", "The requested resource or operation could not be found", IdentityConstants.NOT_PERMITTED);
                }
                if (response.getStatus() == IdentityConstants.NAME_CONFLICT) {
                    TenantConflictFault e = response.readEntity(TenantConflictFault.class);
                    return processIdentityFault(e);
                }
                if (response.getStatus() == IdentityConstants.SERVICE_UNAVAILABLE) {
                    ServiceUnavailableFault e = response.readEntity(ServiceUnavailableFault.class);
                    return processIdentityFault(e);
                }
                if (response.getStatus() == IdentityConstants.NOT_IMPLMENTED) {
                    BadRequestFault e = response.readEntity(BadRequestFault.class);
                    return processIdentityFault(e);
                }
                if (response.getStatus() == IdentityConstants.AUTH_FAULT) {
                    org.openstack.identity.client.faults.IdentityFault e = response.readEntity(org.openstack.identity.client.faults.IdentityFault.class);
                    return processIdentityFault(e);
                }
            } catch (Exception ex) {
                logger.error("Exception was thrown for response with status: " + response.getStatus());
                if (ex instanceof UnsupportedOperationException) {
                    throw new IdentityFault("There was an error communicating with the auth service...", ex.getLocalizedMessage(), response.getStatus());
                }
                logger.error("Exception was thrown and could not be handled by the client. Response status code: " + response.getStatus());
                throw new IdentityFault(ex.getMessage(), ex.getLocalizedMessage(), response.getStatus());
            }
        }
        return new IdentityFault("Unable to process request.", "There was an unexpected error communicating with the auth service.", IdentityConstants.AUTH_FAULT);
    }

    private static IdentityFault processIdentityFault(org.openstack.identity.client.faults.IdentityFault e) throws JAXBException {
        org.openstack.identity.client.faults.IdentityFault ie = null;
        if (e.getMessage() == null && e.getDetails() == null) {
            ElementNSImpl dom = (ElementNSImpl) e.getAny().get(1);
            return new IdentityFault(dom.getFirstChild().getTextContent(), dom.getFirstChild().getTextContent(), e.getCode());

        }
        return new IdentityFault(e.getMessage(), e.getDetails(), e.getCode());
    }
}
