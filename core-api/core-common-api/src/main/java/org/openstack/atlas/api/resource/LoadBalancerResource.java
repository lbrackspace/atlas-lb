package org.openstack.atlas.api.resource;

import org.apache.log4j.Logger;
import org.openstack.atlas.api.config.ConfigHelper;
import org.openstack.atlas.api.config.PluginContextLoaderListener;
import org.openstack.atlas.api.resource.provider.CommonDependencyProvider;
import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.LoadBalancerValidator;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.openstack.atlas.service.domain.pojo.MessageDataContainer;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.service.LoadBalancerService;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.*;

@Controller
@Scope("request")
public class LoadBalancerResource extends CommonDependencyProvider {
    protected final Logger LOG = Logger.getLogger(LoadBalancerResource.class);
    protected Integer id;
    protected Integer accountId;

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected LoadBalancerValidator validator;
    @Autowired
    protected LoadBalancerService loadBalancerService;
    @Autowired
    protected VirtualIpsResource virtualIpsResource;
    @Autowired
    protected NodesResource nodesResource;
    @Autowired
    protected HealthMonitorResource healthMonitorResource;
    @Autowired
    protected ConnectionThrottleResource connectionThrottleResource;
    @Autowired
    protected SessionPersistenceResource sessionPersistenceResource;
    @Autowired
    protected UsageResource usageResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response get() {
        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = loadBalancerRepository.getByIdAndAccountId(id, accountId);
            LoadBalancer _loadBalancer = dozerMapper.map(loadBalancer, LoadBalancer.class);
            return Response.status(Response.Status.OK).entity(_loadBalancer).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response update(LoadBalancer _loadBalancer) {
        ValidatorResult result = validator.validate(_loadBalancer, HttpRequestType.PUT);

        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = dozerMapper.map(_loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            loadBalancer.setId(id);
            loadBalancer.setAccountId(accountId);

            loadBalancerService.update(loadBalancer);

            MessageDataContainer msg = new MessageDataContainer();
            msg.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @DELETE
    public Response deleteLoadBalancer() {
        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer loadBalancer = new org.openstack.atlas.service.domain.entity.LoadBalancer();
            loadBalancer.setId(id);
            loadBalancer.setAccountId(accountId);

            loadBalancerService.preDelete(accountId, id);

            MessageDataContainer data = new MessageDataContainer();
            data.setLoadBalancer(loadBalancer);

            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_LOADBALANCER, data);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e);
        }
    }

    @Path("virtualips")
    public Object retrieveVirtualIpsResource() throws IllegalAccessException, InstantiationException {
        List<String> enabledExtensions = ConfigHelper.getExtensionPrefixesFromConfiguration();
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();

        for (String enabledExtension : enabledExtensions) {
            configBuilder.addUrls(ClasspathHelper.forPackage("org.openstack.atlas." + enabledExtension + ".api"));
        }

        // TODO: Decompose this out and dynamically resolve sub-resource base off of extensions prefix
        Reflections reflections = new Reflections(configBuilder.setScanners(new SubTypesScanner()));
        Set<Class<? extends VirtualIpsResource>> subTypes = reflections.getSubTypesOf(VirtualIpsResource.class);

        if (subTypes.iterator().hasNext()) {
            final Class<? extends VirtualIpsResource> subClass = subTypes.iterator().next();
            final VirtualIpsResource bean = PluginContextLoaderListener.getCurrentWebApplicationContext().getBean(subClass);
            bean.setLoadBalancerId(id);
            bean.setAccountId(accountId);
            return bean;
        } else {
            virtualIpsResource.setLoadBalancerId(id);
            virtualIpsResource.setAccountId(accountId);
            return  virtualIpsResource;
        }
    }

    @Path("nodes")
    public NodesResource retrieveNodesResource() {
        nodesResource.setLoadBalancerId(id);
        nodesResource.setAccountId(accountId);
        return nodesResource;
    }

    @Path("healthmonitor")
    public HealthMonitorResource retrieveHealthMonitorResource() {
        healthMonitorResource.setLoadBalancerId(id);
        healthMonitorResource.setAccountId(accountId);
        return healthMonitorResource;
    }

    @Path("connectionthrottle")
    public ConnectionThrottleResource retrieveConnectionThrottleResource() {
        connectionThrottleResource.setLoadBalancerId(id);
        connectionThrottleResource.setAccountId(accountId);
        return connectionThrottleResource;
    }

    @Path("sessionpersistence")
    public SessionPersistenceResource retrieveSessionPersistenceResource() {
        sessionPersistenceResource.setLoadBalancerId(id);
        sessionPersistenceResource.setAccountId(accountId);
        return sessionPersistenceResource;
    }

    @Path("usage")
    public UsageResource retrieveUsageResource() {
        usageResource.setLoadBalancerId(id);
        usageResource.setAccountId(accountId);
        return usageResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
