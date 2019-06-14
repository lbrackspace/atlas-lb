package org.rackspace.stingray.client.manager;

import org.rackspace.stingray.client.exception.StingrayRestClientException;
import org.rackspace.stingray.client.exception.StingrayRestClientObjectNotFoundException;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public interface RequestManager {

    Response getList(URI endpoint, Client client, String path, String adminUser, String adminKey) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    Response getItem(URI endpoint, Client client, String path, MediaType cType, String adminUser, String adminKey) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    Response createItem(URI endpoint, Client client, String path, Object object, MediaType cType, String adminUser, String adminKey) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    Response updateItem(URI endpoint, Client client, String path, Object object, MediaType cType, String adminUser, String adminKey) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;

    Response deleteItem(URI endpoint, Client client, String path, String adminUser, String adminKey) throws StingrayRestClientException, StingrayRestClientObjectNotFoundException;


}
