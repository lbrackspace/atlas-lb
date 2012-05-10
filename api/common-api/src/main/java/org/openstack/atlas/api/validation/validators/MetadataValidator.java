package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.DuplicateLoadbalancerMetaVerifier;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadbalancerMetadata;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class MetadataValidator implements ResourceValidator<LoadbalancerMetadata> {

    private final Validator<LoadbalancerMetadata> validator;

    public MetadataValidator() {
        validator = build(new ValidatorBuilder<LoadbalancerMetadata>(LoadbalancerMetadata.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getLoadbalancerMetas()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide at least one metadata item");
                result(validationTarget().getLoadbalancerMetas()).if_().exist().then().must().cannotExceedSize(25).withMessage("Must not provide more than twenty five metadata items per load balancer.");
                result(validationTarget().getLoadbalancerMetas()).if_().exist().then().must().delegateTo(new LoadbalancerMetaValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getLoadbalancerMetas()).must().adhereTo(new DuplicateLoadbalancerMetaVerifier()).forContext(POST).withMessage("Duplicate keys detected. Please ensure that the key is unique for each metadata item.");
            }
        });
    }

    @Override
    public ValidatorResult validate(LoadbalancerMetadata metadata, Object context) {
        ValidatorResult result = validator.validate(metadata, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadbalancerMetadata> getValidator() {
        return validator;
    }
}
