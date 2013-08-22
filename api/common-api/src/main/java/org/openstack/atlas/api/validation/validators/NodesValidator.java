package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.api.validation.verifiers.DuplicateNodeVerifier;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.DuplicateNodeVerifier;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class NodesValidator implements ResourceValidator<Nodes> {

    private final Validator<Nodes> validator;

    public NodesValidator() {
        validator = build(new ValidatorBuilder<Nodes>(Nodes.class) {
            {
                // POST EXPECTATIONS
                //result(validationTarget().getNodes()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide at least one node"); //NOT ANY MORE!
                result(validationTarget().getNodes()).if_().exist().then().must().cannotExceedSize(25).withMessage("Must not provide more than twenty five nodes per load balancer.");
                result(validationTarget().getNodes()).if_().exist().then().must().delegateTo(new NodeValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getNodes()).must().adhereTo(new DuplicateNodeVerifier()).forContext(POST).withMessage("Duplicate nodes detected. Please ensure that the ip address and port are unique for each node.");
                //This may not need to be here, need to check the db for current nodes and their conditions...
//                result(validationTarget().getNodes()).must().adhereTo(new ActiveNodeVerifier()).forContext(POST).withMessage("Please ensure that at least one node has an ENABLED condition.");
            }
        });
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
