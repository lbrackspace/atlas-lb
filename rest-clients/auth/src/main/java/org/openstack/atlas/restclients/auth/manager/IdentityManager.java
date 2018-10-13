package org.openstack.atlas.restclients.auth.manager;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.openstack.atlas.restclients.auth.util.IdentityUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;


public abstract class IdentityManager {
    private String auth_uri = "auth_uri";
    protected String url;
    protected Client client;
    protected boolean isDebugging;
    public int timeout = 3000;

    /**
     * @param authUrl the url to the identity auth service
     * @param client  the Jersey client used to talk to identity
     */
    public IdentityManager(String authUrl, Client client) {
        this(authUrl, client, false);
    }

    /**
     * Allows configuration of the timeout value for connection and read
     * @param authUrl
     * @param timeout
     */
    public IdentityManager(String authUrl, int timeout) {
        this.url = authUrl;
        this.client = ClientBuilder.newBuilder().build();
        this.timeout = timeout;
    }

    /**
     * Allows configuration of the timeout value for connection and read
     * @param authUrl
     * @param timeout
     * @param isDebugging
     */
    public IdentityManager(String authUrl, int timeout, boolean isDebugging) {
        this(authUrl, ClientBuilder.newBuilder().build(), timeout, isDebugging);
    }

    /**
     * @param authUrl     the url to the identity auth service
     * @param client      the Jersey client used to talk to identity
     * @param isDebugging allows debugging option
     */
    public IdentityManager(String authUrl, Client client, boolean isDebugging) {
       this(authUrl, client, 3000, isDebugging);
    }

    /**
     * @param authUrl     the url to the identity auth service
     * @param client      the Jersey client used to talk to identity
     * @param timeout
     * @param isDebugging allows debugging option
     */
    public IdentityManager(String authUrl, Client client, int timeout, boolean isDebugging) {
        this.url = authUrl;
        this.timeout = timeout;
        if (authUrl == null) {
            this.url = IdentityUtil.getProperty(auth_uri);
        }
        if (client == null) {
            this.client = configureClient(isDebugging);
        } else {
            this.client = client;
        }
    }

    /**
     * This method will create the clients web resource based on the auth uri
     * found in the properties file
     *
     * @param authUrl the url to the identity auth service
     */
    public IdentityManager(String authUrl) {
        this(authUrl, false);
    }

    /**
     * This method will create the clients web resource based on the auth uri
     * found in the properties file and allows debugging option
     *
     * @param authUrl the url to the identity auth service
     * @param isDebugging allows debugging option
     */
    public IdentityManager(String authUrl, boolean isDebugging) {
        this(authUrl, ClientBuilder.newBuilder().build(), isDebugging);
    }

    /**
     * This method uses the default authUrl found in
     * the properties file
     */
    public IdentityManager() {
        this(false);
    }

    /**
     * This method uses the default authUrl found in
     * the properties file and allows for debugging option
     * @param isDebugging allows for debugging option
     */
    public IdentityManager(boolean isDebugging) {
        this.url = IdentityUtil.getProperty(auth_uri);
        this.client = ClientBuilder.newBuilder().build();
        this.isDebugging = isDebugging;
    }


    private Client configureClient(boolean isDebugging) {
        ClientConfig cc = new ClientConfig();
        cc.getProperties().put(ClientProperties.FOLLOW_REDIRECTS, false);
        cc.getProperties().put(ClientProperties.CONNECT_TIMEOUT, timeout);
        cc.getProperties().put(ClientProperties.READ_TIMEOUT, timeout);
        Client client = ClientBuilder.newBuilder().withConfig(cc).build();
        if (isDebugging) {
            client.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, "FINEST");
        }
        return client;
    }
}
