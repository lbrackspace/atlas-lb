package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.DuplicateMetaVerifier;
import org.openstack.atlas.docs.loadbalancers.api.v1.Metadata;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class MetadataValidator implements ResourceValidator<Metadata> {

    private final Validator<Metadata> validator;

    public MetadataValidator() {
        validator = build(new ValidatorBuilder<Metadata>(Metadata.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getMetas()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide at least one metadata item");
                result(validationTarget().getMetas()).if_().exist().then().must().cannotExceedSize(25).withMessage("Must not provide more than twenty five metadata items per load balancer.");
                result(validationTarget().getMetas()).if_().exist().then().must().delegateTo(new MetaValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getMetas()).must().adhereTo(new DuplicateMetaVerifier()).forContext(POST).withMessage("Duplicate keys detected. Please ensure that the key is unique for each metadata item.");
            }
        });
    }

    @Override
    public ValidatorResult validate(Metadata metadata, Object context) {
        ValidatorResult result = validator.validate(metadata, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Metadata> getValidator() {
        return validator;
    }
}
