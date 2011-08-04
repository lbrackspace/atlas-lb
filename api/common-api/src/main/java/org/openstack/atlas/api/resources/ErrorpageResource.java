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


public class ErrorpageResource extends CommonDependencyProvider{
    private final Log LOG = LogFactory.getLog(ErrorpageResource.class);
    private int loadBalancerId;
    private int accountId;

    @GET
    public Response retrieveErrorpage(){
        Errorpage errorpage = new Errorpage();
        UserPages up;
        try {
            up = loadBalancerService.getUserPages(loadBalancerId, accountId);
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, null,null);
        }
        if(up==null || up.getErrorpage() == null){
            return ResponseFactory.getResponseWithStatus(Response.Status.NOT_FOUND, "errorpage not found");
        }
        errorpage.setContent(up.getErrorpage());
        Response resp = Response.status(200).entity(errorpage).build();
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
