package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.ErrorPage;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.operation.RaxOperation;
import org.openstack.atlas.rax.domain.repository.RaxUserPagesRepository;
import org.openstack.atlas.rax.domain.service.RaxUserPagesService;
import org.openstack.atlas.service.domain.common.Constants;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

@Primary
@Controller
@Scope("request")
public class RaxErrorPageResource extends CommonDependencyProvider {

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @Autowired
    protected RaxUserPagesRepository userPagesRepository;

    @Autowired
    protected RaxUserPagesService raxUserPagesService;

    protected Integer accountId;
    protected Integer loadBalancerId;

    @GET
    public Response retrieveErrorpage() {
        ErrorPage errorpage = new ErrorPage();
        String errorcontent;
        try {
            errorcontent = userPagesRepository.getErrorPageByAccountIdLoadBalancerId(accountId, loadBalancerId);
            if (errorcontent == null) {
                errorcontent = userPagesRepository.getDefaultErrorPage().getValue();
            }
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        errorpage.setContent(errorcontent);
        return Response.status(200).entity(errorpage).build();
    }

    @DELETE
    public Response deleteErrorpage() {
        try {
            userPagesRepository.deleteErrorPage(accountId, loadBalancerId);

            MessageDataContainer container = new MessageDataContainer();
            container.setAccountId(accountId);
            container.setLoadBalancerId(loadBalancerId);
            asyncService.callAsyncLoadBalancingOperation(RaxOperation.DELETE_ERROR_PAGE, container);

            Response resp = Response.status(200).build();
            return resp;
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @PUT
    public Response setErrorPage(ErrorPage errorpage) {
        try {
            String content = errorpage.getContent();
            if (content == null) {
                return ResponseFactory.getValidationFaultResponse("You must provide Content to set ErrorPage");
            } else if (content.length() > Constants.MAX_ERRORPAGE_CONTENT_LENGTH) {
                String msg = String.format("Your content length must be less than %d bytes\n", Constants.MAX_ERRORPAGE_CONTENT_LENGTH);
                return ResponseFactory.getValidationFaultResponse(msg);
            }

            RaxLoadBalancer loadBalancer = (RaxLoadBalancer) loadBalancerRepository.getByIdAndAccountId(loadBalancerId, accountId);
            raxUserPagesService.setErrorPage(accountId, loadBalancerId, content);

            MessageDataContainer dataContainer;
            dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setLoadBalancer(loadBalancer);
            dataContainer.setResource(content);  // TODO: create a SetErrorPageContent method on RaxMessageDataContainer

            asyncService.callAsyncLoadBalancingOperation(RaxOperation.UPDATE_ERROR_PAGE, dataContainer);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

        return Response.status(202).build();
    }


    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

}
