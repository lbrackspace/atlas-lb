package org.openstack.atlas.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.RegionalSourceAddresses;
import org.openstack.atlas.service.domain.entities.ClusterType;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

public class RegionalSourceAddressesResource extends CommonDependencyProvider {

    private final Log LOG = LogFactory.getLog(RegionalSourceAddressesResource.class);
    private Integer accountId;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveRegionalSourceAddresses() {

        try {
            ClusterType cType = clusterService.getClusterTypeByAccountId(accountId);
            RegionalSourceAddresses rsa = hostService.getRegionalSourceAddresses(cType, accountId);
            return Response.status(Response.Status.OK).entity(rsa).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
