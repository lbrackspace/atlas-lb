package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.IpAddressVerifier;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.Node;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.NodeCondition;
import org.openstack.atlas.service.domain.entities.NodeStatus;
import org.openstack.atlas.service.domain.entities.NodeType;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;


public class NodeValidator implements ResourceValidator<Node> {

    private final Validator<Node> validator;

    public NodeValidator() {validator = build(new ValidatorBuilder<Node>(Node.class) {
        {
            //SHARED Expectations
            result(validationTarget().getType()).if_().exist().then().must().adhereTo(new Verifier<String>() {
                @Override
                public VerifierResult verify(String obj) {
                    VerifierResult result = new VerifierResult(false);
                    for (NodeType type : NodeType.values()) {
                        try {
                            result = new VerifierResult(type.equals(NodeType.valueOf(obj)));
                        } catch (Exception e) {
                            return result;
                        }
                        if (result.passed()) {
                            break;
                        }
                    }
                    return result;
                }
            }).forContext(PUT).withMessage("Must provide a valid nodeType");

            result(validationTarget().getStatus()).if_().exist().then().must().adhereTo(new Verifier<String>() {
                @Override
                public VerifierResult verify(String obj) {
                    VerifierResult result = new VerifierResult(false);
                    for (NodeStatus status : NodeStatus.values()) {
                        try {
                            result = new VerifierResult(status.equals(NodeStatus.valueOf(obj)));
                        } catch (Exception e) {
                            return result;
                        }
                        if (result.passed()) {
                            break;
                        }
                    }
                    return result;
                }
            }).forContext(PUT).withMessage("Must provide a valid nodeStatus");

            result(validationTarget().getCondition()).if_().exist().then().must().adhereTo(new Verifier<String>() {
                @Override
                public VerifierResult verify(String obj) {
                    VerifierResult result = new VerifierResult(false);
                    for (NodeCondition condition : NodeCondition.values()) {
                        try {
                            result = new VerifierResult(condition.equals(NodeCondition.valueOf(obj)));
                        } catch (Exception e) {
                            return result;
                        }
                        if (result.passed()) {
                            break;
                        }
                    }
                    return result;
                }
            }).forContext(PUT).withMessage("Must provide a valid nodeCondition");


            //PUT Expectations
            result(validationTarget().getId()).must().not().exist().forContext(PUT).withMessage("Node id field cannot be modified.");
            result(validationTarget().getAddress()).must().not().exist().forContext(HttpRequestType.PUT).withMessage("Node ip field cannot be modified.");
            result(validationTarget().getPort()).must().not().exist().forContext(PUT).withMessage("Port field cannot be modified.");
            must().adhereTo(new Verifier<Node>() {
                @Override
                public VerifierResult verify(Node node) {
                    return new VerifierResult(node.getCondition() != null || node.getWeight() != null || node.getType() != null);
                }
            }).forContext(PUT).withMessage("The node must have at least one of the following to update: condition, weight, type.");
        }
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
