package org.openstack.atlas.restclients.auth.manager;


import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

public interface ResponseManager {
    Response get(Client client, URI uri, String token);

    Response get(Client client, URI uri, String token, MultivaluedStringMap params);

    Response post(Client client, URI uri, String body);

    Response post(Client client, URI uri, String token, String body);

    Response put(Client client, URI uri, String token, String body);

    Response delete(Client client, URI uri, String token);

    Response head(Client client, URI uri, String body);
}
