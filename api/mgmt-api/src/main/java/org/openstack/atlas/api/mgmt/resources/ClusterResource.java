package org.openstack.atlas.api.mgmt.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.adapter.exceptions.VTMRollBackException;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.mapper.dozer.DomainToRestModel;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.BadRequest;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPOctetOutOfRangeException;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException;
import org.openstack.atlas.service.domain.entities.AccountLimit;
import org.openstack.atlas.service.domain.entities.VirtualIpType;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.ClusterNotEmptyException;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.Hostssubnet;
import org.openstack.atlas.service.domain.pojos.Hostsubnet;
import org.openstack.atlas.service.domain.pojos.LoadBalancerCountByAccountIdClusterId;
import org.openstack.atlas.service.domain.pojos.NetInterface;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.util.crypto.CryptoUtil;
import org.openstack.atlas.util.ip.IPUtils;
import org.openstack.atlas.util.ip.IPv4Cidr;
import org.openstack.atlas.util.ip.IPv4Cidrs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class ClusterResource extends ManagementDependencyProvider {

    private final Log LOG = LogFactory.getLog(ClusterResource.class);
    private VirtualIpsResource virtualIpsResource;
    private ErrorpageResource errorpageResource;
    private int id;

    @GET // Jira:https://jira.mosso.com/browse/SITESLB-231
    @Path("customercount")
    public Response getCustomersCounts() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<LoadBalancerCountByAccountIdClusterId> daccountsInCluster;
        AccountsInCluster accountsInCluster = new AccountsInCluster();
        AccountInCluster accountInCluster;
        try {
            daccountsInCluster = clusterService.getAccountsInCluster(id);
            for (LoadBalancerCountByAccountIdClusterId daccountInCluster : daccountsInCluster) {
                accountInCluster = getDozerMapper().map(daccountInCluster, AccountInCluster.class);
                accountsInCluster.getAccountInClusters().add(accountInCluster);
            }
            accountsInCluster.setTotalAccounts(daccountsInCluster.size());
            return Response.status(200).entity(accountsInCluster).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("errorpage")
    public ErrorpageResource retrieveErrorPageResource() {
        errorpageResource.setClusterId(id);
        return errorpageResource;
    }

    @GET
    @Path("hosts")
    public Response getClusterHosts() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        Hosts rHosts = new Hosts();
        List<org.openstack.atlas.service.domain.entities.Host> dHosts;
        try {
            dHosts = clusterService.getHosts(id);
            for (org.openstack.atlas.service.domain.entities.Host dHost : dHosts) {
                rHosts.getHosts().add(getDozerMapper().map(dHost, Host.class));
            }
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

        return Response.status(200).entity(rHosts).build();
    }

    @GET
    @Path("hostssubnets")
    public Response getClusterHostsSubnets() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        List<org.openstack.atlas.service.domain.entities.Host> dHosts;

        try {
            dHosts = clusterService.getHosts(id);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

        org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet rHostssubnets = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet rHostsubnet;
        org.openstack.atlas.service.domain.pojos.Hostssubnet dHostssubnet = new Hostssubnet();

        try {
            for (org.openstack.atlas.service.domain.entities.Host h : dHosts) {
                try {
                    dHostssubnet.getHostsubnets().addAll(reverseProxyLoadBalancerVTMService.getSubnetMappings(h).getHostsubnets());
                } catch (VTMRollBackException srex) {
                    // Unable to Collect host data, most likely because host is down or non-existent in backend...ignore
                }
            }
            if (dHostssubnet.getHostsubnets().size() > 0) {
                for (org.openstack.atlas.service.domain.pojos.Hostsubnet hsub : dHostssubnet.getHostsubnets()) {
                    rHostsubnet = (getDozerMapper().map(hsub, org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostsubnet.class));
                    rHostssubnets.getHostsubnets().add(rHostsubnet);
                }
                return Response.status(200).entity(rHostssubnets).build();
            }

            BadRequest badRequest = new BadRequest();
            badRequest.setCode(400);
            badRequest.setMessage("Could not find any host networks");
            return Response.status(400).entity(badRequest).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, ex.getMessage(), null);
        }
    }

    @GET
    @Path("virtualips")
    public Response getVirtualIpsDetails(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        VirtualIps rVips = new VirtualIps();
        List<org.openstack.atlas.service.domain.entities.VirtualIp> dVips;
        try {
            dVips = clusterService.getVirtualIps(id, offset, limit);
            for (org.openstack.atlas.service.domain.entities.VirtualIp dVip : dVips) {
                rVips.getVirtualIps().add(getDozerMapper().map(dVip, VirtualIp.class));
            }
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(rVips).build();
    }

    @POST
    @Path("virtualipblocks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addVirtualIpBlocks(VirtualIpBlocks vBlocks) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        if(vBlocks == null){
            String error = "VirtualIpBlocks must not be null";
            return getValidationFaultResponse(error);
        }
        try {
            IPv4Cidrs ipv4Cidrs = getIpv4SubnetCidrs(id);
            List<String> errors = getIpsNotContainedInASubnet(vBlocks, ipv4Cidrs);
            if(!errors.isEmpty()){
                return getValidationFaultResponse(errors);
            }
            clusterService.addVirtualIpBlocks(getDozerMapper().map(vBlocks, org.openstack.atlas.service.domain.pojos.VirtualIpBlocks.class), id);
            return Response.status(200).entity(vBlocks).build();
        } catch (Exception ex) {
            LOG.error(ex);
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @GET
    @Path("activeratelimits")
    public Response getActiveRateLimitsInCluster() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        Integer clusterId = id;
        List<ZeusRateLimitedLoadBalancer> zeusRateLimitedLoadBalancerList;
        ZeusRateLimitedLoadBalancers zeusRateLimitedLoadBalancers = new ZeusRateLimitedLoadBalancers();
        try {
            zeusRateLimitedLoadBalancerList = clusterService.getRateLimitedLoadBalancersInCluster(clusterId);
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
        zeusRateLimitedLoadBalancers.getZeusRateLimitedLoadBalancers().addAll(zeusRateLimitedLoadBalancerList);
        return Response.status(200).entity(zeusRateLimitedLoadBalancers).build();
    }

    @GET
    @Path("apiratelimit")
    public Response getApiRateLimitsForCluster() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.AccountGroup> domainAccountGroups;
        AccountGroups acGroups = new AccountGroups();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountGroup accountGroup;
        try {
            domainAccountGroups = clusterService.getAPIRateLimitedAccounts(id);
            for (org.openstack.atlas.service.domain.entities.AccountGroup lbg : domainAccountGroups) {
                accountGroup = dozerMapper.map(lbg, org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountGroup.class);
                acGroups.getAccountGroups().add(accountGroup);
            }
            return Response.status(200).entity(acGroups).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("customlimitaccounts")
    public Response getAbsoluteRateLimitsInCluster() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        Map<Integer, List<AccountLimit>> clusterLimitAccounts = accountLimitService.getAccountLimitsForCluster(id);
        return Response.status(200).entity(DomainToRestModel.customLimitAccountsInClusterMapToCustomLimitAccounts(clusterLimitAccounts)).build();
    }

    @GET
    @Path("endpoint")
    public Response getClusterEndPointHost() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        Integer clusterId = id;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Host rHost;
        org.openstack.atlas.service.domain.entities.Host dHost;

        try {
            dHost = hostService.getRestEndPointHost(clusterId);

            // TODO: remove following check when we've fully migrated off soap...
            if (dHost == null) {
                // try to find soap endpoint host in case we've yet to migrate off it
                dHost = hostService.getEndPointHost(clusterId);
            }

            // unable to find endpoint host
            if (dHost == null) {
                return Response.status(404).build();
            }

            // map the endpoint host we found
            rHost = getDozerMapper().map(dHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
        return Response.status(200).entity(rHost).build();
    }

    @GET
    @Path("virtualips/availabilityreport")
    public Response retrieveCapacityReports() {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport> dVipReports;
        VirtualIpAvailabilityReport rVipReport;
        VirtualIpAvailabilityReports rVipReports = new VirtualIpAvailabilityReports();
        dVipReports = clusterService.getVirtualIpAvailabilityReport(id);
        for (org.openstack.atlas.service.domain.pojos.VirtualIpAvailabilityReport dr : dVipReports) {
            org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport rr;
            rr = getDozerMapper().map(dr, org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIpAvailabilityReport.class);
            rVipReports.getVirtualIpAvailabilityReports().add(rr);
        }
        return Response.status(200).entity(rVipReports).build();
    }

    @POST
    @Path("virtualips")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createClusterVirtualIps(VirtualIps vips) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(VirtualIps.class).validate(vips, HttpRequestType.POST);
        for (VirtualIp vip : vips.getVirtualIps()) {
            vip.setClusterId(id);
        }

        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }

        return Response.status(Response.Status.ACCEPTED).entity(String.format("We are currently provisioning your virtual ips into Cluster %d", id)).build();
    }

    @GET
    public Response retrieveCluster() {
        org.openstack.atlas.service.domain.entities.Cluster domainCl;
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            domainCl = clusterService.get(id);
            org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster dataModelCls;
            dataModelCls = getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster.class, "SIMPLE_CL");
            dataModelCls.setNumberOfHostMachines(clusterService.getHosts(domainCl.getId()).size());
            dataModelCls.setNumberOfUniqueCustomers(clusterService.getNumberOfUniqueAccountsForCluster(domainCl.getId()));
            dataModelCls.setNumberOfLoadBalancingConfigurations(clusterService.getNumberOfActiveLoadBalancersForCluster(domainCl.getId()));
//            dataModelCls.setUtilization(getUtilization(domainCl.getId()));
            dataModelCls.setUtilization("0.0%");
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response deleteCluster(){
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        try {
            org.openstack.atlas.service.domain.entities.Cluster domainCl =
                    new org.openstack.atlas.service.domain.entities.Cluster();
            domainCl.setId(id);
            clusterService.deleteCluster(domainCl);
            return Response.status(Response.Status.ACCEPTED).build();
        }catch (ClusterNotEmptyException cne){
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, cne.getMessage());
        }catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateCluster(Cluster cluster) throws BadRequestException {
        org.openstack.atlas.service.domain.entities.Cluster domainCl;
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        ValidatorResult result = ValidatorRepository.getValidatorFor(Cluster.class).validate(cluster, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", result.getValidationErrorMessages())).build();
        }
        if(cluster.getPassword() != null) {
           String pemKey = cluster.getPassword();
                try {
                    CryptoUtil.decrypt(pemKey);
                } catch (Exception e) {
                    BadRequest badRequest = new BadRequest();
                    badRequest.setCode(400);
                    String errorMessages = "Error decrypting Private key on cluster, please check that your key is encrypted";
                    badRequest.setMessage(errorMessages);
                    return Response.status(400).entity(badRequest).build();
                }
        }
        try {
            org.openstack.atlas.service.domain.entities.Cluster domainCluster = getDozerMapper().map(cluster, org.openstack.atlas.service.domain.entities.Cluster.class);
            clusterService.updateCluster(domainCluster, id);
            return ResponseFactory.getSuccessResponse("PUT Operation Succeeded", 200);
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

    }

    @PUT
    @Path("subnetmappings")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putHostsSubnetMappings(org.openstack.atlas.docs.loadbalancers.api.management.v1.Hostssubnet rHostssubnet,
                                           @QueryParam("addpublicvips") Boolean addpublicvips,
                                           @QueryParam("addservicenetvips") Boolean addservicenetvips) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        VirtualIpType vipType = null;
        boolean addVips = false;
        if (addpublicvips != null && addpublicvips) {
            vipType =  VirtualIpType.PUBLIC;
            addVips = true;
        } else if (addservicenetvips != null && addservicenetvips) {
            vipType =  VirtualIpType.SERVICENET;
            addVips = true;
        }

        org.openstack.atlas.service.domain.entities.Cluster dCluster;
        try {
            dCluster = clusterService.get(id);
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

        EsbRequest req = new EsbRequest();
        org.openstack.atlas.service.domain.pojos.Hostssubnet dHostssubnet;
        if (rHostssubnet.getHostsubnets().size() != 1) {
            ValidationErrors vFault = new ValidationErrors();
            BadRequest badRequest = new BadRequest();
            badRequest.setCode(400);
            badRequest.setMessage("Invalid request");
            vFault.getMessages().add("Please specify only one host per request");
            return Response.status(400).entity(badRequest).build();
        }

        dHostssubnet = getDozerMapper().map(rHostssubnet, org.openstack.atlas.service.domain.pojos.Hostssubnet.class);
        req.setHostssubnet(dHostssubnet);
        req.setCluster(dCluster);

        req.setAddVips(addVips);
        req.setVirtualIpType(vipType);

        try {
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.SET_HOSTS_SUBNET_MAPPINGS, req);
            return Response.status(202).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    public void setVirtualIpsResource(VirtualIpsResource virtualIpsResource) {
        this.virtualIpsResource = virtualIpsResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // TODO: Verify if we're going to use these methods or not. Properly test when/if used...
    public String getUtilization(Integer id) {
        //get sum of max allowed connections for all host in cluster
        long maxAllowed = getHostRepository().getHostsConnectionsForCluster(id);
        double utilization = 0;

        if (maxAllowed > 0) {
            List<org.openstack.atlas.service.domain.entities.Host> hosts = clusterService.getHosts(id);
            int totalConnections = 0;
            for (org.openstack.atlas.service.domain.entities.Host dbHost : hosts) {
                int conn = 0;
                try {

                        conn = reverseProxyLoadBalancerVTMService.getTotalCurrentConnectionsForHost(dbHost);

                } catch (Exception e) {
                    LOG.error(e);
                    notificationService.saveAlert(e, AlertType.ZEUS_FAILURE.name(), "Error during getting total connections for host " + dbHost.getId());
                }
                totalConnections = totalConnections + conn;
            }
            utilization = (totalConnections / maxAllowed) * 100;
        }
        return (utilization + " %");
    }

    public ErrorpageResource getErrorpageResource() {
        return errorpageResource;
    }

    public void setErrorpageResource(ErrorpageResource errorpageResource) {
        this.errorpageResource = errorpageResource;
    }

    private List<String> getIpsNotContainedInASubnet(VirtualIpBlocks vBlocks, IPv4Cidrs ipv4Cidrs) throws IPStringConversionException, IPOctetOutOfRangeException {
        List<String> errors = new ArrayList<String>();
        for(VirtualIpBlock vBlock : vBlocks.getVirtualIpBlocks()){
            String first = vBlock.getFirstIp();
            String last = vBlock.getLastIp();
            long lo = IPv4ToolSet.ip2long(first);
            long hi = IPv4ToolSet.ip2long(last);
            if(lo>hi){
                String msg = String.format("LastIP=%s and FirstIP=%s must have been switched will not proceed",last,first);
                errors.add(msg);
                continue;
            }
            for(long i=lo;i<=hi;i++){
                String ipStr = IPv4ToolSet.long2ip(i);
                List<String> containingCidrs = ipv4Cidrs.getCidrsContainingIp(ipStr);
                if(containingCidrs.isEmpty()){
                    String msg = String.format("ip=%s not found in any Cidrs",ipStr);
                    errors.add(msg);
                }
            }

        }
        return errors;
    }

    private IPv4Cidrs getIpv4SubnetCidrs(Integer clusterId) throws Exception {
        IPv4Cidrs ipv4Cidrs = new IPv4Cidrs();
        Set<String> cidrs = new HashSet<String>();
        List<org.openstack.atlas.service.domain.entities.Host> hosts = clusterService.getHosts(clusterId);
        List<org.openstack.atlas.service.domain.pojos.Cidr> x;
        for (org.openstack.atlas.service.domain.entities.Host host : hosts) {
            Hostssubnet hostssubnet;

                hostssubnet = reverseProxyLoadBalancerVTMService.getSubnetMappings(host);

            for (Hostsubnet hostsubnet : hostssubnet.getHostsubnets()) {
                for (NetInterface ni : hostsubnet.getNetInterfaces()) {
                    for (org.openstack.atlas.service.domain.pojos.Cidr cidr : ni.getCidrs()) {
                        String block = cidr.getBlock();
                        if (IPUtils.isValidIpv4Subnet(block)) {
                            cidrs.add(block);// Avoid the duplicates we will be seeing by adding to a set only
                        }
                    }
                }
            }
        }
        for (String block : cidrs) {
            ipv4Cidrs.getCidrs().add(new IPv4Cidr(block));
        }
        return ipv4Cidrs;
    }
}
