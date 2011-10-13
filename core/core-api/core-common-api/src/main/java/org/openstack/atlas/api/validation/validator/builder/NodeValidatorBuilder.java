package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.*;
import org.openstack.atlas.common.ip.IPv4ToolSet;
import org.openstack.atlas.common.ip.exception.IPStringException;
import org.openstack.atlas.core.api.v1.Node;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

@Component
@Scope("request")
public class NodeValidatorBuilder extends ValidatorBuilder<Node> {
    protected final int MIN_PORT = 1;
    protected final int MAX_PORT = 65535;
    protected final int MIN_WEIGHT = 1;
    protected final int MAX_WEIGHT = 100;

    public NodeValidatorBuilder() {
        super(Node.class);

        // SHARED EXPECTATIONS
        result(validationTarget().getAddress()).if_().exist().then().must().adhereTo(new IpAddressVerifier()).withMessage("Node ip is invalid. Please specify a valid ip.");
        result(validationTarget().getPort()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MIN_PORT, MAX_PORT)).withMessage("Node port is invalid. Please specify a valid port.");
        result(validationTarget().getWeight()).if_().exist().then().must().adhereTo(new MustBeIntegerInRange(MIN_WEIGHT, MAX_WEIGHT)).withMessage("Node weight is invalid. Range is 1-100. Please specify a valid weight.");
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

        // PUT EXPECTATIONS
        result(validationTarget().getAddress()).must().not().exist().forContext(PUT).withMessage("Node ip field cannot be modified.");
        result(validationTarget().getPort()).must().not().exist().forContext(PUT).withMessage("Port field cannot be modified.");
        must().adhereTo(new Verifier<Node>() {
            @Override
            public VerifierResult verify(Node node) {
                return new VerifierResult(node.isEnabled() != null || node.getWeight() != null);
            }
        }).forContext(PUT).withMessage("The node must have at least one of the following to update: enabled, weight.");
    }
}
