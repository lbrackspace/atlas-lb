package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustHaveLengthVerifier;
import org.openstack.atlas.docs.loadbalancers.api.v1.Meta;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class MetaValidator implements ResourceValidator<Meta> {

    private final Validator<Meta> validator;
    private final int MAX_KEY_LENGTH = 32;
    private final int MAX_VALUE_LENGTH = 256;

    public MetaValidator() {
        validator = build(new ValidatorBuilder<Meta>(Meta.class) {

            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Must not specify an id for node meta data");
                result(validationTarget().getKey()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(MAX_KEY_LENGTH)).withMessage(String.format("Node meta key must not exceed %d characters.", MAX_KEY_LENGTH));
                result(validationTarget().getValue()).if_().exist().then().must().adhereTo(new MustHaveLengthVerifier(MAX_VALUE_LENGTH)).withMessage(String.format("Node meta value must not exceed %d characters.", MAX_VALUE_LENGTH));

                // POST EXPECTATIONS
                result(validationTarget().getKey()).must().exist().forContext(POST).withMessage("Must provide a key for the node metadata item.");
                result(validationTarget().getKey()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a key for the node metadata item.");
                result(validationTarget().getValue()).must().exist().forContext(POST).withMessage("Must provide a value for the node metadata item.");
                result(validationTarget().getValue()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a value for the node metadata item.");

                // PUT EXPECTATIONS
                result(validationTarget().getKey()).must().not().exist().forContext(PUT).withMessage("Node meta key field cannot be modified.");
                result(validationTarget().getValue()).must().exist().forContext(PUT).withMessage("Must provide a value to update for the node metadata item.");
                result(validationTarget().getValue()).must().not().beEmptyOrNull().forContext(PUT).withMessage("Must provide a value for the node metadata item.");
            }
        });
    }

    @Override
    public ValidatorResult validate(Meta objectToValidate, Object context) {
        ValidatorResult result = validator.validate(objectToValidate, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Meta> getValidator() {
        return validator;
    }

}