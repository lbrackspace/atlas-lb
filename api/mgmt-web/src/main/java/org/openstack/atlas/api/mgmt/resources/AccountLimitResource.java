package org.openstack.atlas.api.mgmt.resources;


import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Limit;
import org.openstack.atlas.service.domain.entities.AccountLimit;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AccountLimitResource extends ManagementDependencyProvider {

    private int accountId;
    private int id;

    @DELETE
    public Response deleteAccountLimit() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            AccountLimit accountLimit = new AccountLimit();
            accountLimit.setAccountId(accountId);
            accountLimit.setId(id);

            accountLimitService.delete(accountLimit);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateAccountLimit(Limit limit) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            org.openstack.atlas.service.domain.entities.AccountLimit domainAccountLimit = getDozerMapper().map(limit, org.openstack.atlas.service.domain.entities.AccountLimit.class);
            domainAccountLimit.setAccountId(accountId);
            domainAccountLimit.setId(id);

            accountLimitService.update(domainAccountLimit);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

