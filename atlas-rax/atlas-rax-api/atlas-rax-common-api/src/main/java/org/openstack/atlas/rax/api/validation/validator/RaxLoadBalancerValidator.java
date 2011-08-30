package org.openstack.atlas.rax.api.validation.validator;

import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.api.validation.validator.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifier.IsInstanceOf;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class RaxLoadBalancerValidator implements ResourceValidator<Object> {

    private final Validator<Object> validator;

    public RaxLoadBalancerValidator() {
        validator = build(new ValidatorBuilder<Object>(Object.class) {
            {
                // POST EXPECTATIONS
                if_().adhereTo(new IsInstanceOf(AccessList.class)).then().must().delegateTo(new AccessListValidator().getValidator(), POST).forContext(POST);
            }
        });
    }

    @Override
    public ValidatorResult validate(Object object, Object type) {
        ValidatorResult result = validator.validate(object, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Object> getValidator() {
        return validator;
    }
}
