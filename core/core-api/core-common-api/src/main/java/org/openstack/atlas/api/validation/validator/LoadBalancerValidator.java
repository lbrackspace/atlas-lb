package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.LoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class LoadBalancerValidator implements ResourceValidator<LoadBalancer> {
    protected Validator<LoadBalancer> validator;
    protected ValidatorBuilder<LoadBalancer> myBuilder;

    @Autowired
    public LoadBalancerValidator(ValidatorBuilder<LoadBalancer> myBuilder) {
        this.myBuilder = myBuilder;
        validator = build(myBuilder);
    }

    @Override
    public ValidatorResult validate(LoadBalancer lb, Object httpRequestType) {
        ValidatorResult result = validator.validate(lb, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadBalancer> getValidator() {
        return validator;
    }


}
