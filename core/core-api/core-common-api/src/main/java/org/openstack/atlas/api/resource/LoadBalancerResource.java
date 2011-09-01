package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class LoadBalancerResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(LoadBalancerResource.class);

    private int id;
    private Integer accountId;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response get() {
        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = loadBalancerRepository.getByIdAndAccountId(id, accountId);
            LoadBalancer _loadBalancer = dozerMapper.map(loadBalancer, LoadBalancer.class);
            return Response.status(Response.Status.OK).entity(_loadBalancer).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
