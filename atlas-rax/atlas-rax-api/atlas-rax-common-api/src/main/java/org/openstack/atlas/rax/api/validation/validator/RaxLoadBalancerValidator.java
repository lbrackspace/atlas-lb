package org.openstack.atlas.rax.api.validation.validator;

import org.openstack.atlas.api.validation.validator.LoadBalancerValidator;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@Component("RAX-RaxLoadBalancerValidator")
@Scope("request")
public class RaxLoadBalancerValidator extends LoadBalancerValidator {

    public RaxLoadBalancerValidator() {
        validator = build(new MySecondBuilder(LoadBalancer.class));
    }

    private class MySecondBuilder extends MyBuilder {
        {
            // POST EXPECTATIONS
            result(validationTarget().getName()).must().not().exist().forContext(POST).withMessage("Must NOT provide a name for the load balancer. :)");
        }

        public MySecondBuilder(Class<LoadBalancer> type) {
            super(type);
        }
    }
}
