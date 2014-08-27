package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ZeusEvent;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class ZeusEventValidator implements ResourceValidator<ZeusEvent> {

    private final Validator<ZeusEvent> validator;

    public ZeusEventValidator() {
        validator = build(new ValidatorBuilder<ZeusEvent>(ZeusEvent.class) {
            {
                result(validationTarget().getCallbackHost()).must().exist().withMessage("Must provide a callback host.");
                result(validationTarget().getParamLine()).must().exist().withMessage("Must provide a param line.");

            }
        });
    }

    @Override
    public ValidatorResult validate(ZeusEvent zeusEvent, Object httpRequestType) {
        ValidatorResult result = validator.validate(zeusEvent, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<ZeusEvent> getValidator() {
        return validator;
    }
}
