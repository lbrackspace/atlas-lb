package org.openstack.atlas.rax.api.validation.validator;

import org.openstack.atlas.api.v1.extensions.rax.AccessList;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.context.NetworkItemContext;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.ResourceValidator;
import org.openstack.atlas.api.validation.validator.ValidatorUtilities;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Primary
@Component
@Scope("request")
public class AccessListValidator implements ResourceValidator<AccessList> {

    private final Validator<AccessList> validator;

    public AccessListValidator() {
        validator = build(new ValidatorBuilder<AccessList>(AccessList.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getNetworkItems()).must().not().beEmptyOrNull().withMessage("Must provide at least one network item to the access list.");
                result(validationTarget().getNetworkItems()).if_().exist().then().must().haveSizeOfAtMost(100).withMessage("Must not provide more than one hundred network items for the access list.");

                // FULL EXPECTATIONS
                result(validationTarget().getNetworkItems()).must().delegateTo(new NetworkItemValidator().getValidator(), NetworkItemContext.FULL);
            }
        });
    }

    @Override
    public ValidatorResult validate(AccessList accessList, Object httpRequestType) {
        ValidatorResult result = validator.validate(accessList, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<AccessList> getValidator() {
        return validator;
    }
}
