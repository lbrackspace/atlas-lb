package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Component
@Scope("request")
public class ConnectionThrottleValidatorBuilder extends ValidatorBuilder<ConnectionThrottle> {
    public final int[] MAX_REQUEST_RATE = new int[]{0, 100000};
    public final int[] RATE_INTERVAL = new int[]{1, 3600};
    private final int[] MIN_CONNECTIONS = new int[]{0, 1000};
    private final int[] MAX_CONNECTIONS = new int[]{1, 100000};

    public ConnectionThrottleValidatorBuilder() {
        super(ConnectionThrottle.class);

        // SHARED EXPECTATIONS
        result(validationTarget().getMaxRequestRate()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MAX_REQUEST_RATE[0], MAX_REQUEST_RATE[1])).withMessage("Must provide a valid maximum connection rate range.");
        result(validationTarget().getRateInterval()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(RATE_INTERVAL[0], RATE_INTERVAL[1])).withMessage("Must provide a valid rate interval range.");

        // PUT EXPECTATIONS
        must().adhereTo(new Verifier<ConnectionThrottle>() {
            @Override
            public VerifierResult verify(ConnectionThrottle obj) {
                return new VerifierResult(obj.getMaxRequestRate() != null || obj.getRateInterval() != null);
            }
        }).forContext(PUT).withMessage("You must provide at least one of the following: maxConnectionRate, rateInterval.");
    }
}
