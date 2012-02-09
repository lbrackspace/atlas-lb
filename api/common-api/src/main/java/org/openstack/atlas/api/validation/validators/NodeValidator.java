package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.openstack.atlas.api.validation.verifiers.IpAddressVerifier;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeIntegerInRange;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class NodeValidator implements ResourceValidator<Node> {

    private final Validator<Node> validator;

    public NodeValidator() {
        validator = build(new ValidatorBuilder<Node>(Node.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getAddress()).if_().exist().then().must().adhereTo(new IpAddressVerifier()).withMessage("Node ip is invalid. Please specify a valid ip.");
                result(validationTarget().getPort()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(1, 65535)).withMessage("Node port is invalid. Please specify a valid port.");
                result(validationTarget().getCondition()).if_().exist().then().must().adhereTo(new MustBeInArray(NodeCondition.values())).withMessage("Node condition is invalid. Please specify a valid condition.");
                result(validationTarget().getWeight()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(1, 100)).withMessage("Node weight is invalid. Range is 1-100. Please specify a valid weight.");
                result(validationTarget().getStatus()).must().not().exist().withMessage("Node status field cannot be modified.");
                result(validationTarget().getId()).must().not().exist().withMessage("Node id field cannot be modified.");
                must().adhereTo(new Verifier<Node>() {
                    @Override
                    public VerifierResult verify(Node node) {
                        //TODO: allow this to filter other addresses...
                        String address = node.getAddress();
                        if (address == null) {
                            return new VerifierResult(true);
                        }
                        if (IPv4ToolSet.rejectUnwantedIps(address)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Please specify a valid address.");
                must().adhereTo(new Verifier<Node>() {
                    @Override
                    public VerifierResult verify(Node node) {
                        long ipLong;
                        try {
                            ipLong = IPv4ToolSet.ip2long(node.getAddress());
                        } catch (IPStringException ex) {
                            return new VerifierResult(true); // Let the regex verifier determin if string is valid
                        } catch (NullPointerException ex) {
                            return new VerifierResult(true);   // This parameter is apparently optional.
                        }

                        if (ipLong >= 2130706432L && ipLong <= 2147483647L) {
                            return new VerifierResult(false); // ip is in the 127/8 block
                        }

                        if (ipLong == 0L || ipLong == 4294967295L) {
                            return new VerifierResult(false); // ip was 0.0.0.0 or 255.255.255.255
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Node ip is invalid. Please specify a valid ip.");

                // POST EXPECTATIONS
                result(validationTarget().getAddress()).must().exist().forContext(POST).withMessage("Must provide a valid ip for the node.");
                result(validationTarget().getPort()).must().exist().forContext(POST).withMessage("Must provide a valid port for the node.");
                result(validationTarget().getCondition()).must().exist().forContext(POST).withMessage("Must provide a valid condition for the node.");

                // PUT EXPECTATIONS
                result(validationTarget().getAddress()).must().not().exist().forContext(PUT).withMessage("Node ip field cannot be modified.");
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
    public ValidatorResult validate(Node node, Object type) {
        ValidatorResult result = validator.validate(node, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Node> getValidator() {
        return validator;
    }
}
