package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroup;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroups;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.*;


public class LoadBalancerLimitGroupsValidator implements ResourceValidator<LoadBalancerLimitGroups> {

    private final Validator<LoadBalancerLimitGroups> validator;

    public LoadBalancerLimitGroupsValidator() {
        validator = build(new ValidatorBuilder<LoadBalancerLimitGroups>(LoadBalancerLimitGroups.class) {
            {
                result(validationTarget().getLoadBalancerLimitGroups()).must().exist().withMessage("Must provide valid load balancer limit group.");
                result(validationTarget().getLoadBalancerLimitGroups()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide atleast one load balancer limit group.");
                result(validationTarget().getLoadBalancerLimitGroups()).if_().exist().then().must().delegateTo(new LoadBalancerLimitGroupValidator().getValidator(), POST);
                result(validationTarget().getLoadBalancerLimitGroups()).if_().exist().then().must().delegateTo(new LoadBalancerLimitGroupValidator().getValidator(), PUT);

            }
        });
    }

    @Override
    public ValidatorResult validate(LoadBalancerLimitGroups lbs, Object httpRequestType) {
        ValidatorResult result = validator.validate(lbs, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadBalancerLimitGroups> getValidator() {
        return validator;
    }

}