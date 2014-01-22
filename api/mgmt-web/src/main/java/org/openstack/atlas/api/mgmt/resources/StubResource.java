package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.helpers.StubFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.AllowedDomain;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.service.domain.events.entities.CategoryType;
import org.openstack.atlas.service.domain.events.entities.EventSeverity;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent;
import org.openstack.atlas.util.debug.Debug;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstack.atlas.api.mgmt.helpers.StubFactory.newNetInterface;

// Undocumented resource
public class StubResource extends ManagementDependencyProvider {

    @GET
    @Path("subnetmappings")
    public Response stubSubnetMappings() {
        Hostssubnet hostssubnetmaps = new Hostssubnet();
        Hostsubnet hostsubnet;
        hostsubnet = new Hostsubnet();
        hostsubnet.setName("n01.zeus.something");
        hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "192.168.0.0/16", "172.16.0.0/12"));
        hostsubnet.getNetInterfaces().add(newNetInterface("eth1", "10.0.0.0/8"));
        hostssubnetmaps.getHostsubnets().add(hostsubnet);
        hostsubnet = new Hostsubnet();
        hostsubnet.setName("n02.zeus.something");
        hostsubnet.getNetInterfaces().add(newNetInterface("eth0", "169.254.0.0/16"));
        hostsubnet.getNetInterfaces().add(newNetInterface("lo", "127.0.0.0/8"));
        hostssubnetmaps.getHostsubnets().add(hostsubnet);
        return Response.status(200).entity(hostssubnetmaps).build();
    }

    @GET
    @Path("host")
    public Response stubPublicHost() {
        Host host = new Host();
        host.setClusterId(100);
        host.setCoreDeviceId("SomeCoreDevice");
        host.setManagementIp("12.34.56.78");
        host.setManagementSoapInterface("https://SomeSoapNode.com:9090");
        host.setManagementRestInterface("https://SomeRestNode.com:9070");
        host.setId(5);
        host.setMaxConcurrentConnections(5);
        host.setName("someName");
        host.setNumberOfLoadBalancingConfigurations(300);
        host.setNumberOfUniqueCustomers(50000);
        host.setStatus(HostStatus.BURN_IN);
        host.setTrafficManagerName("zues01.blah.blah");
        host.setType(HostType.FAILOVER);
        host.setUtilization("UtilizationString");
        host.setSoapEndpointActive(Boolean.TRUE);
        host.setZone(Zone.B);
        host.setIpv4Public("127.0.0.1");
        host.setIpv6Public("::1");
        host.setIpv4Public("64.23.33.44");
        host.setIpv4Public("ffff::ffff");
        return Response.status(200).entity(host).build();
    }

    @GET
    @Path("ratelimit")
    public Response stubRateLimit() {
        Ticket ticket = new Ticket();
        ticket.setTicketId("13");
        ticket.setComment("My first comment!");

        RateLimit lt = new RateLimit();
        lt.setTicket(ticket);
        lt.setMaxRequestsPerSecond(23);
        lt.setExpirationTime(Calendar.getInstance());
        return Response.status(200).entity(lt).build();
    }

    @GET
    @Path("virtualip")
    public Response stubVirtualIp() {
        VirtualIp lt = new VirtualIp();
        lt.setAddress("12.23.23.33");
        lt.setClusterId(100);
        lt.setType(VipType.PUBLIC);
        lt.setId(5);
        lt.setLoadBalancerId(44);
        return Response.status(200).entity(lt).build();
    }

    @GET
    @Path("virtualipblocks")
    public Response getVirtualIpBlocks() {
        return Response.status(200).entity(StubFactory.getVirtualIpBlocks()).build();
    }

    @GET
    @Path("byidorname")
    public Response getByIdOrName() {
        nop();
        ByIdOrName byIdOrName = new ByIdOrName();
        byIdOrName.setId(1);
        byIdOrName.setName("SomeName");
        return Response.status(200).entity(byIdOrName).build();
    }

    @GET
    @Path("ldapinfo")
    public Response getLdapInfo() {
        nop();
        LdapInfo li = new LdapInfo();
        LdapGroup lg;
        UserRole ul;

        li.setUserName(getLDAPUser());
        for (String group : getLDAPGroups()) {
            lg = new LdapGroup();
            lg.setGroup(group);
            li.getLdapGroups().add(lg);
        }

        for (String role : userRoles()) {
            ul = new UserRole();
            ul.setRoleName(role);
            li.getUserRoles().add(ul);
        }

        return Response.status(200).entity(li).build();
    }

    @GET
    @Path("gensha1sums")
    public Response gensha1sum() {
        ListOfInts listOfInts = new ListOfInts();
        List<Integer> updatedAccounts;
        try {
            updatedAccounts = virtualIpService.genSha1SumsForAccountTable();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(StubResource.class.getName()).log(Level.SEVERE, null, ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        for (Integer accountId : updatedAccounts) {
            listOfInts.getInts().add(accountId);
        }
        return Response.status(200).entity(listOfInts).build();
    }

    @GET
    @Path("alloweddomain")
    public Response getAllowedDomain() {
        AllowedDomain ad = new AllowedDomain();
        ad.setName("somedomain.org");
        return Response.status(200).entity(ad).build();
    }

    @GET
    @Path("loadBalancerServiceEvents")
    public Response getLoadBalancerServiceEvent() {
        org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents dEvents = new org.openstack.atlas.service.domain.events.pojos.LoadBalancerServiceEvents();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents rEvents = new org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerServiceEvents();
        LoadBalancerServiceEvent lbSe;
        for (int i = 0; i < 3; i++) {
            lbSe = new LoadBalancerServiceEvent();
            lbSe.setAccountId(1000);
            lbSe.setAuthor("Some Author");
            lbSe.setCategory(CategoryType.UPDATE);
            lbSe.setCreated(Calendar.getInstance());
            lbSe.setDescription(String.format("lb %d", i));
            lbSe.setId(i);
            lbSe.setRelativeUri("SomeUri");
            lbSe.setSeverity(EventSeverity.INFO);
            lbSe.setTitle("No Title");
            lbSe.setType(EventType.BUILD_LOADBALANCER);
            try {
                throw new Exception(String.format("Exception for lb %d", i));
            } catch (Exception ex) {
                if (i % 2 == 1) {
                    lbSe.setDetailedMessage(Debug.getEST(ex));
                }
            }
            dEvents.getLoadBalancerServiceEvents().add(lbSe);
            rEvents = dozerMapper.map(dEvents, rEvents.getClass());
        }
        return Response.status(200).entity(rEvents).build();
    }
}
