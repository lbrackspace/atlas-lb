package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.verifier.HealthMonitorTypeVerifier;
import org.openstack.atlas.api.validation.verifier.MustBeIntegerInRange;
import org.openstack.atlas.datamodel.CoreHealthMonitorType;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

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
                if_().adhereTo(new HealthMonitorTypeVerifier(new CoreHealthMonitorType(CoreHealthMonitorType.CONNECT))).then().must().delegateTo(new ConnectHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(new CoreHealthMonitorType(CoreHealthMonitorType.HTTP))).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);
                if_().adhereTo(new HealthMonitorTypeVerifier(new CoreHealthMonitorType(CoreHealthMonitorType.HTTPS))).then().must().delegateTo(new HttpHealthMonitorValidator().getValidator(), PUT).forContext(PUT);

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
