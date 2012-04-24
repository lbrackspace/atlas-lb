package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeMetadata;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class NodeMetadataValidator implements ResourceValidator<NodeMetadata> {

    private final Validator<NodeMetadata> validator;

    public NodeMetadataValidator() {
        validator = build(new ValidatorBuilder<NodeMetadata>(NodeMetadata.class) {

            {
                // FULL EXPECTATIONS
                result(validationTarget().getNodeMetas()).must().exist().forContext(POST).withMessage("Must provide at least 1 entry of meta data.");
            }
        });
    }

    @Override
    public ValidatorResult validate(NodeMetadata objectToValidate, Object context) {
        ValidatorResult result = validator.validate(objectToValidate, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<NodeMetadata> getValidator() {
        return validator;
    }

}