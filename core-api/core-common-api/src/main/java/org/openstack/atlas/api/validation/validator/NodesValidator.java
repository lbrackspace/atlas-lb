package org.openstack.atlas.api.validation.validator;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.result.ValidatorResult;
import org.openstack.atlas.api.validation.validator.builder.NodesValidatorBuilder;
import org.openstack.atlas.core.api.v1.Nodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

@Component
@Scope("request")
public class NodesValidator implements ResourceValidator<Nodes> {
    protected Validator<Nodes> validator;
    protected NodesValidatorBuilder ruleBuilder;

    @Autowired
    public NodesValidator(NodesValidatorBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        validator = build(ruleBuilder);
    }

    @Override
    public ValidatorResult validate(Nodes nodes, Object httpRequestType) {
        ValidatorResult result = validator.validate(nodes, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Nodes> getValidator() {
        return validator;
    }
}
