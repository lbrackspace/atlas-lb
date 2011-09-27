package org.openstack.atlas.rax.api.validation.validator.builder;

import org.openstack.atlas.api.validation.validator.builder.*;
import org.openstack.atlas.api.validation.verifier.MustBeEmptyOrNull;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.datamodel.AlgorithmType;
import org.openstack.atlas.datamodel.ProtocolType;
import org.openstack.atlas.rax.api.mapper.dozer.converter.ExtensionObjectMapper;
import org.openstack.atlas.rax.api.validation.validator.RaxLoadBalancerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@Primary
@Component
@Scope("request")
public class RaxLoadBalancerValidatorBuilder extends LoadBalancerValidatorBuilder {

    @Autowired
    public RaxLoadBalancerValidatorBuilder(AlgorithmType algorithmType,
                                           ProtocolType protocolType,
                                           NodeValidatorBuilder nodeValidatorBuilder,
                                           VirtualIpValidatorBuilder virtualIpValidatorBuilder,
                                           HealthMonitorValidatorBuilder healthMonitorValidatorBuilder,
                                           ConnectionThrottleValidatorBuilder connectionThrottleValidatorBuilder,
                                           SessionPersistenceValidatorBuilder sessionPersistenceValidatorBuilder) {
        super(algorithmType, protocolType, nodeValidatorBuilder, virtualIpValidatorBuilder, healthMonitorValidatorBuilder, connectionThrottleValidatorBuilder, sessionPersistenceValidatorBuilder);

        // POST EXPECTATIONS
        result(validationTarget().getAnies()).if_().exist().then().must().delegateTo(new RaxLoadBalancerValidator().getValidator(), POST).forContext(POST);
        result(validationTarget().getOtherAttributes()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String crazyNameValue = ExtensionObjectMapper.getOtherAttribute(otherAttributes, "crazyName");
                return new VerifierResult(crazyNameValue.equals("foo"));
            }
        }).forContext(POST).withMessage("'crazyName' attribute must equal foo!");
    }
}
