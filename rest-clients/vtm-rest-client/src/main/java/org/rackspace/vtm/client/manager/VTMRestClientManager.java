package org.rackspace.vtm.client.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.rackspace.vtm.client.config.ClientConfigKeys;
import org.rackspace.vtm.client.config.Configuration;
import org.rackspace.vtm.client.config.VTMRestClientConfiguration;
import org.rackspace.vtm.client.exception.VTMRestClientException;
import org.rackspace.vtm.client.manager.util.Authenticator;
import org.rackspace.vtm.client.manager.util.VTMRequestManagerUtil;
import org.rackspace.vtm.client.util.ClientConstants;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class VTMRestClientManager {
    private static final Log LOG = LogFactory.getLog(VTMRestClientManager.class);
    protected URI endpoint;
    protected Configuration config;
    protected Client client;
    protected boolean isDebugging;
    protected String adminUser;
    protected String adminKey;
    protected Integer read_timeout;
    protected Integer connect_timeout;


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
    public VTMRestClientManager(Configuration config, URI endpoint, Client client, boolean isDebugging,
                                String adminUser, String adminKey) {

        this.isDebugging = isDebugging;

        if (config == null) {
            this.config = new VTMRestClientConfiguration();
        } else {
            this.config = config;
        }

        if (endpoint == null) {
            this.endpoint = URI.create(this.config.getString(ClientConfigKeys.stingray_rest_endpoint)
                    + this.config.getString(ClientConfigKeys.stingray_base_uri));
        } else {
            this.endpoint = endpoint;
        }

        if (adminUser == null) {
            this.adminUser = this.config.getString(ClientConfigKeys.stingray_admin_user);
        } else {
            this.adminUser = adminUser;
        }

        if (adminKey == null) {
            try {
                this.adminKey = CryptoUtil.decrypt(this.config.getString(ClientConfigKeys.stingray_admin_key));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            this.adminKey = adminKey;
        }

        try {
            this.read_timeout = Integer.parseInt(this.config.getString(ClientConfigKeys.stingray_read_timeout));
        } catch (Exception e) {
            this.read_timeout = 5000;
        }
        try {
            this.connect_timeout = Integer.parseInt(this.config.getString(ClientConfigKeys.stingray_connect_timeout));
        } catch (Exception e) {
            this.connect_timeout = 5000;
        }

        /** Load or Create the client **/
        if (client == null) {
            this.client = this.createClient(false);
        } else {
            this.client = client;
        }
    }

    /**
     * Retrieves and interprets the response entity.
     *
     * @param <T>       Generic object for the declaration of the returned entity
     * @param response  Holds all the values from the REST api response
     * @param clazz     Specific class to map the entity needed
     * @return          Returns an object of the passed type
     */
    public synchronized <T> Object interpretResponse(Response response, Class<T> clazz)  throws VTMRestClientException {
        Object t = null;
        String s;
        VTMRequestManagerUtil rmu = new VTMRequestManagerUtil();
        try {
            t = response.readEntity(clazz);
        } catch (Exception ex) {
            LOG.error("Could not retrieve object of type: " + clazz + " Exception: " + ex);
            if (!rmu.isResponseValid(response)) {
                throw new VTMRestClientException(ClientConstants.REQUEST_ERROR, ex);
            }
            //The script calls dont return on POST/PUT...
            return null;
        }
        return t;
    }

    /**
     * @param isDebugging
     * @return Configured Client
     */
    public Client configureClient(boolean isDebugging) {
        TrustManager[] certs = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }
                }
        };
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, certs, new SecureRandom());
        } catch (java.security.GeneralSecurityException ex) {
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

        ClientConfig config = new ClientConfig();
        if (isDebugging) config.property(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, LoggingFeature.Verbosity.PAYLOAD_ANY);

        this.client =  ClientBuilder.newBuilder()
                .withConfig(config).sslContext(ctx)
                .hostnameVerifier((hostname, session) -> true).build();
        this.client.register(JacksonFeature.class);
        this.client.register(new Authenticator(this.adminUser, this.adminKey));
        this.client.property(ClientProperties.CONNECT_TIMEOUT, this.connect_timeout);
        this.client.property(ClientProperties.READ_TIMEOUT, this.read_timeout);

        return this.client;

    }

    public Client createClient(boolean isDebugging) {
        return this.configureClient(isDebugging);
    }

}
