package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.SharedOrNewVipVerifier;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class VirtualIpsValidator_New implements ResourceValidator<VirtualIps> {

    private final Validator<VirtualIps> validator;

    public VirtualIpsValidator_New() {
        validator = build(new ValidatorBuilder<VirtualIps>(VirtualIps.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getVirtualIps()).must().exist().withMessage("Must provide at least one virtual ip for the load balancer.");
                result(validationTarget().getVirtualIps()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide at least one virtual Ip.");
                result(validationTarget().getVirtualIps()).if_().exist().then().must().adhereTo(new SharedOrNewVipVerifier()).forContext(POST).withMessage("Must specify either a shared or new virtual ip.");
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