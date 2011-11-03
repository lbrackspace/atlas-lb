package org.openstack.atlas.adapter.netscaler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.*;
import javax.ws.rs.core.Response.Status;

public class NSRequest
{
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    
    public static Log LOG = LogFactory.getLog(NetScalerAdapterImpl.class.getName());

    static String perform_request(String method, String urlStr, Map<String,String> headers, String body)
    throws IOException
    {
    	
    	ClientResponse response;
    	
        // sigh.  openConnection() doesn't actually open the connection,
        // just gives you a URLConnection.  connect() will open the connection.

        LOG.debug("[issuing request: " + method + " " + urlStr + "]");

        Client client = Client.create();
        WebResource  resource = client.resource(urlStr);

        WebResource.Builder resourceBuilder = resource.getRequestBuilder();
       
        URL url = new URL(urlStr);
        
        LOG.debug("Before writing headers...");

        
        LOG.debug("writing headers");
        // write  headers
        for (Map.Entry<String,String> header : headers.entrySet())
        {
            LOG.debug(header.getKey() +  ":" + header.getValue());

            resourceBuilder.header(header.getKey(), header.getValue());
        }


        response = null;
        
        if (method.toUpperCase() == "GET")
        {
        	LOG.debug("Doing a GET request...");
        	
        	response = resourceBuilder.get(ClientResponse.class);
        }
        
        if (method.toUpperCase() == "POST")
        {
        	LOG.debug("Doing a POST request...");
        	response = resourceBuilder.post(ClientResponse.class, body);
        }
        
        
        if (method.toUpperCase() == "PUT")
        {
        	LOG.debug("Doing a PUT request...");
        	response = resourceBuilder.put(ClientResponse.class, body);
        }
        
        if (method.toUpperCase() == "DELETE")
        {
        	LOG.debug("Doing a DELETE request...");
        	response = resourceBuilder.delete(ClientResponse.class);
        }
        
        String resp_body;
        
        if (response != null)
        {
        	int statuscode = response.getStatus();
        	

        	LOG.debug("Status code of response is: " + statuscode);
        
        	resp_body = response.getEntity(String.class);
        	LOG.debug("Response body: " + resp_body);
        	
        	if (method.toUpperCase() != "GET" && statuscode != Status.ACCEPTED.getStatusCode())
        	{
				if(statuscode != Status.CREATED.getStatusCode())
					throw new IOException("Error : " + resp_body);
        	}
        	
        } else {
        	LOG.debug("response was set to null");
        	resp_body = null;
        }
        
        return resp_body;
    }
}
