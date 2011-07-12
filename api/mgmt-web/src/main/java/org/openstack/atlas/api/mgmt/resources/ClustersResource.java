package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.operations.OperationResponse;
import org.openstack.atlas.service.domain.services.helpers.AlertType;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class ClustersResource extends ManagementDependencyProvider {

    private ClusterResource clusterResource;
     private final Log LOG = LogFactory.getLog(ClustersResource.class);

    @GET
    public Response retrieveAllClusters(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.Cluster> domainCls;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters dataModelCls = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters();
        try {
            domainCls = getClusterRepository().getAll(offset, limit);


            for (org.openstack.atlas.service.domain.entities.Cluster domainCl : domainCls) {
                dataModelCls.getClusters().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster.class, "SIMPLE_CL"));
            }

            for (org.openstack.atlas.docs.loadbalancers.api.management.v1.Cluster cl : dataModelCls.getClusters()) {
                cl.setNumberOfHostMachines(getClusterRepository().getHosts(cl.getId()).size());
                cl.setNumberOfUniqueCustomers(getClusterRepository().getNumberOfUniqueAccountsForCluster(cl.getId()));
                cl.setNumberOfLoadBalancingConfigurations(getClusterRepository().getNumberOfLoadBalancersForCluster(cl.getId()));
                /* TODO: Read ticket SITESLB-1360 */ //cl.setUtilization(getUtilization(domainCl.getId()));
                cl.setUtilization("0.0%");

            }
            return Response.status(200).entity(dataModelCls).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [1-9][0-9]*}")
    public ClusterResource ammendClusterId(@PathParam("id") int id) {
        clusterResource.setId(id);
        return clusterResource;
    }

    // According to Jira:https://jira.mosso.com/browse/SITESLB-219
    @POST
    @Path("customers")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCustomersList(ByIdOrName idOrName) {
        Object key;
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.pojos.Customer> dcustomerList;
        CustomerList rcustomerList = new CustomerList();
        Customer rcustomer;

        try {
            if (idOrName.getId() == null && idOrName.getName() != null) {
                key = (String) idOrName.getName();
                dcustomerList = getClusterRepository().getCustomerList(key);
            } else if (idOrName.getId() != null && idOrName.getName() == null) {
                key = (Integer) idOrName.getId();
                dcustomerList = getClusterRepository().getCustomerList(key);
            } else {
                ValidationErrors validationFault = new ValidationErrors();
                String errMsg = "Choose only the Id attribute or Name attribute, but not both. Using neither is also invalid.";
                validationFault.getMessages().add(errMsg);
                return Response.status(400).entity(validationFault).build();
            }
            for (org.openstack.atlas.service.domain.pojos.Customer dcustomer : dcustomerList) {
                rcustomer = new Customer();
                rcustomer.setAccountId(dcustomer.getAccountId());
                for (org.openstack.atlas.service.domain.entities.LoadBalancer dloadbalancer : dcustomer.getLoadBalancers()) {
                    rcustomer.getLoadBalancers().add(getDozerMapper().map(dloadbalancer, LoadBalancer.class, "SIMPLE_CUSTOMER_LB"));
                }
                rcustomerList.getCustomers().add(rcustomer);
            }

            return Response.status(200).entity(rcustomerList).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("pollendpoints")
    public Response pollEndPoints() {
                if(!isUserInRole("ops,cp")) {
            return ResponseFactory.accessDenied();
        }
        EsbRequest req = new EsbRequest();
        OperationResponse resp;
        try {
            getManagementAsyncService().callAsyncLoadBalancingOperation(Operation.POLL_ENDPOINTS, req);

            return ResponseFactory.getSuccessResponse("EndPoint Poller Called", 200);

        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }

    }

    public void setClusterResource(ClusterResource clusterResource) {
        this.clusterResource = clusterResource;
    }

    public String getUtilization(Cluster cl) {
        double utilization = 0;
        //get sum of max allowed connections for all host in cluster
        long maxAllowed = getHostRepository().getHostsConnectionsForCluster(cl.getId());

        if (maxAllowed > 0) {
            List<org.openstack.atlas.service.domain.entities.Host> hosts = getClusterRepository().getHosts(cl.getId());

            int totalConnections = 0;
            for (org.openstack.atlas.service.domain.entities.Host dbHost : hosts) {
                int conn = 0;
                try {
                    conn = reverseProxyLoadBalancerService.getTotalCurrentConnectionsForHost(dbHost);
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
}
