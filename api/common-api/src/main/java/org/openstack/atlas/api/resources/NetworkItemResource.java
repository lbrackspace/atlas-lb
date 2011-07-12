package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.entities.AccessList;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

public class NetworkItemResource extends CommonDependencyProvider {
    private Integer accountId;
    private Integer loadBalancerId;
    private int id;

    @DELETE
    public Response deleteNetworkItem() {
        try {
            LoadBalancer rLb = new LoadBalancer();
            Set<AccessList> accessLists = new HashSet<AccessList>();
            AccessList aList = new AccessList();
            aList.setId(id);
            accessLists.add(aList);
            rLb.setId(loadBalancerId);
            rLb.setAccountId(accountId);
            rLb.setAccessLists(accessLists);

            rLb = accessListService.markForDeletionNetworkItem(rLb);

            asyncService.callAsyncLoadBalancingOperation(Operation.APPEND_TO_ACCESS_LIST, rLb);


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
