package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class VirtualIpResource extends CommonDependencyProvider {

    private Integer loadBalancerId;
    private Integer accountId;
    private Integer id;
    private HttpHeaders requestHeaders;

    @DELETE
    public Response removeVirtualIpFromLoadBalancer() {
        try {
            LoadBalancer lb = new LoadBalancer();
            lb.setId(loadBalancerId);
            lb.setAccountId(accountId);

            virtualIpService.prepareForVirtualIpDeletion(lb, id);

            List<Integer> vipIdsToDelete = new ArrayList<Integer>();
            vipIdsToDelete.add(id);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setIds(vipIdsToDelete);
            if (requestHeaders != null) dataContainer.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));

            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_VIRTUAL_IPS, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    void setId(Integer id) {
        this.id = id;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
