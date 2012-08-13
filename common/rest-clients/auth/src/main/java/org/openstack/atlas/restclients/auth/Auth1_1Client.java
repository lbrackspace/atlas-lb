package org.openstack.atlas.restclients.auth;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.xml.datatype.XMLGregorianCalendar;
import org.openstack.atlas.restclients.auth.objects.AuthData;
import org.openstack.atlas.restclients.auth.objects.AuthFault;
import org.apache.commons.io.IOUtils;

import org.openstack.atlas.restclients.auth.objects.CustomObjects.RootElementUserCredentials;

public class Auth1_1Client {

    private String endPoint;
    private RootElementUserCredentials userCredentials;

    public Auth1_1Client(String endPoint, String user, String key) {
        this.endPoint = endPoint;
        userCredentials = new RootElementUserCredentials();
        userCredentials.setUsername(user);
        userCredentials.setKey(key);
    }

    public AuthData getAuthData() throws IOException {
        Object resp = getAuthResponse();
        if (!(resp instanceof AuthData)) {
            String fmt = "was expecting AuthData object but got %s object on in call to /auth";
            String msg = String.format(fmt, resp.getClass().getName());
            throw new IOException(msg);
        }
        AuthData authData = (AuthData) resp;
        return authData;
    }

    public Object getAuthResponse() {
        Client client = new Client();
        Builder reqBuilder = client.resource(endPoint).path("/auth").
                type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);
        ClientResponse resp = reqBuilder.post(ClientResponse.class, userCredentials);
        Object out = getAuthResponseEntity(resp);
        return out;
    }

    private Object getAuthResponseEntity(ClientResponse resp) {
        AuthFault unknown;
        Object out = null;
        int sc = resp.getClientResponseStatus().getStatusCode();
        switch (sc) {
            case 200:
            case 203:
                out = resp.getEntity(AuthData.class);
                break;
            case 400:
            case 401:
            case 403:
            case 500:
            case 503:
                out = resp.getEntity(AuthFault.class);
                break;
            default:
                InputStream is = resp.getEntityInputStream();
                try {
                    String body = IOUtils.toString(is, "UTF-8");
                    // At this point no telling what this problemis is so just throw
                    // an exception and let some one else deal with it.
                    String fmt = "Error validating user unknown response from Auth: %s";
                    String msg = String.format(fmt, body);
                    throw new RuntimeException(msg);
                } catch (IOException ex) {
                    throw new RuntimeException("Error fetching Auth token, Error reading resp from auth");
                }
        }
        return out;
    }
}
