package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.*;
import org.openstack.atlas.core.api.v1.HealthMonitor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Component
@Scope("request")
public class HttpMonitorValidatorBuilder extends ValidatorBuilder<HealthMonitor> {
    public static final int[] DELAY_RANGE = new int[]{1, 3600};
    public static final int[] TIMEOUT_RANGE = new int[]{1, 300};
    public static final int[] ATTEMPTS_BEFORE_DEACTIVATION_RANGE = new int[]{1, 10};
    public static final int MAX_PATH_LENGTH = 128;

    public HttpMonitorValidatorBuilder() {
        super(HealthMonitor.class);

        // SHARED EXPECTATIONS
        result(validationTarget().getType()).must().exist().withMessage("Must provide a type for the health monitor.");
        result(validationTarget().getDelay()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(DELAY_RANGE[0], DELAY_RANGE[1])).withMessage(String.format("Delay for the health monitor must be between %d and %d.", DELAY_RANGE[0], DELAY_RANGE[1]));
        result(validationTarget().getTimeout()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(TIMEOUT_RANGE[0], TIMEOUT_RANGE[1])).withMessage(String.format("Timeout for the health monitor must be between %d and %d.", TIMEOUT_RANGE[0], TIMEOUT_RANGE[1]));
        result(validationTarget().getAttemptsBeforeDeactivation()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(ATTEMPTS_BEFORE_DEACTIVATION_RANGE[0], ATTEMPTS_BEFORE_DEACTIVATION_RANGE[1])).withMessage(String.format("Attempts before deactivation for the health monitor must be between %d and %d.", ATTEMPTS_BEFORE_DEACTIVATION_RANGE[0], ATTEMPTS_BEFORE_DEACTIVATION_RANGE[1]));
        result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new MustNotBeEmpty()).withMessage("Must provide a valid path for the health monitor.");
        result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new HaveSizeOfAtMost(MAX_PATH_LENGTH)).withMessage(String.format("Path can not exceed %d characters.", MAX_PATH_LENGTH));
        result(validationTarget().getPath()).if_().exist().then().must().adhereTo(new HealthMonitorPathVerifier()).withMessage("Must provide a forward slash(/) as the beginning of the path.");

        // PUT EXPECTATIONS
        must().adhereTo(new Verifier<HealthMonitor>() {
            @Override
            public VerifierResult verify(HealthMonitor monitor) {
                return new VerifierResult(monitor.getDelay() != null ||
                        monitor.getTimeout() != null ||
                        monitor.getAttemptsBeforeDeactivation() != null ||
                        monitor.getPath() != null);
            }
        }).forContext(PUT).withMessage("The health monitor must have at least one of the following to update: delay, timeout, attempts before deactivation, path.");

    }
}
