package org.openstack.atlas.restclients.auth.manager;


import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

public interface ResponseManager {
    public Response get(Client client, URI uri, String token);

    public Response get(Client client, URI uri, String token, MultivaluedStringMap params);

    public Response post(Client client, URI uri, String body);

    public Response post(Client client, URI uri, String token, String body);

    public Response put(Client client, URI uri, String token, String body);

    public Response delete(Client client, URI uri, String token);

    public Response head(Client client, URI uri, String body);
}
