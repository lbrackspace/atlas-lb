package org.openstack.atlas.api.mgmt.resources;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancers;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;
import org.openstack.atlas.util.debug.Debug;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountRecord;
import org.openstack.atlas.service.domain.entities.Account;

public class AccountResource extends ManagementDependencyProvider {

    private LoadBalancersResource loadBalancersResource;
    private int id;

    @Path("loadbalancers")
    public LoadBalancersResource getLoadBalancersResource() {
        loadBalancersResource.setAccountId(id);
        return loadBalancersResource;
    }

    @GET
    @Path("loadbalancers")
    public Response retrieveLoadBalancers() {
        if (!isUserInRole("cp,ops,support,billing")) {
            return ResponseFactory.accessDenied();
        }

        AccountLoadBalancer raccountLoadBalancer;
        AccountLoadBalancers raccountLoadBalancers = new AccountLoadBalancers();
        List<org.openstack.atlas.service.domain.pojos.AccountLoadBalancer> daccountLoadBalancers;
        raccountLoadBalancers.setAccountId(id);
        try {
            daccountLoadBalancers = loadBalancerService.getAccountLoadBalancers(id);
            for (org.openstack.atlas.service.domain.pojos.AccountLoadBalancer daccountLoadBalancer : daccountLoadBalancers) {
                raccountLoadBalancer = getDozerMapper().map(daccountLoadBalancer, AccountLoadBalancer.class);
                raccountLoadBalancers.getAccountLoadBalancers().add(raccountLoadBalancer);
            }
            return Response.status(200).entity(raccountLoadBalancers).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("extendedloadbalancers")
    // Implements V1-B-34873
    public Response retrieveExtendedAccountLoadBalancers() {
        if (!isUserInRole("cp,ops,support,billing")) {
            return ResponseFactory.accessDenied();
        }

        ExtendedAccountLoadbalancer raccountLoadBalancer;
        ExtendedAccountLoadbalancers raccountLoadBalancers = new ExtendedAccountLoadbalancers();
        List<ExtendedAccountLoadBalancer> daccountLoadBalancerExtendedAccountLoadBalancers;
        raccountLoadBalancers.setAccountId(id);
        try {
            daccountLoadBalancerExtendedAccountLoadBalancers = loadBalancerService.getExtendedAccountLoadBalancer(id);
            for (ExtendedAccountLoadBalancer daccountLoadBalancerExtendedAccountLoadBalancer : daccountLoadBalancerExtendedAccountLoadBalancers) {
                raccountLoadBalancer = getDozerMapper().map(daccountLoadBalancerExtendedAccountLoadBalancer, ExtendedAccountLoadbalancer.class, "SIMPLE_VIP_CPLB");
                raccountLoadBalancers.getExtendedAccountLoadbalancers().add(raccountLoadBalancer);
            }
            return Response.status(200).entity(raccountLoadBalancers).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("groups")
    public Response retrieveGroups() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<GroupRateLimit> rateLimit = null;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimits lts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimits();
        try {
            rateLimit = groupRepository.getByAccountId(id);
            for (org.openstack.atlas.service.domain.entities.GroupRateLimit domain : rateLimit) {
                lts.getGroupRateLimits().add(getDozerMapper().map(domain, org.openstack.atlas.docs.loadbalancers.api.management.v1.GroupRateLimit.class));
            }
            return Response.status(200).entity(lts).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Path("groups")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteAccountGroup() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {

            //delete all groups for account
            groupRepository.deleteAllForAccount(id);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAccountRecord() {
        Account account;
        if (!isUserInRole("ops,cp,support,billing")) {
            return ResponseFactory.accessDenied();
        }
        try {
            account = virtualIpService.getAccountRecord(id);
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        AccountRecord ar = (AccountRecord) dozerMapper.map(account, AccountRecord.class);
        return Response.status(200).entity(ar).build();
    }

    @DELETE
    public Response removeAccountRecord() {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        boolean deleted = virtualIpService.deleteAccountRecord(id);
        if (!deleted) {
            String msg = "account not deleted possibly because it wasn't in the table";
            return ResponseFactory.getValidationFaultResponse(msg);
        }
        return ResponseFactory.getSuccessResponse("deleted account " + id + " from Account table", 200);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateOrCreateAccountRecord(AccountRecord apiAccount) {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        apiAccount.setId(id); // Just incase the account on the URL doesn't match the account in the body
        Account dbAccount;
        dbAccount = (Account) dozerMapper.map(apiAccount, Account.class);
        try {
            dbAccount = virtualIpService.updateOrCreateAccountRecord(dbAccount);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AccountResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EntityNotFoundException ex) {
            return ResponseFactory.getErrorResponse(ex, "Exception:", Debug.getExtendedStackTrace(ex));
        }
        AccountRecord accountRecord = (AccountRecord) dozerMapper.map(dbAccount, AccountRecord.class);
        return Response.status(200).entity(accountRecord).build();
    }

    public void setLoadBalancersResource(LoadBalancersResource loadBalancersResource) {
        this.loadBalancersResource = loadBalancersResource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
