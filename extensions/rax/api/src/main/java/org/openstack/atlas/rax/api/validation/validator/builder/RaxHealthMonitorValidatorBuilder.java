package org.openstack.atlas.rax.api.validation.validator.builder;

import org.openstack.atlas.api.validation.validator.builder.HttpMonitorValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeEmptyOrNull;
import org.openstack.atlas.api.validation.verifier.RegexValidatorVerifier;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.rax.api.mapper.dozer.converter.ExtensionObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.Map;

import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Primary
@Component
@Scope("request")
public class RaxHealthMonitorValidatorBuilder extends HttpMonitorValidatorBuilder {

    public RaxHealthMonitorValidatorBuilder() {
        super();
        //PUT EXPECTATIONS
        result(validationTarget().getOtherAttributes()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String bodyRegex = ExtensionObjectMapper.getOtherAttribute(otherAttributes, "bodyRegex");
                return new RegexValidatorVerifier().verify(bodyRegex);
            }
        }).forContext(PUT).withMessage("Must provide a valid body regex.");

        result(validationTarget().getOtherAttributes()).if_().not().adhereTo(new MustBeEmptyOrNull()).then().must().adhereTo(new Verifier<Map<QName, String>>() {
            @Override
            public VerifierResult verify(Map<QName, String> otherAttributes) {
                String statusRegex = ExtensionObjectMapper.getOtherAttribute(otherAttributes, "statusRegex");
                return new RegexValidatorVerifier().verify(statusRegex);
            }
        }).forContext(PUT).withMessage("Must provide a valid status regex.");
    }
}
