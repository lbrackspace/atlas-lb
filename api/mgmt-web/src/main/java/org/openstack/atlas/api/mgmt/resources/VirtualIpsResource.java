package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReports;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpBlocks;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpLoadbalancerDetails;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.TrafficType;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.POST;

public class VirtualIpsResource extends ManagementDependencyProvider {

    private VirtualIpResource virtualIpResource;
    private VirtualIpAvailabilityReports vipAvailabilityReports;
    private int id;
    private Integer accountId;

    @GET
    public Response retrieveAllVirtualIps(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps rvips = new org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps();
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            List<org.openstack.atlas.service.domain.entities.VirtualIp> vips = getVipRepository().getAll("id", offset, limit);
            for (org.openstack.atlas.service.domain.entities.VirtualIp vip : vips) {
                org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp rvip = getDozerMapper().map(vip, org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp.class);
                rvips.getVirtualIps().add(rvip);
            }
            return Response.status(200).entity(rvips).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("availabilityreport")
    @GET
    public Response retrieveAvailabilityReports() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport> dVipReports;
        VirtualIpAvailabilityReport rVipReport;
        VirtualIpAvailabilityReports rVipReports = new VirtualIpAvailabilityReports();
        dVipReports = getClusterRepository().getVirtualIpAvailabilityReport(null);
        for (org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport dr : dVipReports) {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport rr;
            rr = getDozerMapper().map(dr, org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport.class);
            rVipReports.getVirtualIpAvailabilityReports().add(rr);
        }
        return Response.status(200).entity(rVipReports).build();
    }

    @Path("{id: [1-9][0-9]*}")
    public VirtualIpResource appendVirtualIpsId(@PathParam("id") int id) {
        virtualIpResource.setId(id);
        return virtualIpResource;
    }

    @Path("detailsbyip")
    @GET
    public Response retrieveDetailsForIp(@QueryParam("ip") String ipAddress) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        VirtualIpLoadbalancerDetails rLbDetails = new VirtualIpLoadbalancerDetails();
        TrafficType rProtocol;
        List<LoadBalancer> lbsByIp;
        ArrayList<LoadBalancerProtocol> udpProtocols = new ArrayList<LoadBalancerProtocol>();
        udpProtocols.add(LoadBalancerProtocol.DNS_UDP);
        udpProtocols.add(LoadBalancerProtocol.UDP);
        udpProtocols.add(LoadBalancerProtocol.UDP_STREAM);
        try {
            lbsByIp = getVipRepository().getLoadBalancersByVipAddress(ipAddress);
            for (LoadBalancer lb : lbsByIp) {
                // Just set the top-level attributes once
                if (rLbDetails.getAccountId() == null) {
                    rLbDetails.setAccountId(lb.getAccountId());
                    rLbDetails.setLoadBalancerId(lb.getId());
                    Set<LoadBalancerJoinVip> vipSet = lb.getLoadBalancerJoinVipSet();
                    for (LoadBalancerJoinVip vip : vipSet) {
                        if (vip.getVirtualIp().getIpAddress().equals(ipAddress)) {
                            rLbDetails.setVirtualIpId(vip.getVirtualIp().getId());
                        }
                    }
                }

                if (lb.isHttpsRedirect()) {
                    // In case of HTTPS Redirect, add 80/443 since they are always static
                    rProtocol = new TrafficType();
                    rProtocol.setProtocol("TCP");
                    rProtocol.setPort(80);
                    rLbDetails.getProtocols().add(rProtocol);

                    rProtocol = new TrafficType();
                    rProtocol.setProtocol("TCP");
                    rProtocol.setPort(443);
                    rLbDetails.getProtocols().add(rProtocol);
                } else if (!lb.hasSsl()) {
                    // If no HTTPS Redirect or SSL, add the main VS config
                    rProtocol = new TrafficType();
                    LoadBalancerProtocol lbProtocol = lb.getProtocol();
                    rProtocol.setProtocol(udpProtocols.contains(lbProtocol) ? "UDP" : "TCP");
                    rProtocol.setPort(lb.getPort());
                    rLbDetails.getProtocols().add(rProtocol);
                } else {
                    // For SSL term, add HTTPS and whatever port, as long as SSL is enabled
                    if (lb.getSslTermination().isEnabled()) {
                        rProtocol = new TrafficType();
                        rProtocol.setProtocol("TCP");
                        rProtocol.setPort(lb.getSslTermination().getSecurePort());
                        rLbDetails.getProtocols().add(rProtocol);
                    }

                    // If SSL is disabled or in mixed mode, add the main VS config too
                    if (!lb.getSslTermination().isSecureTrafficOnly() || !lb.getSslTermination().isEnabled()) {
                        rProtocol = new TrafficType();
                        LoadBalancerProtocol lbProtocol = lb.getProtocol();
                        rProtocol.setProtocol(udpProtocols.contains(lbProtocol) ? "UDP" : "TCP");
                        rProtocol.setPort(lb.getPort());
                        rLbDetails.getProtocols().add(rProtocol);
                    }
                }
            }
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

        if (rLbDetails.getAccountId() == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(200).entity(rLbDetails).build();
    }

    @Path("lbsbyvipblocks")
    @POST
    public Response getLoadBalancersByVirtualIpBlocks(VirtualIpBlocks vBlocks) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        org.openstack.atlas.service.domain.pojos.VirtualIpBlocks dvBlocks;
        List<LoadBalancer> dLoadBalancers;
        LoadBalancers rLoadBalancers = new LoadBalancers();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer rLb;

        try {
            dvBlocks = getDozerMapper().map(vBlocks, org.openstack.atlas.service.domain.pojos.VirtualIpBlocks.class);
            dLoadBalancers = getVipRepository().getLbsByVirtualIp4Blocks(dvBlocks);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        for (LoadBalancer dLb : dLoadBalancers) {
            rLb = getDozerMapper().map(dLb, org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer.class);
            rLoadBalancers.getLoadBalancers().add(rLb);
        }

        return Response.status(200).entity(rLoadBalancers).build();
    }

    @GET
    @Path("freevips")
    public Response getFreeVips() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.VirtualIp> dvips;
        VirtualIps rvips = new VirtualIps();
        try {
            dvips = getVipRepository().listFreeVirtualIps();
            for (org.openstack.atlas.service.domain.entities.VirtualIp dvip : dvips) {
                rvips.getVirtualIps().add(getDozerMapper().map(dvip, VirtualIp.class));
            }

            return Response.status(200).entity(rvips).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setVirtualIpResource(VirtualIpResource virtualIpResource) {
        this.virtualIpResource = virtualIpResource;
    }

    public void setId(int id) {
        this.id = id;
    }
}
