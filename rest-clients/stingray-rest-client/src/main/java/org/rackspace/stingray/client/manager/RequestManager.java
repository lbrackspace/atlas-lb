package org.rackspace.stingray.client.manager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public interface RequestManager {

    ClientResponse getList(URI endpoint, Client client, String path) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    ClientResponse getItem(URI endpoint, Client client, String path, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    ClientResponse createItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    ClientResponse updateItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    boolean deleteItem(URI endpoint, Client client, String path) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;


}
