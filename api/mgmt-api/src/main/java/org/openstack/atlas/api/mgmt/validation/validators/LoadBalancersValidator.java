package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class LoadBalancersValidator implements ResourceValidator<LoadBalancers> {

    private final Validator<LoadBalancers> validator;

    public LoadBalancersValidator() {
        validator = build(new ValidatorBuilder<LoadBalancers>(LoadBalancers.class) {
            {
                // PUT EXPECTATIONS
                result(validationTarget().getLoadBalancers()).must().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must provide valid load balancers.");
                result(validationTarget().getLoadBalancers()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide atleast one load balancer.");
                result(validationTarget().getLoadBalancers()).if_().exist().then().must().delegateTo(new LoadBalancerValidator().getValidator(), ReassignHostContext.REASSIGN_HOST);
            }
        });
    }

    @Override
    public ValidatorResult validate(LoadBalancers lbs, Object httpRequestType) {
        ValidatorResult result = validator.validate(lbs, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadBalancers> getValidator() {
        return validator;
    }

}
