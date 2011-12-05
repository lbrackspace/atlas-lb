package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.operation.RaxOperation;
import org.openstack.atlas.rax.domain.service.RaxAccessListService;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

@Primary
@Controller
@Scope("request")
public class RaxNetworkItemResource extends CommonDependencyProvider {
    private Integer accountId;
    private Integer loadBalancerId;
    private int id;

    @Autowired
    protected RaxAccessListService accessListService;

    @DELETE
    public Response deleteNetworkItem() {
        try {
            RaxLoadBalancer loadBalancer = new RaxLoadBalancer();
            loadBalancer.setAccountId(accountId);
            loadBalancer.setId(loadBalancerId);

            MessageDataContainer data = new MessageDataContainer();
            data.setLoadBalancer(loadBalancer);

            accessListService.markNetworkItemForDeletion(accountId, loadBalancerId, id);

            asyncService.callAsyncLoadBalancingOperation(RaxOperation.UPDATE_ACCESS_LIST, data);


            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setId(int id) {                                     
        this.id = id;
    }
}
