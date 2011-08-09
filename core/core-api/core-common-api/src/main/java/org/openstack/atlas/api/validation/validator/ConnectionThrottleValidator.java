package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.verifier.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class ConnectionThrottleValidator implements ResourceValidator<ConnectionThrottle> {
    private Validator<ConnectionThrottle> validator;
    private final int[] MAX_CONNECTION_RATE = new int[]{0, 100000};
    private final int[] RATE_INTERVAL = new int[]{1, 3600};

    public ConnectionThrottleValidator() {
        validator = build(new ValidatorBuilder<ConnectionThrottle>(ConnectionThrottle.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getMaxRequestRate()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_CONNECTION_RATE[0], MAX_CONNECTION_RATE[1])).withMessage("Must provde a valid maximum connection rate range.");
                result(validationTarget().getRateInterval()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(RATE_INTERVAL[0], RATE_INTERVAL[1])).withMessage("Must provide a valid rate interval range.");

                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<ConnectionThrottle>() {
                    @Override
                    public VerifierResult verify(ConnectionThrottle obj) {
                        return new VerifierResult(obj.getMaxRequestRate() != null || obj.getRateInterval() != null);
                    }
                }).forContext(PUT).withMessage("You must provide at least one of the following: minConnections, maxConnections, maxConnectionRate, rateInterval.");
            }
        });
    }

    @Override
    public ValidatorResult validate(ConnectionThrottle connectionLimits, Object requestType) {
        ValidatorResult result = validator.validate(connectionLimits, requestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ConnectionThrottle> getValidator() {
        return validator;
    }
}
