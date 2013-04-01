package org.openstack.atlas.restclients.atomhopper;

import com.sun.jersey.api.client.ClientResponse;

public interface AtomHopperClient {

    ClientResponse postEntry(Object entry) throws Exception;

    ClientResponse postEntryWithToken(Object entry, String token) throws Exception;

    void destroy();
}
