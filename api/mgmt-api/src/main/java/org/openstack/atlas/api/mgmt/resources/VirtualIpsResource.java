package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.lb.helpers.ipstring.IPv4Range;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPCidrBlockOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.VirtualIpService;
import org.openstack.atlas.service.domain.services.impl.VirtualIpServiceImpl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public class VirtualIpsResource extends ManagementDependencyProvider {

    private VirtualIpResource virtualIpResource;
    private ClusterResource clusterResource;
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
        if (!isUserInRole("cp,ops,support")) {
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
        VirtualIpLoadBalancerDetails rLbDetails;

        try {
            rLbDetails = virtualIpService.getLoadBalancerDetailsForIp(ipAddress);
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

        return Response.status(Response.Status.OK).entity(rLbDetails).build();
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

    @Path("migrate")
    @PUT
    public Response updateClusterForVipBlock(@QueryParam("oldClusterId") Integer oldClusterId, @QueryParam("newClusterId") Integer newClusterId, Cidr cidr) throws IPOctetOutOfRangeException, IPStringConversionException, IPCidrBlockOutOfRangeException {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        boolean isInRange = false;
        List<org.openstack.atlas.service.domain.entities.VirtualIp> oldInRangeClusterIps = new ArrayList<>();
        IPv4Range iPv4Range = IPv4ToolSet.ipv4BlockToRange(cidr.getBlock());
        Long loIp = iPv4Range.getLo();
        Long hiIp = iPv4Range.getHi();
        List<org.openstack.atlas.service.domain.entities.VirtualIp> vipsToBeUpdated = new ArrayList<>();
        try {
            List<org.openstack.atlas.service.domain.entities.VirtualIp> oldClusterIps = virtualIpService.getVipsByClusterId(oldClusterId);
            org.openstack.atlas.service.domain.entities.Cluster updatedCluster = clusterService.get(newClusterId);
            for(org.openstack.atlas.service.domain.entities.VirtualIp virtualIp : oldClusterIps) {

                if(virtualIp.getIpAddress().equals(loIp.toString()) || isInRange){
                    isInRange = true;
                    oldInRangeClusterIps.add(virtualIp);
                } else if (virtualIp.getIpAddress().equals(hiIp.toString())) {
                    isInRange = false;
                    break;
                 }
            }
            if(oldInRangeClusterIps.isEmpty()){
                throw new EntityNotFoundException("cannot find VIPS in that range for cluster " + oldClusterId);
            }
                for(org.openstack.atlas.service.domain.entities.VirtualIp inRangeIp : oldInRangeClusterIps) {
                    if(!inRangeIp.isAllocated()){
                        inRangeIp.setCluster(updatedCluster);
                        vipsToBeUpdated.add(inRangeIp);
                    }
                }
                for(org.openstack.atlas.service.domain.entities.VirtualIp vipToBeUpdated : vipsToBeUpdated) {
                    virtualIpService.updateClusterForVirtualIp(vipToBeUpdated);
                }
            return Response.status(200).entity(cidr).build();

        } catch (Exception e) {

            return ResponseFactory.getErrorResponse(e, null, null);

        }






    }



    public void setId(int id) {
        this.id = id;
    }
}
