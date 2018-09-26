package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Port;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Ports;
import org.openstack.atlas.service.domain.entities.VirtualIp;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class VirtualIpResource extends ManagementDependencyProvider {

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @GET
    @Path("loadbalancers")
    public Response getLoadBalancersByVipId() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        LoadBalancers rlbs = new LoadBalancers();
        List<org.openstack.atlas.service.domain.entities.LoadBalancer> dlbs;
        LoadBalancer rlb;
        try {
            dlbs = getVipRepository().getLoadBalancersByVipId(id);
            for (org.openstack.atlas.service.domain.entities.LoadBalancer dlb : dlbs) {
                rlb = getDozerMapper().map(dlb, LoadBalancer.class);
                rlbs.getLoadBalancers().add(rlb);
            }
            return Response.status(200).entity(rlbs).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

    }

    @GET
    @Path("loadbalancerports")
    public Response getLoadBalancerPorts() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        Ports rPorts = new Ports();
        Port rPort;
        Map<Integer, List<org.openstack.atlas.service.domain.entities.LoadBalancer>> portMap;
        List<org.openstack.atlas.service.domain.entities.LoadBalancer> lbList;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer rLb;
        try {
            portMap = getVipRepository().getPorts(id);
            for (Integer key : portMap.keySet()) {
                lbList = portMap.get(key);
                rPort = new Port();
                rPort.setValue(key);
                for (org.openstack.atlas.service.domain.entities.LoadBalancer dLb : lbList) {
                    rLb = getDozerMapper().map(dLb, LoadBalancer.class);
                    rPort.getLoadBalancers().add(rLb);
                }
                rPorts.getPorts().add(rPort);
            }
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
        return Response.status(200).entity(rPorts).build();
    }

    @DELETE
    public Response deleteVirtualIp() {
        try {
            if (!isUserInRole("cp,ops")) {
                return ResponseFactory.accessDenied();
            }

            VirtualIp domainVip = new VirtualIp();
            domainVip.setId(id);
            virtualIpService.removeVipFromCluster(domainVip);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
