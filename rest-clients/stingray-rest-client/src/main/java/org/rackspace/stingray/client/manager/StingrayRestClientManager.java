package org.rackspace.stingray.client.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.rackspace.stingray.client.config.ClientConfigKeys;
import org.rackspace.stingray.client.config.Configuration;
import org.rackspace.stingray.client.config.StingrayRestClientConfiguration;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.manager.util.RequestManagerUtil;
import org.rackspace.stingray.client.util.ClientConstants;
import org.rackspace.stingray.client.util.StingrayRestClientUtil;


import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

public class StingrayRestClientManager {
    private static final Log LOG = LogFactory.getLog(StingrayRestClientManager.class);
    protected URI endpoint;
    protected Configuration config;
    protected Client client;
    protected boolean isDebugging;
    protected final String adminUser;
    protected final String adminKey;
    protected Integer read_timeout;
    protected Integer connect_timeout;



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
            try {
                adminKey = CryptoUtil.decrypt(config.getString(ClientConfigKeys.stingray_admin_key));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        this.config = config;
        this.endpoint = endpoint;
        this.client = client;
        this.isDebugging = isDebugging;
        this.adminUser = adminUser;
        this.adminKey = adminKey;

        try {
            this.read_timeout = Integer.parseInt(config.getString(ClientConfigKeys.stingray_read_timeout));
        } catch (Exception e) {
            this.read_timeout = 5000;
        }
        try {
            this.connect_timeout = Integer.parseInt(config.getString(ClientConfigKeys.stingray_connect_timeout));
        } catch (Exception e) {
            this.connect_timeout = 5000;
        }
        client.property(ClientProperties.CONNECT_TIMEOUT, this.connect_timeout);
        client.property(ClientProperties.READ_TIMEOUT, this.read_timeout);

        this.client.target(endpoint).register(HttpAuthenticationFeature.basic(this.adminUser, this.adminKey));
    }

    /**
     * Retrieves and interprets the response entity.
     *
     * @param <T>       Generic object for the declaration of the returned entity
     * @param response  Holds all the values from the REST api response
     * @param clazz     Specific class to map the entity needed
     * @return          Returns an object of the passed type
     */
    public synchronized <T> Object interpretResponse(Response response, Class<T> clazz)  throws StingrayRestClientException {
        Object t = null;
        String s;
        RequestManagerUtil rmu = new RequestManagerUtil();
        try {
            t = response.getEntity();
//              s = response.getEntity(String.class);
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
