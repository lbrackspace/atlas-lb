package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Node;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;



public class NodeValidator implements ResourceValidator<Node> {

    private final Validator<Node> validator;

    public NodeValidator() {validator = build(new ValidatorBuilder<Node>(Node.class) {




        });

    }

    @Override
    public ValidatorResult validate(Node node, Object httpRequestType) {
        ValidatorResult result = validator.validate(node, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Node> getValidator() {
        return validator;
    }

}
