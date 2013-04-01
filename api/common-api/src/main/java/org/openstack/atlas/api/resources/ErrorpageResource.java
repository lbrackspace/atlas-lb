package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.util.Constants;

import javax.jms.JMSException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;


public class ErrorpageResource extends CommonDependencyProvider{
    private final Log LOG = LogFactory.getLog(ErrorpageResource.class);
    private int loadBalancerId;
    private int accountId;

    @GET
    public Response retrieveErrorpage() {
        Errorpage errorpage = new Errorpage();
        String errorcontent;
        try {
            errorcontent = loadBalancerService.getErrorPage(loadBalancerId, accountId);
            if(errorcontent == null){
                errorcontent = loadBalancerService.getDefaultErrorPage();
            }
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null,null);
        }
        errorpage.setContent(errorcontent);
        Response resp = Response.status(200).entity(errorpage).build();
        return resp;
    }

    @DELETE
    public Response deleteErrorpage() {
        try {
            loadBalancerService.removeErrorPage(loadBalancerId, accountId);
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        } catch (UnprocessableEntityException ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        } catch (ImmutableEntityException ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

        MessageDataContainer container = new MessageDataContainer();
        container.setAccountId(accountId);
        container.setLoadBalancerId(loadBalancerId);
        try {
            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_ERRORFILE, container);
        } catch (JMSException e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
        Response resp = Response.status(202).build();
        return resp;
    }

    @PUT
    public Response setErrorPage(Errorpage errorpage){
        MessageDataContainer dataContainer;
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
        dataContainer = new MessageDataContainer();
        dataContainer.setAccountId(accountId);
        dataContainer.setLoadBalancerId(loadBalancerId);
        //QuickFix for V1-D-12106
//        content = "\n" + content;
        dataContainer.setErrorFileContents(content);
        try {
            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_ERRORFILE, dataContainer);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null,null);
        }
        return Response.status(202).build();
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
