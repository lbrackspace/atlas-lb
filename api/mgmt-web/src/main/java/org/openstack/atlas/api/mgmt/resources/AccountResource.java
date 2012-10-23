package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ExtendedAccountLoadbalancers;
import org.openstack.atlas.service.domain.entities.GroupRateLimit;
import org.openstack.atlas.service.domain.pojos.ExtendedAccountLoadBalancer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
    // Implements Jira: SITESLB-220
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
        try {

            //delete all groups for account
            groupRepository.deleteAllForAccount(id);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
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
