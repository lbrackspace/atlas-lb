package org.openstack.atlas.rax.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.validator.builder.ConnectionThrottleValidatorBuilder;
import org.openstack.atlas.api.validation.validator.builder.LoadBalancerValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeEmptyOrNull;
import org.openstack.atlas.api.validation.verifier.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.core.api.v1.ConnectionThrottle;
import org.openstack.atlas.rax.api.mapper.dozer.converter.ExtensionObjectMapper;
import org.openstack.atlas.rax.api.validation.validator.RaxLoadBalancerValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Primary
@Component
@Scope("request")
public class RaxConnectionThrottleValidatorBuilder extends ConnectionThrottleValidatorBuilder {
    private final int[] MIN_CONNECTIONS = new int[]{0, 1000};
    private final int[] MAX_CONNECTIONS = new int[]{1, 100000};

    public RaxConnectionThrottleValidatorBuilder() {
        super();
        //PUT EXPECTATIONS
        result(validationTarget().getOtherAttributes()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String minConnections = ExtensionObjectMapper.getOtherAttribute(otherAttributes, "minConnections");
                return new MustBeIntegerInRange(MIN_CONNECTIONS[0], MIN_CONNECTIONS[1]).verify(Integer.parseInt(minConnections));
            }
        }).forContext(PUT).withMessage("Must provide a valid minimum connections range.");

        result(validationTarget().getOtherAttributes()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String maxConnections = ExtensionObjectMapper.getOtherAttribute(otherAttributes, "maxConnections");
                return new MustBeIntegerInRange(MAX_CONNECTIONS[0], MAX_CONNECTIONS[1]).verify(Integer.parseInt(maxConnections));
            }
        }).forContext(PUT).withMessage("Must provide a valid maximum connections range.");
    }
}
