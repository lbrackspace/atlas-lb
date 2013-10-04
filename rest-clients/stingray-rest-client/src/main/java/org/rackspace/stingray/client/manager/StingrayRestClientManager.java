package org.rackspace.stingray.client.manager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.config.Configuration;
import org.rackspace.stingray.client.config.StingrayRestClientConfiguration;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.manager.util.RequestManagerUtil;
import org.rackspace.stingray.client.util.ClientConstants;
import org.rackspace.stingray.client.util.StingrayRestClientUtil;

import java.net.URI;

public class StingrayRestClientManager {
    private static final Log LOG = LogFactory.getLog(StingrayRestClientManager.class);
    protected URI endpoint;
    protected Configuration config;
    protected Client client;
    protected boolean isDebugging;
    protected final String adminUser;
    protected final String adminKey;




    /**
     * Creates the client configured for authentication and security.

     * @param config      The object to retrieve configuration data
     * @param endpoint    The rest client endpoint
     * @param client      The client used to process requests
     */
    public StingrayRestClientManager(URI endpoint, Configuration config, Client client) {
        this(config, endpoint, client, false, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param endpoint      The REST client endpoint
     * @param config        The object to retrieve configuration data
     */
    public StingrayRestClientManager(URI endpoint, Configuration config) {
        this(config, endpoint, null, false, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param endpoint      The REST client endpoint
     */
    public StingrayRestClientManager(URI endpoint) {
        this(null, endpoint, null, false, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param endpoint      The REST client endpoint
     * @param adminUser     The configuration username
     * @param adminKey      The configuration user's key
     */
    public StingrayRestClientManager(URI endpoint, String adminUser, String adminKey) {
        this(null, endpoint, null, false, adminUser, adminKey);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param endpoint      The REST client endpoint
     * @param isDebugging   Boolean for switching context
     * @param adminUser     The configuration username
     * @param adminKey      The configuration user's key
     */
    public StingrayRestClientManager(URI endpoint, boolean isDebugging, String adminUser, String adminKey) {
        this(null, endpoint, null, isDebugging, adminUser, adminKey);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param isDebugging   Boolean for switching context
     */
    public StingrayRestClientManager(boolean isDebugging) {
        this(null, null, null, isDebugging, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     * @param configuration is supplied for a custom configuration.
     */
    public StingrayRestClientManager(Configuration configuration) {
        this(configuration, null, null, false, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     */
    public StingrayRestClientManager() {
        this(null, null, null, false, null, null);
    }

    /**
     * Creates the client configured for authentication and security.
     *
     * @param config      The object to retrieve configuration data
     * @param endpoint    The rest client endpoint
     * @param client      The client used to process requests
     * @param isDebugging Is debugging enabled for the client
     * @param adminUser   The admin user name
     * @param adminKey    The admin key or password
     */
    public StingrayRestClientManager(Configuration config, URI endpoint, Client client, boolean isDebugging,
                                     String adminUser, String adminKey) {

        if (config == null) {
            config = new StingrayRestClientConfiguration();
        }

        if (endpoint == null) {
            endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint)
                    + config.getString(ClientConfigKeys.stingray_base_uri));
        }

        if (client == null) {
            client = StingrayRestClientUtil.ClientHelper.createClient(false);
        }

        if (adminUser == null) {
            adminUser = config.getString(ClientConfigKeys.stingray_admin_user);
        }

        if (adminKey == null) {
            adminKey = config.getString(ClientConfigKeys.stingray_admin_key);
        }

        this.config = config;
        this.endpoint = endpoint;
        this.client = client;
        this.isDebugging = isDebugging;
        this.adminUser = adminUser;
        this.adminKey = adminKey;

        this.client.addFilter(new HTTPBasicAuthFilter(this.adminUser, this.adminKey));
    }

    /**
     * Retrieves and interprets the response entity.
     *
     * @param response  Holds all the values from the REST api response
     * @param clazz     Specific class to map the entity needed
     * @param <T>       Generic object for the declaration of the returned entity
     * @return          Returns an object of the passed type
     */
    public synchronized <T> T interpretResponse(ClientResponse response, Class<T> clazz)  throws StingrayRestClientException {
        T t;
        RequestManagerUtil rmu = new RequestManagerUtil();
        try {
            t = response.getEntity(clazz);
        } catch (Exception ex) {
            LOG.error("Could not retrieve object of type: " + clazz + " Exception: " + ex);
            if (!rmu.isResponseValid(response)) {
                throw new StingrayRestClientException(ClientConstants.REQUEST_ERROR, ex);
            }
            //The script calls dont return on POST/PUT...
            return null;
        }
        return t;
    }
}
