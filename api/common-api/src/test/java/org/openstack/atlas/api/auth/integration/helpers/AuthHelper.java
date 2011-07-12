package org.openstack.atlas.api.auth.integration.helpers;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AuthHelper {
    static final String AUTH_LOCATION_URI = "https://172.17.0.64/auth";
    static final String USERNAME_HEADER_KEY = "X-Auth-User";
    static final String API_KEY_HEADER_KEY = "X-Auth-Key";
    static final String AUTH_TOKEN_HEADER = "X-Auth-Token";

    static URL AUTH_URL;

    static {
        try {
            AUTH_URL = new URL(AUTH_LOCATION_URI);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static String getAuthToken(String userName, String apiKey) throws IOException {

        URLConnection connection = AUTH_URL.openConnection();
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection c = (HttpsURLConnection) connection;
            c.setHostnameVerifier(new CustomHostNameVerifier());
        }
        connection.setRequestProperty(USERNAME_HEADER_KEY, userName);
        connection.setRequestProperty(API_KEY_HEADER_KEY, apiKey);
        connection.connect();

        return connection.getHeaderField(AUTH_TOKEN_HEADER);
    }
}
