package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancers;
import org.openstack.atlas.api.mgmt.validation.contexts.ReassignHostContext;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class LoadBalancerValidator implements ResourceValidator<LoadBalancer> {

    private final Validator<LoadBalancer> validator;

    public LoadBalancerValidator() {
            validator = build(new ValidatorBuilder<LoadBalancer>(LoadBalancer.class) {
                {
                    //EXPECTATIONS
                    result(validationTarget().getAlgorithm()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Algorithm not allowed for this request.");
                    result(validationTarget().getCluster()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Cluster not allowed for this request.");
                    result(validationTarget().getRateLimit()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Rate limit not allowed for this request.");
                    result(validationTarget().getConnectionThrottle()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Connection throttle not allowed for this request.");
                    result(validationTarget().getHealthMonitor()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Health monitor not allowed for this request.");
                    result(validationTarget().getName()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Name not allowed for this request.");
                    result(validationTarget().getSuspension()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Suspension not allowed for this request.");
                    result(validationTarget().getSessionPersistence()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Session persistence not allowed for this request.");
                    result(validationTarget().getPort()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Port not allowed for this request.");
                    result(validationTarget().getProtocol()).must().not().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Protocol not allowed for this request.");
                    result(validationTarget().getHost()).if_().exist().then().must().delegateTo(new HostValidator().getValidator(), ReassignHostContext.REASSIGN_HOST).forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Hostl failed.");
                    result(validationTarget().getId()).must().exist().forContext(ReassignHostContext.REASSIGN_HOST).withMessage("Must provide ID for this request.");
                    result(validationTarget().getConnectionLogging()).must().not().exist().withMessage("Connection logging not allowed for this request.");

                }
            });
        }

        @Override
        public ValidatorResult validate(LoadBalancer lb, Object httpRequestType) {
            ValidatorResult result = validator.validate(lb, httpRequestType);
            return ValidatorUtilities.removeEmptyMessages(result);
        }

        @Override
        public Validator<LoadBalancer> getValidator() {
            return validator;
        }

    }
