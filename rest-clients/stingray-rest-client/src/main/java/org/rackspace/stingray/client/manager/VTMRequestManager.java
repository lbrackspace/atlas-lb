package org.rackspace.stingray.client.manager;

import org.rackspace.stingray.client.exception.VTMRestClientException;
import org.rackspace.stingray.client.exception.VTMRestClientObjectNotFoundException;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public interface VTMRequestManager {

    Response getList(URI endpoint, Client client, String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException;

    Response getItem(URI endpoint, Client client, String path, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException;

    Response createItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException;

    Response updateItem(URI endpoint, Client client, String path, Object object, MediaType cType) throws VTMRestClientException, VTMRestClientObjectNotFoundException;

    Response deleteItem(URI endpoint, Client client, String path) throws VTMRestClientException, VTMRestClientObjectNotFoundException;


}
