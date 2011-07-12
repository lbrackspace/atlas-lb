package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import org.openstack.atlas.api.validation.context.NetworkItemContext;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class AccessListValidator implements ResourceValidator<AccessList> {

    private final Validator<AccessList> validator;

    public AccessListValidator() {
        validator = build(new ValidatorBuilder<AccessList>(AccessList.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getNetworkItems()).must().not().beEmptyOrNull().withMessage("Must provide at least one network item to the access list.");
                result(validationTarget().getNetworkItems()).if_().exist().then().must().cannotExceedSize(100).withMessage("Must not provide more than one hundred network items for the access list.");

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
