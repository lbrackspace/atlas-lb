package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdHostId;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.helpers.StubFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.mgmt.validation.contexts.HostContext;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class
        HostResource extends ManagementDependencyProvider {

    private BackupsResource backupsResource;
    private int id;

    @GET // Jira:https://jira.mosso.com/browse/SITESLB-232
    @Path("customercount")
    public Response getCustomersCounts() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<LoadBalancerCountByAccountIdHostId> daccountsInHost;
        AccountsInHost accountsInHost = new AccountsInHost();
        AccountInHost accountInHost;
        try {
            daccountsInHost = getHostRepository().getAccountsInHost(id);
            for (LoadBalancerCountByAccountIdHostId daccountInCluster : daccountsInHost) {
                accountInHost = getDozerMapper().map(daccountInCluster, AccountInHost.class);
                accountsInHost.getAccountInHosts().add(accountInHost);
            }
            accountsInHost.setTotalAccounts(daccountsInHost.size());
            return Response.status(200).entity(accountsInHost).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }


    @GET
    public Response getHost() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.pojos.HostMachineDetails hD = new org.openstack.atlas.service.domain.pojos.HostMachineDetails();
            org.openstack.atlas.service.domain.entities.Host host = hostRepository.getById(id);
            Integer totalConnection = getTotalConcurrentConnections(id);
            double calc = totalConnection / host.getMaxConcurrentConnections();
            double calcMath = calc * 100;
            String calcString = calcMath + "%";

            hD.setHost(host);
            hD.setUniqueCustomers(hostRepository.getNumberOfUniqueAccountsForHost(host.getId()));
            hD.setActiveLBConfigurations(hostRepository.getActiveLoadBalancerForHost(host.getId()));
            hD.setTotalConcurrentConnections(totalConnection);
            hD.setAvailableConcurrentConnections(host.getMaxConcurrentConnections() - totalConnection);
            hD.setCurrentUtilization(calcString);

            if (host.getHostStatus().name().equals(HostStatus.ACTIVE.name()) | host.getHostStatus().name().equals((HostStatus.ACTIVE_TARGET.name()))) {
                hD.setCurrentUtilization(calcString);
            } else {
                hD.setCurrentUtilization("0 %");
            }
            HostMachineDetails rHostMD = getDozerMapper().map(hD, HostMachineDetails.class);        

            return Response.status(200).entity(rHostMD).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("activate")
    @PUT
    public Response activateHost() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = new org.openstack.atlas.service.domain.entities.Host();
            domainHost.setId(id);
            hostService.activateHost(domainHost);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("deactivate")
    @PUT
    public Response inactivateHost() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = new org.openstack.atlas.service.domain.entities.Host();
            domainHost.setId(id);
            hostService.inActivateHost(domainHost);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("subnetmappings")
    public Response retrieveHostsSubnetMappings() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        org.openstack.atlas.service.domain.entities.Host domainHost = new org.openstack.atlas.service.domain.entities.Host();
        Hostssubnet rHostssubnets = new Hostssubnet();
        Hostsubnet rHostsubnet = new Hostsubnet();
        org.openstack.atlas.service.domain.pojos.Hostssubnet dHostssubnet;

        domainHost.setId(id);

        try {
            domainHost = hostService.getById(id);
            dHostssubnet = reverseProxyLoadBalancerService.getSubnetMappings(domainHost);

            if (dHostssubnet != null) {
                for (org.openstack.atlas.service.domain.pojos.Hostsubnet hsub : dHostssubnet.getHostsubnets()) {
                    rHostsubnet = (getDozerMapper().map(hsub, Hostsubnet.class));
                    rHostssubnets.getHostsubnets().add(rHostsubnet);
                }
                return Response.status(200).entity(rHostssubnets).build();
            }
            BadRequest badRequest = new BadRequest();
            badRequest.setCode(400);
            badRequest.setMessage("Could not find subnets");

            return Response.status(200).entity(badRequest).build();


        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, ex.getMessage(), null);
        }
    }

    @PUT
    @Path("subnetmappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putHostsSubnetMappings(Hostssubnet rHostssubnet) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        EsbRequest req = new EsbRequest();
        OperationResponse resp;
        org.openstack.atlas.service.domain.entities.Host dHost = new org.openstack.atlas.service.domain.entities.Host();
        org.openstack.atlas.service.domain.pojos.Hostssubnet dHostssubnet;
        if (rHostssubnet.getHostsubnets().size() != 1) {
            ValidationErrors vFault = new ValidationErrors();
            BadRequest badRequest = new BadRequest();
            badRequest.setCode(400);
            badRequest.setMessage("Invalid request");
            vFault.getMessages().add("Please specify only one host per request");
            return Response.status(200).entity(badRequest).build();
        }
        dHostssubnet = getDozerMapper().map(rHostssubnet, org.openstack.atlas.service.domain.pojos.Hostssubnet.class);
        dHost.setId(id);
        req.setHost(dHost);
        req.setHostssubnet(dHostssubnet);

        try {
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.SET_HOST_SUBNET_MAPPINGS, req);
            return ResponseFactory.getSuccessResponse("Successfully put subnetmappings", 200);

        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @DELETE
    @Path("subnetmappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response delHostsSubnetMappings(Hostssubnet rHostssubnet) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        EsbRequest req = new EsbRequest();
        OperationResponse resp;
        org.openstack.atlas.service.domain.entities.Host dHost = new org.openstack.atlas.service.domain.entities.Host();
        org.openstack.atlas.service.domain.pojos.Hostssubnet dHostssubnet;
        if (rHostssubnet.getHostsubnets().size() != 1) {
            ValidationErrors vFault = new ValidationErrors();
            BadRequest badRequest = new BadRequest();
            badRequest.setCode(400);
            badRequest.setMessage("Invalid request");
            vFault.getMessages().add("Please specify only one host per request");
            return Response.status(200).entity(badRequest).build();
        }
        dHostssubnet = getDozerMapper().map(rHostssubnet, org.openstack.atlas.service.domain.pojos.Hostssubnet.class);
        dHost.setId(id);
        req.setHost(dHost);
        req.setHostssubnet(dHostssubnet);

        try {
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.DELETE_HOST_SUBNET_MAPPINGS, req);
            return Response.status(200).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @GET
    @Path("detail")
    public Response retrieveHosts(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        // Undocumented url endpoint assuming only ops
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.Host> domainHosts;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts dataModelHosts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts();
        try {
            domainHosts = getHostRepository().getAll(offset, limit);
            for (org.openstack.atlas.service.domain.entities.Host domainHost : domainHosts) {
                domainHost.getCluster().setVirtualIps(null); // Don't feel like sifting through a bunch of VIPS
                dataModelHosts.getHosts().add(getDozerMapper().map(domainHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class));
            }
            return Response.status(200).entity(dataModelHosts).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("endpoint/enable")
    public Response enableEndPoint() {
        if (!isUserInRole("cp,op")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.Host dHost = new org.openstack.atlas.service.domain.entities.Host();
            dHost.setEndpointActive(Boolean.TRUE);
            dHost.setId(id);
            hostService.updateHost(dHost);
            return ResponseFactory.getSuccessResponse("EndPoint Enabled", 200);

        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("endpoint/disable")
    public Response disableEndPoint() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.Host dHost = new org.openstack.atlas.service.domain.entities.Host();
            dHost.setEndpointActive(Boolean.FALSE);
            dHost.setId(id);
            hostService.updateHost(dHost);
            return ResponseFactory.getSuccessResponse("EndPoint Disabled", 200);

        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateHost(Host host) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(Host.class).validate(host, HostContext.PUT);
        expandIpv6InHost(host);

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = getDozerMapper().map(host, org.openstack.atlas.service.domain.entities.Host.class);
            domainHost.setId(id);
            hostService.updateHost(domainHost);
            return ResponseFactory.getSuccessResponse("PUT Operation Succeeded", 200);

        } catch (Exception e) {
            String msg = getExtendedStackTrace(e);
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteHost() {
        try {
            if (!isUserInRole("cp,ops")) {
                return ResponseFactory.accessDenied();
            }
            getHostRepository().getById(id); // Throw up an exception if this doesn't exist.
            org.openstack.atlas.service.domain.entities.Host domainHost = new org.openstack.atlas.service.domain.entities.Host();
            domainHost.setId(id);
            hostService.delete(domainHost);

            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("backups")
    public BackupsResource getBackupsResource() {
        backupsResource.setHostId(id);
        return backupsResource;
    }

    //TODO: We still need to get this clarified
    @Path("capacityreport")
    @GET
    public Response getHostCapacityReports() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        HostCapacityReports hostCapacityReports = StubFactory.rndHostCapacityReports(5);
        for (HostCapacityReport hr : hostCapacityReports.getHostCapacityReports()) {
            hr.setHostId(id);
            hr.setHostName(String.format("Host.%d", id));
        }
        return Response.status(200).entity(hostCapacityReports).build(); // WARNING bogus Data
    }

    public void setBackupsResource(BackupsResource backupsResource) {
        this.backupsResource = backupsResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Integer getTotalConcurrentConnections(Integer hostId) {
        int connection = 0;

        List<org.openstack.atlas.service.domain.entities.Host> hosts = new ArrayList();
        org.openstack.atlas.service.domain.entities.Host host = new org.openstack.atlas.service.domain.entities.Host();
        host.setId(hostId);

        try {
            hostService.getById(id);
            connection = reverseProxyLoadBalancerService.getTotalCurrentConnectionsForHost(host);

        } catch (Exception e) {
            //log
        }


        return connection;
    }
}
