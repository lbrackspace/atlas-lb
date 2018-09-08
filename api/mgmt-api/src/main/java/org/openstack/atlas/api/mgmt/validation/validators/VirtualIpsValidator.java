package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIps;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.POST;
import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.VIPS_POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class VirtualIpsValidator implements ResourceValidator<VirtualIps> {

    private final Validator<VirtualIps> validator;

    public VirtualIpsValidator() {
        validator = build(new ValidatorBuilder<VirtualIps>(VirtualIps.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getVirtualIps()).must().exist().withMessage("Must provide a virtual IP.");
                //POST EXPECTATATIONS
                result(validationTarget().getVirtualIps()).if_().exist().then().must().haveSizeOfAtLeast(1).forContext(POST).withMessage("Must provide at least one virtual IP.");
                result(validationTarget().getVirtualIps()).must().delegateTo(new VirtualIpValidator().getValidator(), POST).forContext(POST);
                //VIPS_POST EXPECTATIONS
                result(validationTarget().getVirtualIps()).if_().exist().then().must().haveSizeOfExactly(1).forContext(VIPS_POST).withMessage("Must provide one and only one virtual IP.");
                result(validationTarget().getVirtualIps()).must().delegateTo(new VirtualIpValidator().getValidator(), VIPS_POST).forContext(VIPS_POST);
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
