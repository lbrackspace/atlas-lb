package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.validators.HealthMonitorValidator.CEILING;
import static org.openstack.atlas.api.validation.validators.HealthMonitorValidator.FLOOR;

public class ConnectHealthMonitorValidator implements ResourceValidator<HealthMonitor> {

    private final Validator<HealthMonitor> validator;

    public ConnectHealthMonitorValidator() {

        validator = build(new ValidatorBuilder<HealthMonitor>(HealthMonitor.class) {
            {
                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<HealthMonitor>() {
                    @Override
                    public VerifierResult verify(HealthMonitor monitor) {
                        return new VerifierResult(monitor.getDelay() != null || monitor.getTimeout() != null || monitor.getAttemptsBeforeDeactivation() != null);
                    }
                }).forContext(PUT).withMessage("The health monitor must have at least one of the following to update: delay, timeout, attempts before deactivation.");

                // SHARED EXPECTATIONS
                result(validationTarget().getType()).must().exist().withMessage("Must provide a type for the health monitor.");
                result(validationTarget().getId()).must().not().exist().withMessage("Health monitor id field cannot be modified.");
                result(validationTarget().getPath()).must().not().exist().withMessage("A connect health monitor may not have a path. Use HTTP/HTTPS health monitor instead.");
                result(validationTarget().getBodyRegex()).must().not().exist().withMessage("A connect health monitor may not have a body regex. Use HTTP/HTTPS health monitor instead.");
                result(validationTarget().getStatusRegex()).must().not().exist().withMessage("A connect health monitor may not have a status regex. Use HTTP/HTTPS health monitor instead.");
                result(validationTarget().getDelay()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Delay for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getTimeout()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Timeout for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getAttemptsBeforeDeactivation()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(FLOOR, CEILING)).withMessage(String.format("Attempts before deactivation for the health monitor must be between %d and %d.", FLOOR, CEILING));
                result(validationTarget().getHostHeader()).must().not().exist().withMessage("Host Header is not supported for CONNECT based health monitors.");
            }
        });
    }

    @Override
    public ValidatorResult validate(HealthMonitor healthMonitor, Object monitorContext) {
        ValidatorResult result = validator.validate(healthMonitor, monitorContext);
        return ValidatorUtilities.removeEmptyMessages(result);
    }


    @Override
    public Validator<HealthMonitor> getValidator() {
        return validator;
    }
}
