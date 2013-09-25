package org.openstack.atlas.restclients.atomhopper;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.cfg.Configuration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfiguration;
import org.openstack.atlas.restclients.atomhopper.config.AtomHopperConfigurationKeys;
import org.openstack.atlas.restclients.atomhopper.handler.AtomHopperClientHandler;
import org.openstack.atlas.restclients.atomhopper.util.AtomHopperUtil;
import ru.hh.jersey.hchttpclient.ApacheHttpClient;

import javax.ws.rs.core.MediaType;

public class AtomHopperClientImpl implements AtomHopperClient {
    private final Log LOG = LogFactory.getLog(AtomHopperClientImpl.class);
    private static Configuration configuration = new AtomHopperConfiguration();


    private String endPoint;
    private static final String TOKEN_HEADER = "X-AUTH-TOKEN";
    private ApacheHttpClient client;

    public AtomHopperClientImpl(String endPoint, ApacheHttpClient client) {
        this.endPoint = endPoint;
        if (endPoint == null) {
            this.endPoint = configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint);
        }
        this.client = client;
    }

    /**
     * This method will create the clients web resource based on the atom_hopper_endpoint
     * provided.
     *
     * @throws Exception
     */
    public AtomHopperClientImpl() throws Exception {
        this(configuration.getString(AtomHopperConfigurationKeys.atom_hopper_endpoint), AtomHopperClientHandler.createHttpClient());
    }


    /**
     * This method destroys the client
     */
    public void destroy() {
        this.client.destroy();
    }

    /**
     * This method will take an xml bean based object and POST it to the specified endpoint of the Atom-Hopper service
     *
     * @param entry the object to post
     * @return the ClientResponse
     */
    @Override
    public ClientResponse postEntry(Object entry) throws Exception {
        ClientResponse response = null;
        try {
            response = client.resource(endPoint)
                    .accept(MediaType.APPLICATION_XML)
                    .type(MediaType.APPLICATION_ATOM_XML)
                    .post(ClientResponse.class, entry);
        } catch (ClientHandlerException cpe) {
            throw new ClientHandlerException(AtomHopperUtil.getStackTrace(cpe));
        } catch (Exception ex) {
            throw new Exception(AtomHopperUtil.getStackTrace(ex));
        }
        return response;
    }

    /**
     * This method will take an xml bean based object and POST it to the specified endpoint of the Atom-Hopper service using
     * X-AUTH-TOKEN Header to identify the service..
     *
     * @param entry the object to post
     * @return the ClientResponse
     */
    @Override
    public ClientResponse postEntryWithToken(Object entry, String token) throws Exception {
        ClientResponse response = null;
        try {
            response = client.resource(endPoint)
                    .accept(MediaType.APPLICATION_XML)
                    .header(TOKEN_HEADER, token)
                    .type(MediaType.APPLICATION_ATOM_XML)
                    .post(ClientResponse.class, entry);
        } catch (ClientHandlerException cpe) {
            throw new ClientHandlerException(AtomHopperUtil.getStackTrace(cpe));
        } catch (Exception ex) {
            throw new Exception(AtomHopperUtil.getStackTrace(ex));
        }
        return response;
    }

    @Override
    public ClientResponse getEntry(String token, String uuid) throws Exception {
        ClientResponse response = null;
        String q = String.format("?marker=urn:uuid:%s&limit=1", uuid);
        try {
            LOG.debug("Retrieving from AH endpoint: " + endPoint + q);
            response = client.resource(endPoint + q)
                    .accept(MediaType.APPLICATION_XML)
                    .header(TOKEN_HEADER, token)
                    .type(MediaType.APPLICATION_ATOM_XML)
                    .get(ClientResponse.class);
        } catch (ClientHandlerException cpe) {
            throw new ClientHandlerException(AtomHopperUtil.getStackTrace(cpe));
        } catch (Exception ex) {
            throw new Exception(AtomHopperUtil.getStackTrace(ex));
        }
        return response;
    }
}
