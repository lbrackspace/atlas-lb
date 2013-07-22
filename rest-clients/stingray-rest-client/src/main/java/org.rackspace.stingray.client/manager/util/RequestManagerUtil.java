package org.rackspace.stingray.client.manager.util;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;
import org.rackspace.stingray.client.util.ClientConstants;

import java.io.IOException;

public class RequestManagerUtil {
    private static final Log logger = LogFactory.getLog(RequestManagerUtil.class);

    /**
     * This method checks whether or not the status returned through the Stingray REST api response object is valid.
     *
     * @param response Holds all the details from the response of the Stingray REST api
     * @return Boolean result for the validity check
     */
    public  boolean isResponseValid(ClientResponse response) {
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
     * @throws StingrayRestClientException, StingrayRestClientObjectNotFoundException
     * @throws org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException
     */
    public void buildFaultMessage(ClientResponse response)
            throws StingrayRestClientException, StingrayRestClientObjectNotFoundException {

        String objectNotFound = "Invalid resource URI";
        String objectNotFoundMessage = "The resource does not exist.";
        String error;

        logger.info("ResponseWrapper, response status code is: " + response.getStatus());

            //TODO: ClientException seems to break for certain errors: ex: Exception entity: {"error_id":"resource.not_found","error_text":"Invalid resource URI"}
        //TODO: errors like these break response.GetEntity(ClientException.class) :::
        //TODO: {"properties":{"basic":{"disabled":["127.0.0.2:80"],"draining":[],"monitors":[],"nodes":["127.0.0.1:80"],"passive_monitoring":false},"connection":{},"load_balancing":{"algorithm":"weighted_least_connections","node_weighting":[{"node":"127.0.0.1:80"},{"node":"127.0.0.2:80"}],"priority_enabled":false,"priority_values":["127.0.0.1:80:2"]}}}
        try {
             error = response.getEntity(String.class);
            logger.debug(String.format("Client Request failed: %s", error.toString()));
        } catch (Exception ex) {
            //TODO: Temp fix
            logger.debug(String.format("Client Request failed: %s", ex));
//            logger.debug(String.format("Error to process.. %s", response.getEntity(String.class)));

            throw new StingrayRestClientException("Gathering error response entity failed, often means no response, body. Need to fix this bug...");
        }


        if (error.contains(objectNotFound)) {
            throw new StingrayRestClientObjectNotFoundException(objectNotFoundMessage);
        } else {
            throw new StingrayRestClientException(String.format("Caused By: %s", error));
        }
        //TODO: other messages?
    }
}
