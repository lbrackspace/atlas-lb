package org.rackspace.stingray.client.manager.util;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rackspace.stingray.client.exception.ClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.util.ClientConstants;

public class RequestManagerUtil {
    private static final Log logger = LogFactory.getLog(RequestManagerUtil.class);

    /**
     * This method checks whether or not the status returned through the Stingray REST api response object is valid.
     *
     * @param response Holds all the details from the response of the Stingray REST api
     * @return Boolean result for the validity check
     */
    public static boolean isResponseValid(ClientResponse response) {
        return (response != null && (response.getStatus() == ClientConstants.ACCEPTED
                || response.getStatus() == ClientConstants.NON_AUTHORATIVE
                || response.getStatus() == ClientConstants.OK
                || response.getStatus() == ClientConstants.NO_CONTENT
                || response.getStatus() == ClientConstants.CREATED));
    }

    /**
     * A method to build a message detailing a failure response from the Stingray REST api
     *
     * @param response Holds all the details from the response of the Stingray REST api
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     */
    public static void buildFaultMessage(ClientResponse response)
            throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {

        String objectNotFound = "Invalid resource URI";
        String objectNotFoundMessage = "The resource does not exist.";
        ClientException exception = null;

        logger.info("ResponseWrapper, response status code is: " + response.getStatus());
        try {
            exception = response.getEntity(ClientException.class);
            logger.debug(String.format("Client Request failed: %s", exception.toString()));
        } catch (Exception ex) {
            logger.error("Exception was thrown and could not be handled by the client. " +
                    "Response status code: " + response.getStatus());
            throw new StingrayRestClientException(ClientConstants.CLIENT_ERROR, ex);
        }

        if (exception.getError_text().contains(objectNotFound)) {
            throw new StingrayRestClientObjectNotFoundException(objectNotFoundMessage);
        } else {
            throw new StingrayRestClientException(String.format("Caused By: %s: Reason: %s: Additional: %s",
                    exception.getError_id(), exception.getError_text(), exception.getError_info()));
        }
        //TODO: other messages?
    }
}
