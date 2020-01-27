package org.rackspace.stingray.client.manager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;
import org.rackspace.stingray.client.util.ClientConstants;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class VTMRequestManagerUtil {
    private static final Log logger = LogFactory.getLog(VTMRequestManagerUtil.class);

    /**
     * This method checks whether or not the status returned through the Stingray REST api response object is valid.
     *
     * @param response Holds all the details from the response of the Stingray REST api
     * @return Boolean result for the validity check
     */
    public boolean isResponseValid(Response response) {
        return (response != null && (response.getStatus() == ClientConstants.ACCEPTED
                || response.getStatus() == ClientConstants.NON_AUTHORATIVE
                || response.getStatus() == ClientConstants.OK
                || response.getStatus() == ClientConstants.NO_CONTENT
                || response.getStatus() == ClientConstants.CREATED));
    }


    public <T> T stringToObject(String str, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object myObject = mapper.readValue(str, clazz);
        return (T) myObject;

    }


    /**
     * A method to build a message detailing a failure response from the Stingray REST api
     *
     * @param response Holds all the details from the response of the Stingray REST api
     * @throws VTMRestClientException, VTMRestClientObjectNotFoundException
     * @throws VTMRestClientObjectNotFoundException
     *
     */
    public void buildFaultMessage(Response response)
            throws VTMRestClientException, VTMRestClientObjectNotFoundException {

        String objectNotFoundMessage = "does not exist";
        String objectNotFound = "Invalid resource URI";
        String notFound = "not found";

        String error;


        try {
            logger.info("ResponseWrapper, response status code is: " + response.getStatus());
            error = response.readEntity(String.class); //Too many permutations of errors, catch all and pull out what we want...
            logger.debug(String.format("Client Request failed: %s", error));
        } catch (Exception ex) {
            logger.debug(String.format("Client Request failed: %s", ex));
            throw new VTMRestClientException(String.format("Gathering error response entity failed: %s", ex));
        }

        if (error.contains(objectNotFound) || error.contains(objectNotFoundMessage) || error.contains(notFound)) {
            throw new VTMRestClientObjectNotFoundException(String.format("Error processing request: Caused By: %s: ",
                    error));
        } else {
            throw new VTMRestClientException(String.format("Error processing request: Caused By: %s: ",
                    error));
        }
    }
}
