package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerLimitGroup;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class LoadBalancerLimitGroupValidator implements ResourceValidator<LoadBalancerLimitGroup> {

    private final Validator<LoadBalancerLimitGroup> validator;

    public LoadBalancerLimitGroupValidator() {
            validator = build(new ValidatorBuilder<LoadBalancerLimitGroup>(LoadBalancerLimitGroup.class) {
                {
                    // POST EXPECTATIONS
                    result(validationTarget().getLimit()).must().exist().forContext(POST).withMessage("Must provide a limit for the load balancer limit group.");
                    result(validationTarget().getName()).must().exist().forContext(POST).withMessage("Must provide a name for the load balancer limit group.");
                    result(validationTarget().isIsDefault()).must().exist().forContext(POST).withMessage("Must provide the default value for the load balancer limit group.");

                    //Shared EXPECTATIONS
                    result(validationTarget().getId()).must().not().exist().withMessage("load balancer limit id not allowed for this request.");
                    result(validationTarget().getLimit()).if_().exist().then().must().not().beEmptyOrNull().withMessage("Must provide a limit for load balancer limit group update.");
                    result(validationTarget().getName()).if_().exist().then().must().not().beEmptyOrNull().withMessage("Must provide a name for load balancer limit group update.");
                    result(validationTarget().isIsDefault()).if_().exist().then().must().not().beEmptyOrNull().withMessage("Must provide the default value for load balancer limit group update.");

                    //PUT EXPECTATIONS


                }
            });
        }

        @Override
        public ValidatorResult validate(LoadBalancerLimitGroup lb, Object httpRequestType) {
            ValidatorResult result = validator.validate(lb, httpRequestType);
            return ValidatorUtilities.removeEmptyMessages(result);
        }

        @Override
        public Validator<LoadBalancerLimitGroup> getValidator() {
            return validator;
        }

    }
