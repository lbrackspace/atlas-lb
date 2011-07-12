package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.api.validation.verifiers.HealthMonitorTypeVerifier;
import org.openstack.atlas.api.validation.verifiers.HealthMonitorTypeVerifier;
import org.openstack.atlas.api.validation.verifiers.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeIntegerInRange;

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
                result(validationTarget().getDelay()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_DELAY_RANGE[0], MAX_DELAY_RANGE[1])).withMessage("Must specify valid delay range.");
                result(validationTarget().getTimeout()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_TIMEOUT_RANGE[0], MAX_TIMEOUT_RANGE[1])).withMessage("Must specify valid timeout range.");
                result(validationTarget().getAttemptsBeforeDeactivation()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_ATTEMPTS_BEFORE_DEACTIVATION[0], MAX_ATTEMPTS_BEFORE_DEACTIVATION[1])).withMessage("Must specify valid attempts before deactivation range.");

                // PUT EXPECTATIONS
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.CONNECT)).then().must().delegateTo(new ConnectHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTP)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(HealthMonitorType.HTTPS)).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);

            }
        });
    }

    @Override
    public ValidatorResult validate(HealthMonitor healthMonitor, Object ctx) {
        return validator.validate(healthMonitor, ctx);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
