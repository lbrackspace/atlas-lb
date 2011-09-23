package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.validator.NodeValidator;
import org.openstack.atlas.api.validation.verifier.DuplicateNodeVerifier;
import org.openstack.atlas.core.api.v1.Nodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@Component
@Scope("request")
public class NodesValidatorBuilder extends ValidatorBuilder<Nodes> {
    protected final int MIN_NODES = 1;
    protected final int MAX_NODES = 25;

    @Autowired
    public NodesValidatorBuilder(NodeValidatorBuilder nodeValidatorBuilder) {
        super(Nodes.class);

        // POST EXPECTATIONS
        result(validationTarget().getNodes()).must().haveSizeOfAtLeast(MIN_NODES).forContext(POST).withMessage(String.format("Must provide at least %d node(s).", MIN_NODES));
        result(validationTarget().getNodes()).must().haveSizeOfAtMost(MAX_NODES).forContext(POST).withMessage(String.format("Must not provide more than %d nodes.", MAX_NODES));
        result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
        result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator(nodeValidatorBuilder).getValidator(), POST).forContext(POST);
    }
}
