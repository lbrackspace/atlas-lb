package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.validator.NodeValidator;
import org.openstack.atlas.api.validation.verifier.DuplicateNodeVerifier;
import org.openstack.atlas.core.api.v1.Nodes;
import org.springframework.beans.factory.annotation.Autowired;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class NodesValidatorBuilder extends ValidatorBuilder<Nodes> {
    protected final int MAX_NODES = 25;

    @Autowired
    public NodesValidatorBuilder(NodeValidatorBuilder nodeValidatorBuilder) {
        super(Nodes.class);

        // POST EXPECTATIONS
        result(validationTarget().getNodes()).must().exist().forContext(POST).withMessage("Must provide at least 1 node for the load balancer.");
        result(validationTarget().getNodes()).must().haveSizeOfAtMost(MAX_NODES).forContext(POST).withMessage(String.format("Must not provide more than %d nodes per load balancer.", MAX_NODES));
        result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator(nodeValidatorBuilder).getValidator(), POST).forContext(POST);
        result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
    }
}
