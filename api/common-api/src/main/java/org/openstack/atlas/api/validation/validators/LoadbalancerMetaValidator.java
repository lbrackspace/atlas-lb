package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustHaveLengthVerifier;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadbalancerMeta;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class LoadbalancerMetaValidator implements ResourceValidator<LoadbalancerMeta> {

    private final Validator<LoadbalancerMeta> validator;
    private final int MAX_KEY_LENGTH = 32;
    private final int MAX_VALUE_LENGTH = 256;

    public LoadbalancerMetaValidator() {
        validator = build(new ValidatorBuilder<LoadbalancerMeta>(LoadbalancerMeta.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("LoadbalancerMeta id cannot be modified.");
                result(validationTarget().getKey()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(MAX_KEY_LENGTH)).withMessage(String.format("LoadbalancerMeta key must not exceed %d characters.", MAX_KEY_LENGTH));
                result(validationTarget().getValue()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(MAX_VALUE_LENGTH)).withMessage(String.format("LoadbalancerMeta value must not exceed %d characters.", MAX_VALUE_LENGTH));

                // POST EXPECTATIONS
                result(validationTarget().getKey()).must().exist().forContext(POST).withMessage("Must provide a key for the metadata item.");
                result(validationTarget().getKey()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a key for the metadata item.");
                result(validationTarget().getValue()).must().exist().forContext(POST).withMessage("Must provide a value for the metadata item.");
                result(validationTarget().getValue()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a value for the metadata item.");

                // PUT EXPECTATIONS
                result(validationTarget().getKey()).must().not().exist().forContext(PUT).withMessage("LoadbalancerMeta key field cannot be modified.");
                result(validationTarget().getValue()).must().exist().forContext(PUT).withMessage("Must provide a value to update for the metadata item.");
                result(validationTarget().getValue()).must().not().beEmptyOrNull().forContext(PUT).withMessage("Must provide a value for the metadata item.");
            }
        });
    }

    @Override
    public ValidatorResult validate(LoadbalancerMeta meta, Object context) {
        ValidatorResult result = validator.validate(meta, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<LoadbalancerMeta> getValidator() {
        return validator;
    }
}
