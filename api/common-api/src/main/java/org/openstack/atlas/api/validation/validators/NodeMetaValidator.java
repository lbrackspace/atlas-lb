package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMeta;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class NodeMetaValidator implements ResourceValidator<NodeMeta> {

    private final Validator<NodeMeta> validator;

    public NodeMetaValidator() {
        validator = build(new ValidatorBuilder<NodeMeta>(NodeMeta.class) {

            {
                // FULL EXPECTATIONS
                result(validationTarget().getKey()).must().exist().forContext(POST).withMessage("Must provide a key for the meta data.");
                result(validationTarget().getValue()).must().exist().forContext(POST).withMessage("Must provide a value for the meta data.");
                result(validationTarget().getId()).must().not().exist().forContext(POST).withMessage("Must not specify an id for any meta data.");
            }
        });
    }

    @Override
    public ValidatorResult validate(NodeMeta objectToValidate, Object context) {
        ValidatorResult result = validator.validate(objectToValidate, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<NodeMeta> getValidator() {
        return validator;
    }

}