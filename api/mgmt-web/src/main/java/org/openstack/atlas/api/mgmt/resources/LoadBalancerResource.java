package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountLoadBalancerServiceEvents;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Host;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.mapper.UsageMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoadBalancerResource extends ManagementDependencyProvider {

    private LoadBalancerSuspensionResource loadBalancerSuspensionResource;
    private RateLimitResource rateLimitResource;
    private LoadbalancerVipResource loadbalancerVipResource;
    private HostsResource hostsResource;
    private SyncResource syncResource;
    private TicketsResource ticketsResource;
    private int id;
    private ErrorpageResource errorPageResource;

    @GET
    @Path("host")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHost() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            Host rlb = new Host();
            org.openstack.atlas.service.domain.entities.Host dlb = hostService.getHostsByLoadBalancerId(id);
            if (dlb != null) {
                rlb = getDozerMapper().map(dlb, Host.class);
            }
            return Response.status(200).entity(rlb).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Path("suspended")
    public Response deleteSuspendedLoadBalancer() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        try {
            getLoadBalancerRepository().getById(id); // Throw up an exception if this doesn't exist.
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            domainLb.setId(id);

            esbService.callAsyncLoadBalancingOperation(Operation.DELETE_LOADBALANCER, loadBalancerService.prepareMgmtLoadBalancerDeletion(domainLb, LoadBalancerStatus.SUSPENDED));
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    @Path("error")
    public Response deleteErroredLoadBalancer() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        try {
            loadBalancerService.get(id); // Throw up an exception if this doesn't exist.
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            domainLb.setId(id);

            esbService.callAsyncLoadBalancingOperation(Operation.DELETE_LOADBALANCER, loadBalancerService.prepareMgmtLoadBalancerDeletion(domainLb, LoadBalancerStatus.ERROR));
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Path("/setstatuspendingupdate")
    public Response updateLbStatusPending(@PathParam("status") String statusStr) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        String format;
        String msg;
        Response resp;
        LoadBalancerStatus status;
        boolean isStatusChanged;
        Integer accountId;

        status = LoadBalancerStatus.PENDING_UPDATE;

        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer dLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dLb = loadBalancerService.get(id);
            accountId = dLb.getAccountId();
            isStatusChanged = loadBalancerService.testAndSetStatusPending(accountId, id);
            if (isStatusChanged) {
                msg = "status was set to pending";
                resp = ResponseFactory.getSuccessResponse(msg, 200);

            } else {
                msg = "Status was already pending";
                resp = ResponseFactory.getSuccessResponse(msg, 200);
            }
            return resp;
        } catch (Exception ex) {
            resp = ResponseFactory.getErrorResponse(ex, null, null);
            return resp;
        }
    }

    @PUT
    @Path("/setstatus/{status}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateLbStatus(@PathParam("status") String statusStr) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        String format;
        String msg;
        Response resp;
        LoadBalancerStatus status;

        try {
            status = LoadBalancerStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            BadRequestException e = new BadRequestException();
            format = "Invalid LoadBalancer Status '%s'";
            msg = String.format(format, statusStr);
            return ResponseFactory.getErrorResponse(e, msg, null);
        }
        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer dLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            loadBalancerService.get(id);
            dLb.setId(id);
            dLb.setStatus(status);
            loadBalancerService.setLoadBalancerAttrs(dLb);
            msg = String.format("Status is now %s", status);
            resp = ResponseFactory.getSuccessResponse(msg, 200);
            return resp;
        } catch (Exception ex) {
            resp = ResponseFactory.getErrorResponse(ex, null, null);
            return resp;
        }


    }

    @Path("sync")
    public SyncResource retrieveSyncResource() {
        syncResource.setLoadBalancerId(id);
        return syncResource;
    }

    @Path("suspension")
    public LoadBalancerSuspensionResource getLoadBalancerSuspensionResource() {
        loadBalancerSuspensionResource.setLoadBalancerId(id);
        return loadBalancerSuspensionResource;
    }

    @Path("sticky")
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateSticky() {
        // Undocumented Url assuming its just for ops
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer dLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dLb.setId(id);
            hostService.updateLoadBalancerSticky(dLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("sticky")
    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response makeUnsticky() {
        //  Undocumented Url assuming its just for ops
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer dLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();
            dLb.setId(id);
            hostService.deleteLoadBalancerSticky(dLb);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("ratelimit")
    public RateLimitResource getRateLimitResource() {
        rateLimitResource.setLoadbalancerId(id);
        return rateLimitResource;
    }

    @Path("virtualips")
    public LoadbalancerVipResource retrieveLoadBalancerVirtualIpsResource() {
        loadbalancerVipResource.setLoadBalancerId(id);
        return loadbalancerVipResource;
    }

    @Path("tickets")
    public TicketsResource retrieveTicketsResource() {
        ticketsResource.setLoadBalancerId(id);
        return ticketsResource;
    }

    @GET
    @Path("extendedview")
    public Response getExtendedView(@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        org.openstack.atlas.service.domain.events.pojos.AccountLoadBalancerServiceEvents dEvents;
        AccountLoadBalancerServiceEvents rEvents = new AccountLoadBalancerServiceEvents();
        try {
            org.openstack.atlas.service.domain.entities.LoadBalancer dlb = getLoadBalancerRepository().getById(id);
            LoadBalancer rlb = getDozerMapper().map(dlb, LoadBalancer.class);
            // Attach tickets
            if (dlb.getTickets() != null && !dlb.getTickets().isEmpty()) {
                rlb.setTickets(new ArrayList<org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket>());
                for (Ticket ticket : dlb.getTickets()) {
                    rlb.getTickets().add(dozerMapper.map(ticket, org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket.class));
                }
            }
            // Attach usage for last 24 hours
            Calendar now = Calendar.getInstance();
            Calendar nowYesterday = (Calendar) now.clone();
            nowYesterday.add(Calendar.DAY_OF_MONTH, -1);
            List<Usage> usage = getLoadBalancerRepository().getUsageByLbId(id, nowYesterday, now);
            if (usage != null && !usage.isEmpty()) {
                rlb.setLoadBalancerUsage(UsageMapper.toRestApiCurrentUsage(usage));
            }

            //Attach events for the past 24 hours
            dEvents = getEventRepository().getRecentLoadBalancerServiceEventsByLbId(dlb, startDate, endDate);

            rEvents = getDozerMapper().map(dEvents, rEvents.getClass());
            rlb.setAccountLoadBalancerServiceEvents(rEvents);

            if (usage != null && !usage.isEmpty()) {
                rlb.setLoadBalancerUsage(UsageMapper.toRestApiCurrentUsage(usage));
            }

            return Response.status(200).entity(rlb).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setSyncResource(SyncResource syncResource) {
        this.syncResource = syncResource;
    }

    public void setLoadBalancerSuspensionResource(LoadBalancerSuspensionResource loadBalancerSuspensionResource) {
        this.loadBalancerSuspensionResource = loadBalancerSuspensionResource;
    }

    public void setRateLimitResource(RateLimitResource rateLimitResource) {
        this.rateLimitResource = rateLimitResource;
    }

    public void setLoadbalancerVipResource(LoadbalancerVipResource loadbalancerVipResource) {
        this.loadbalancerVipResource = loadbalancerVipResource;
    }

    public void setHostsResource(HostsResource hostsResource) {
        this.hostsResource = hostsResource;
    }

    public void setTicketsResource(TicketsResource ticketsResource) {
        this.ticketsResource = ticketsResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setErrorPageResource(ErrorpageResource errorPageResource) {
        this.errorPageResource = errorPageResource;
    }

    public ErrorpageResource getErrorPageResource() {
        return errorPageResource;
    }
}
