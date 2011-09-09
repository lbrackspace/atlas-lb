package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.validator.builder.NodeValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.Node;
import org.springframework.beans.factory.annotation.Autowired;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class NodeValidator implements ResourceValidator<Node> {
    protected Validator<Node> validator;
    protected NodeValidatorBuilder validatorBuilder;

    @Autowired
    public NodeValidator(NodeValidatorBuilder validatorBuilder) {
        this.validatorBuilder = validatorBuilder;
        validator = build(validatorBuilder);
    }

    @Override
    public ValidatorResult validate(Node node, Object type) {
        ValidatorResult result = validator.validate(node, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Node> getValidator() {
        return validator;
    }
}
