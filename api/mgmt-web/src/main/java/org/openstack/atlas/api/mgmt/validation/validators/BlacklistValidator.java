package org.openstack.atlas.api.mgmt.validation.validators;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Blacklist;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Clusters;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class BlacklistValidator implements ResourceValidator<Blacklist> {

    private final Validator<Blacklist> validator;

    public BlacklistValidator() {
        validator = build(new ValidatorBuilder<Blacklist>(
                Blacklist.class) {

            {
                result(validationTarget().getBlacklistItems()).must().exist().withMessage("Must provide a black list.");
                result(validationTarget().getBlacklistItems()).if_().exist().then().must().haveSizeOfAtLeast(1).withMessage("Must provide at least one black list item.");
                result(validationTarget().getBlacklistItems()).must().delegateTo(new BlacklistItemValidator().getValidator(), POST);

            }
        });
    }

    @Override
    public ValidatorResult validate(Blacklist bl, Object httpRequestType) {
        ValidatorResult result = validator.validate(bl, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Blacklist> getValidator() {
        return validator;
    }
}
