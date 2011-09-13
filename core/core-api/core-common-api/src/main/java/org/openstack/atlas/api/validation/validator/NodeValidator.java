package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.core.api.v1.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class NodeValidator implements ResourceValidator<Node> {
    protected Validator<Node> validator;
    protected ValidatorBuilder<Node> validatorBuilder;

    @Autowired
    public NodeValidator(@Qualifier("CORE") ValidatorBuilder<Node> validatorBuilder) {
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
