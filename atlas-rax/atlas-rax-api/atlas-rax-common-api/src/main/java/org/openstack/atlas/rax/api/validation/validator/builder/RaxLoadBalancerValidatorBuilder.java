package org.openstack.atlas.rax.api.validation.validator.builder;

import org.openstack.atlas.api.validation.validator.builder.LoadBalancerValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.datamodel.Algorithm;
import org.openstack.atlas.rax.api.validation.validator.RaxLoadBalancerValidator;
import org.openstack.atlas.rax.api.validation.verifier.RaxLoadBalancerElementVerifier;
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
    public RaxLoadBalancerValidatorBuilder(Algorithm algorithm) {
        super(algorithm);

        // POST EXPECTATIONS
        result(validationTarget().getAnies()).if_().exist().then().must().delegateTo(new RaxLoadBalancerValidator().getValidator(), POST).forContext(POST);
        result(validationTarget().getOtherAttributes()).if_().exist().then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String crazyNameValue = otherAttributes.get(new QName("http://docs.openstack.org/atlas/api/v1.1/extensions/rax", "crazyName", "rax"));
                return new VerifierResult(crazyNameValue.equals("foo"));
            }
        }).forContext(POST).withMessage("'crazyName' attribute must equal foo!");
    }
}
