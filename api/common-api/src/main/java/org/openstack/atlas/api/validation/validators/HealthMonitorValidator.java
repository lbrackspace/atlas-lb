package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.api.validation.verifiers.HealthMonitorTypeVerifier;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeIntegerInRange;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class HealthMonitorValidator implements ResourceValidator<HealthMonitor> {

    private final Validator<HealthMonitor> validator;
    public static final Integer FLOOR = 1;
    public static final Integer CEILING = 99999;
    private final int[] MAX_DELAY_RANGE = new int[]{1, 3600};
    private final int[] MAX_TIMEOUT_RANGE = new int[]{1, 300};
    private final int[] MAX_ATTEMPTS_BEFORE_DEACTIVATION = new int[]{1, 10};

    public HealthMonitorValidator() {

        validator = build(new ValidatorBuilder<HealthMonitor>(HealthMonitor.class) {
            {
                // SHARED EXPECTATIONS
                must().exist().withMessage("Must provide a health monitor.");
                result(validationTarget().getType()).must().exist().withMessage("Must provide a type for the health monitor.");
                result(validationTarget().getDelay()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_DELAY_RANGE[0], MAX_DELAY_RANGE[1])).withMessage(String.format("Must specify valid delay range between %d and %d.", MAX_DELAY_RANGE[0], MAX_DELAY_RANGE[1]));
                result(validationTarget().getTimeout()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_TIMEOUT_RANGE[0], MAX_TIMEOUT_RANGE[1])).withMessage(String.format("Must specify valid timeout range between %d and %d.",MAX_TIMEOUT_RANGE[0],MAX_TIMEOUT_RANGE[1]));
                result(validationTarget().getAttemptsBeforeDeactivation()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_ATTEMPTS_BEFORE_DEACTIVATION[0], MAX_ATTEMPTS_BEFORE_DEACTIVATION[1])).withMessage(String.format("Must specify valid attempts before deactivation range between %d and %d.",MAX_ATTEMPTS_BEFORE_DEACTIVATION[0],MAX_ATTEMPTS_BEFORE_DEACTIVATION[1]));

                // Fix for CLB-72 New LB calls allow an incorrect "path" attribute when including healthMonitoring objects/clb-299 validation of body/status regex should be a shared expectation
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.CONNECT)).then().must().delegateTo(new ConnectHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTP)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTPS)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.CONNECT)).then().must().delegateTo(new ConnectHealthMonitorValidator().getValidator(), POST).forContext(POST);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTP)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), POST).forContext(POST);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTPS)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), POST).forContext(POST);

                //CLB-279 Creating a Load Balancer with empty Health Monitor - creates the Load Balancer in Error state.
                result(validationTarget().getDelay()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide delay for the health monitor.");
                result(validationTarget().getTimeout()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide timeout for the health monitor.");
                result(validationTarget().getAttemptsBeforeDeactivation()).must().adhereTo(new MustNotBeEmpty()).forContext(POST).withMessage("Must provide attempts before deactivation for the health monitor.");
            }
        });
    }

    @Override
    public ValidatorResult validate(HealthMonitor healthMonitor, Object ctx) {
        ValidatorResult result = validator.validate(healthMonitor, ctx);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
