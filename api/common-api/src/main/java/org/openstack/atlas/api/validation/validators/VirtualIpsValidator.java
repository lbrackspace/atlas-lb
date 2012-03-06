package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.*;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class VirtualIpsValidator implements ResourceValidator<VirtualIps> {

    private final Validator<VirtualIps> validator;

    public VirtualIpsValidator() {
        validator = build(new ValidatorBuilder<VirtualIps>(VirtualIps.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getVirtualIps()).must().exist().withMessage("Must provide at least one virtual Ip.");
                result(validationTarget().getVirtualIps()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide at least one virtual Ip.");
                result(validationTarget().getVirtualIps()).must().delegateTo(new VirtualIpValidator().getValidator(), POST);
            }
        });
    }

    @Override
    public ValidatorResult validate(VirtualIps vips, Object requestType) {
        ValidatorResult result = validator.validate(vips, requestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<VirtualIps> getValidator() {
        return validator;
    }
}
