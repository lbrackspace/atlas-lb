package org.openstack.atlas.api.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.service.domain.entities.UserPages;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.api.config.RestApiConfiguration;
import org.openstack.atlas.service.domain.util.Constants;
public class ErrorpageResource extends CommonDependencyProvider{
    private final Log LOG = LogFactory.getLog(ErrorpageResource.class);
    private int loadBalancerId;
    private int accountId;

    @GET
    public Response retrieveErrorpage(){
        Errorpage errorpage = new Errorpage();
        String errorcontent;
        try {
            errorcontent = loadBalancerService.getErrorPage(loadBalancerId, accountId);
            if(errorcontent == null){
                throw new EntityNotFoundException("Errorpage was empty");
            }
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, null,null);
        }
        errorpage.setContent(errorcontent);
        Response resp = Response.status(200).entity(errorpage).build();
        return resp;
    }

    @PUT
    public Response setErrorPage(Errorpage errorpage){
        String content = errorpage.getContent();
        if(content == null){
            return getValidationFaultResponse("You must provide Content to set ErrorPage");
        }else if(content.length() > Constants.MAX_ERRORPAGE_CONTENT_LENGTH){
            String msg = String.format("Your content length must be less than %d bytes\n",Constants.MAX_ERRORPAGE_CONTENT_LENGTH);
            return getValidationFaultResponse(msg);
        }
        try {
            loadBalancerService.setErrorPage(loadBalancerId, accountId, content);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null,null);
        }

        return Response.status(202).build();
    }

    @DELETE
    public Response deleteErrorpage(){
        String errorcontent;

//        errorcontent = loadBalancerService.setErrorPage(loadBalancerId, accountId);
//        errorpage.setContent(errorcontent);
        Response resp = Response.status(200).build();
        return resp;
    }

    public Log getLOG() {
        return LOG;
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
