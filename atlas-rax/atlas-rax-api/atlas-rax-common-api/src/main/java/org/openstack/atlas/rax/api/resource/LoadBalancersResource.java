package org.openstack.atlas.rax.api.resource;

import org.openstack.atlas.api.response.ResponseFactory;
import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.v1.extensions.rax.NetworkItem;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.openstack.atlas.rax.domain.entity.AccessListType;
import org.openstack.atlas.rax.domain.entity.RaxLoadBalancer;
import org.openstack.atlas.rax.domain.pojo.RaxMessageDataContainer;
import org.openstack.atlas.service.domain.operation.Operation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller("RAX-LoadBalancersResource")
@Scope("request")
public class LoadBalancersResource extends org.openstack.atlas.api.resource.LoadBalancersResource {
    Logger logger = Logger.getLogger("LoadBalancersResource");

    @Override
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        logger.log(Level.INFO, "loadbalancer: " + loadBalancer);

        AccessList _accessList = AnyObjectMapper.getAccessList(loadBalancer);
        String crazyName = AnyObjectMapper.getCrazyName(loadBalancer);

        ValidatorResult result = validator.validate(loadBalancer, HttpRequestType.POST);
        if (!result.passedValidation()) {
            return ResponseFactory.getValidationFaultResponse(result);
        }

        try {
            org.openstack.atlas.service.domain.entity.LoadBalancer mappedLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entity.LoadBalancer.class);
            mappedLb.setAccountId(accountId);

            RaxLoadBalancer raxLoadBalancer = dozerMapper.map(mappedLb, RaxLoadBalancer.class);
            raxLoadBalancer.setAccessLists(getDomainAccessLists(_accessList));
            raxLoadBalancer.setCrazyName(crazyName);

            //This call should be moved somewhere else
            virtualIpService.addAccountRecord(accountId);

            org.openstack.atlas.service.domain.entity.LoadBalancer newlyCreatedLb = loadbalancerService.create(raxLoadBalancer);
            RaxMessageDataContainer msg = new RaxMessageDataContainer();
            msg.setLoadBalancer(newlyCreatedLb);
            asyncService.callAsyncLoadBalancingOperation(Operation.CREATE_LOADBALANCER, msg);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(newlyCreatedLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private Set<org.openstack.atlas.rax.domain.entity.AccessList> getDomainAccessLists(AccessList _accessList) {
        //TODO : Use dozer mapper to map to AccessList Entity
        Set<org.openstack.atlas.rax.domain.entity.AccessList> accessLists = new HashSet<org.openstack.atlas.rax.domain.entity.AccessList>();
        if (_accessList == null) {
            logger.log(Level.INFO, "No accesslist found");
        } else {
            for (NetworkItem _networkItem : _accessList.getNetworkItems()) {
                logger.log(Level.INFO, "Element Network Item: " + _networkItem.getAddress() + " : " + _networkItem.getType());
                org.openstack.atlas.rax.domain.entity.AccessList accessList = new org.openstack.atlas.rax.domain.entity.AccessList();
                accessList.setIpAddress(_networkItem.getAddress());
                accessList.setType(AccessListType.valueOf(_networkItem.getType().value()));
                accessLists.add(accessList);
            }
        }
        return accessLists;
    }
}
