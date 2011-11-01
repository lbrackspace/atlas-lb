package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.builder.VirtualIpValidatorBuilder;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class VirtualIpValidator implements ResourceValidator<VirtualIp> {
    protected Validator<VirtualIp> validator;
    protected VirtualIpValidatorBuilder ruleBuilder;

    @Autowired
    public VirtualIpValidator(VirtualIpValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(VirtualIp virtualIp, Object context) {
        ValidatorResult result = validator.validate(virtualIp, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<VirtualIp> getValidator() {
        return validator;
    }
}
