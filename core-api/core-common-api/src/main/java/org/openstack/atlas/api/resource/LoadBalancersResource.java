package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.LoadBalancerValidator;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.core.api.v1.LoadBalancers;
import org.openstack.atlas.service.domain.operation.CoreOperation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.openstack.atlas.service.domain.service.VirtualIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class LoadBalancersResource extends CommonDependencyProvider {
    private final Logger LOG = Logger.getLogger(LoadBalancersResource.class);
    private HttpHeaders requestHeaders;
    protected Integer accountId;

    @Autowired
    protected LoadBalancerValidator validator;
    @Autowired
    protected LoadBalancerService loadbalancerService;
    @Autowired
    protected VirtualIpService virtualIpService;
    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected LoadBalancerResource loadBalancerResource;

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response create(LoadBalancer _loadBalancer) {
        ValidatorResult result = validator.validate(_loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = dozerMapper.map(_loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            loadBalancer.setAccountId(accountId);

            loadBalancer = loadbalancerService.create(loadBalancer);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(CoreOperation.CREATE_LOADBALANCER, dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(loadBalancer, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response list() {
        LoadBalancers _loadbalancers = new LoadBalancers();
        List<org.openstack.atlas.service.domain.entity.LoadBalancer> loadbalancers = loadBalancerRepository.getByAccountId(accountId);
        for (org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer : loadbalancers) {
            _loadbalancers.getLoadBalancers().add(dozerMapper.map(loadBalancer, org.openstack.atlas.core.api.v1.LoadBalancer.class, "SIMPLE_LB"));
        }
        return Response.status(Response.Status.OK).entity(_loadbalancers).build();

    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public LoadBalancerResource retrieveLoadBalancerResource(@PathParam("id") int id) {
        loadBalancerResource.setId(id);
        loadBalancerResource.setAccountId(accountId);
        return loadBalancerResource;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
