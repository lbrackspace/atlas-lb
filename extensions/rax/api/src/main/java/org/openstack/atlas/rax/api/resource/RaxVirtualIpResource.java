package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.rax.domain.operation.RaxOperation;
import org.openstack.atlas.rax.domain.service.RaxVirtualIpService;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Primary
@Controller
@Scope("request")
public class RaxVirtualIpResource extends CommonDependencyProvider {
    private Integer loadBalancerId;
    private Integer accountId;
    private Integer id;

    @Autowired
    protected RaxVirtualIpService virtualIpService;

    @DELETE
    @Path("/ext/RAX-ATLAS-DV")
    public Response removeVirtualIpFromLoadBalancer() {
        try {
            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);

            virtualIpService.prepareForVirtualIpDeletion(lb, id);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.getIds().add(id);
//            if (requestHeaders != null) dataContainer.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            asyncService.callAsyncLoadBalancingOperation(RaxOperation.RAX_REMOVE_VIRTUAL_IPS, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setId(Integer virtualIpId) {
        this.id = virtualIpId;
    }
}
