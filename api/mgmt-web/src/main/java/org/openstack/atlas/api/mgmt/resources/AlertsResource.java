package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.util.Constants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertsResource extends ManagementDependencyProvider {

    private AlertResource alertResource;

    @Path("{id: [1-9][0-9]*}")
    public AlertResource retrieveAlertResource(@PathParam("id") int id) {
        alertResource.setId(id);
        return alertResource;
    }

    @GET
    public Response retrieveAll(@QueryParam("status") String status, @QueryParam("marker") Integer marker, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<Alert> domainCls;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts dataModelCls = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts();
        try {
            domainCls = getAlertRepository().getAll(status, marker, limit);
            for (Alert domainCl : domainCls) {
                dataModelCls.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
            }

            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @GET
    @Path("byloadbalancerids")
    public Response retrieveByLoadBalancerids(@QueryParam("id") List<Integer> ids,
                                              @QueryParam("startDate") String startDate,
                                              @QueryParam("endDate") String endDate) {
        Alerts rAlerts = new Alerts();
        List<Alert> alerts;
        try {
            alerts = alertService.getByLoadBalancerIds(ids, startDate, endDate);
        } catch (BadRequestException ex) {
            Logger.getLogger(AlertsResource.class.getName()).log(Level.SEVERE, null, ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        for (Alert dAlert : alerts) {
            rAlerts.getAlerts().add(getDozerMapper().map(dAlert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
        }
        return Response.status(200).entity(rAlerts).build();
    }

    // TODO: document
    // Working on this method for multiple accounts support
    @GET
    @Path("account")
    public Response retrieveByAccountIds(@QueryParam("marker") Integer marker, @QueryParam("limit") Integer limit, @QueryParam("account") List<Integer> accounts, @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @QueryParam("showStackTrace") boolean showStackTrace) {

        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<Alert> domainCls;
        Alerts apiModelCls = new Alerts();
        try {
            for (int account : accounts) {
                domainCls = alertService.getByAccountId(marker, limit, account, startDate, endDate);

                for (Alert dAlert : domainCls) {
                    if (showStackTrace) {
                        apiModelCls.getAlerts().add(getDozerMapper().map(dAlert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class));
                    } else {
                        apiModelCls.getAlerts().add(getDozerMapper().map(dAlert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
                    }
                }
            }
            return Response.status(200).entity(apiModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("account/{aid: [1-9][0-9]*}")
    public Response retrieveByAccountId_(@PathParam("aid") int accountId,
                                         @QueryParam("marker") Integer marker, @QueryParam("limit") Integer limit,
                                         @QueryParam("startDate") String startDate,
                                         @QueryParam("endDate") String endDate) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<Alert> domainCls;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts dataModelCls = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts();
        try {

            domainCls = alertService.getByAccountId(accountId, startDate, endDate);
            for (Alert domainCl : domainCls) {
                dataModelCls.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
            }
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @GET
    @Path("cluster/{cid: [1-9][0-9]*}")
    public Response retrieveByClusterId(@PathParam("cid") int cid,
                                        @QueryParam("startDate") String startDate,
                                        @QueryParam("endDate") String endDate) {
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts rAlerts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts();
        List<Alert> alerts;
        try {
            alerts = alertService.getByClusterId(cid, startDate, endDate);
        } catch (BadRequestException ex) {
            Logger.getLogger(AlertsResource.class.getName()).log(Level.SEVERE, null, ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        for (Alert dAlert : alerts) {
            rAlerts.getAlerts().add(getDozerMapper().map(dAlert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
        }
        return Response.status(200).entity(rAlerts).build();
    }


    @GET
    @Path("loadbalancer")
    public Response retrieveByLoadBalancerId(@QueryParam("id") Integer id, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        List<Alert> domainCls;
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        Alerts dataModelCls = new Alerts();
        try {

            domainCls = alertService.getByLoadBalancerId(id);

            for (Alert domainCl : domainCls) {
                dataModelCls.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
            }
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @GET
    @Path("unacknowledged")
    public Response retrieveAllUnacknowledged(@QueryParam("marker") Integer marker, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<Alert> domainCls;
        Alerts dataModelCls = new Alerts();
        try {
            domainCls = alertService.getAllUnacknowledged(marker, limit);

            for (Alert domainCl : domainCls) {
                dataModelCls.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
            }
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("atomhopper")
    public Response retrieveAllByAtomHopperUnacknowledged(@QueryParam("marker") Integer marker,
                                                          @QueryParam("messageName") String messageName,
                                                          @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<Alert> domainCls;
        Alerts dataModelCls = new Alerts();
        try {
            domainCls = alertService.getAllAtomHopperUnacknowledged(Constants.AH_USAGE_EVENT_FAILURE, messageName, marker, limit);

            for (Alert domainCl : domainCls) {
                dataModelCls.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
            }
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setAlertResource(AlertResource alertResource) {
        this.alertResource = alertResource;
    }

    @GET
    @Path("atomhopper/byloadbalancerids")
    public Response retrieveByLoadBalanceridsForAtomHopper(@QueryParam("id") List<Integer> ids,
                                                           @QueryParam("messageName") String messageName,
                                                           @QueryParam("startDate") String startDate,
                                                           @QueryParam("endDate") String endDate) {
        Alerts rAlerts = new Alerts();
        List<Alert> alerts;
        try {
            alerts = alertService.getAtomHopperByLoadBalancersByIds(ids, startDate, endDate, Constants.AH_USAGE_EVENT_FAILURE);
        } catch (BadRequestException ex) {
            Logger.getLogger(AlertsResource.class.getName()).log(Level.SEVERE, null, ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        for (Alert dAlert : alerts) {
            rAlerts.getAlerts().add(getDozerMapper().map(dAlert, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
        }
        return Response.status(200).entity(rAlerts).build();
    }
}
