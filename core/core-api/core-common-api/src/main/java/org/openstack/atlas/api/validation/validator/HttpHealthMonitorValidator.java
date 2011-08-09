package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.verifier.*;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.validator.HealthMonitorValidator.CEILING;
import static org.openstack.atlas.api.validation.validator.HealthMonitorValidator.FLOOR;

public class HttpHealthMonitorValidator implements ResourceValidator<HealthMonitor> {
    private Validator<HealthMonitor> validator;
    private static final int MAXSTR=128;
    private static final String maxStrMsg = String.format(" can not exceed %d characters.",MAXSTR);

    public HttpHealthMonitorValidator() {
        this.validator = build(new ValidatorBuilder<HealthMonitor>(HealthMonitor.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getType()).must().exist().withMessage("Must provide a type for the health monitor.");
                result(validationTarget().getId()).must().not().exist().withMessage("Health monitor id field cannot be modified.");
                result(validationTarget().getDelay()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Delay for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getTimeout()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Timeout for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getAttemptsBeforeDeactivation()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Attempts before deactivation for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new MustNotBeEmpty()).withMessage("Must provide a valid path for the health monitor.");
                result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new CannotExceedSize(MAXSTR)).withMessage("path" + maxStrMsg);
                result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new HealthMonitorPathVerifier()).withMessage("Must provide a foward slash(/) as the begining of the path.");                                                                                                                         

                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<HealthMonitor>() {
                    @Override
                    public VerifierResult verify(HealthMonitor monitor) {
                        return new VerifierResult(monitor.getDelay() != null ||
                                monitor.getTimeout() != null ||
                                monitor.getAttemptsBeforeDeactivation() != null ||
                                monitor.getPath() != null);
                    }
                }).forContext(PUT).withMessage("The health monitor must have at least one of the following to update: delay, timeout, attempts before deactivation, path, status regex, body regex.");
            }
        });
    }

    @Override
    public ValidatorResult validate(HealthMonitor monitor, Object monitorContext) {
        ValidatorResult result = validator.validate(monitor, monitorContext);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
