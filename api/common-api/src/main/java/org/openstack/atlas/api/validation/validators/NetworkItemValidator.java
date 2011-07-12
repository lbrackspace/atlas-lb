package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.api.validation.context.NetworkItemContext;
import org.openstack.atlas.api.validation.util.IPString.IPUtils;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.validation.context.NetworkItemContext.FULL;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class NetworkItemValidator implements ResourceValidator<NetworkItem> {

    private final Validator<NetworkItem> validator;

    public NetworkItemValidator() {
        validator = build(new ValidatorBuilder<NetworkItem>(NetworkItem.class) {

            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Network item id field cannot be modified.");
                result(validationTarget().getType()).if_().exist().then().must().adhereTo(new MustBeInArray(NetworkItemType.values())).withMessage("Network item type is invalid. Please specify a valid network item type.");

                must().adhereTo(new Verifier<NetworkItem>() {

                    @Override
                    public VerifierResult verify(NetworkItem ni) {
                        String ipStr = ni.getAddress();
                        VerifierResult passed = new VerifierResult(true);
                        VerifierResult failed = new VerifierResult(false);
                        if (ipStr == null) {
                            return passed;
                        }
                        if(IPUtils.isValidIpv4String(ipStr)) {
                            return passed;
                        }

                        if(IPUtils.isValidIpv4Subnet(ipStr)) {
                            return passed;
                        }
                        if(IPUtils.isValidIpv6String(ipStr)) {
                            return passed;
                        }
                        if(IPUtils.isValidIpv6Subnet(ipStr)) {
                            return passed;
                        }

                        return failed;

                    }
                }).withMessage("Network Address must be valid Ip Address");

                must().adhereTo(new Verifier<NetworkItem>() {
                    @Override
                    public VerifierResult verify(NetworkItem nwi) {
                        String address = nwi.getAddress();
                        if(address == null) {
                            return new VerifierResult(true);
                        }
                        if(IPv4ToolSet.rejectUnwantedIps(address)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Please specify a valid network item address.");


                // FULL EXPECTATIONS
                result(validationTarget().getAddress()).must().exist().forContext(FULL).withMessage("Must provide an address for the network item.");
                result(validationTarget().getIpVersion()).must().not().exist().forContext(FULL).withMessage("The ip version field cannot be modified.");
                result(validationTarget().getType()).must().exist().forContext(FULL).withMessage("Must provide a valid type for the network item.");


                // PARTIAL EXPECTATIONS
                must().adhereTo(new Verifier<NetworkItem>() {

                    @Override
                    public VerifierResult verify(NetworkItem ni) {
                        return new VerifierResult((ni.getAddress() != null || ni.getType() != null));
                    }
                }).forContext(NetworkItemContext.PARTIAL).withMessage("The network item must have at least one of the following to update: ip address and/or type.");
            }
        });
    }

    @Override
    public ValidatorResult validate(NetworkItem objectToValidate, Object context) {
        ValidatorResult result = validator.validate(objectToValidate, context);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<NetworkItem> getValidator() {
        return validator;
    }
}
