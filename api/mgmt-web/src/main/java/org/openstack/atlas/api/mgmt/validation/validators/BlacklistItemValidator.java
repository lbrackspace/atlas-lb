package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistItem;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.BlacklistType;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;
import static org.openstack.atlas.util.ip.IPUtils.isValidIpv4Subnet;
import static org.openstack.atlas.util.ip.IPUtils.isValidIpv6Subnet;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;


public class BlacklistItemValidator implements ResourceValidator<BlacklistItem> {

    private final Validator<BlacklistItem> validator;

    public BlacklistItemValidator() {
        validator = build(new ValidatorBuilder<BlacklistItem>(
                BlacklistItem.class) {

            {
                // SHARED EXPECTATIONS
                //ipv6 validation here..
                result(validationTarget().getId()).must().not().exist().withMessage("Must not provide an id for this request.");
                result(validationTarget().getType()).if_().exist().then().must().adhereTo(new MustBeInArray(BlacklistType.values())).withMessage("Blacklist type is invalid. Please specify a valid type.");
                result(validationTarget().getIpVersion()).must().exist().then().must().adhereTo(new MustBeInArray(IpVersion.values())).withMessage("Blacklist ip version is invalid. Please specify a valid version.");
                result(validationTarget().getCidrBlock()).must().exist().withMessage("Must provide an ip address to blacklist");
                must().adhereTo(new Verifier<BlacklistItem>() {

                    @Override
                    public VerifierResult verify(BlacklistItem bli) {
                        String ip;
                        ip = bli.getCidrBlock();
                        if(bli == null || ip == null) return new VerifierResult(true); // Its some other validators job to reject null;

                        switch(bli.getIpVersion()) {
                            case IPV4:
                                return new VerifierResult(isValidIpv4Subnet(ip));
                            case IPV6:
                                return new VerifierResult(isValidIpv6Subnet(ip));
                            default:
                                return new VerifierResult(true); // Again the IpVersion is validated in another expectation
                        }
                    }
                }).withMessage("Ip address must be a valid Cidr for the given ip version");
            }
        });
    }

    @Override
    public ValidatorResult validate(BlacklistItem blitem, Object httpRequestType) {
        ValidatorResult result = validator.validate(blitem, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<BlacklistItem> getValidator() {
        return validator;
    }
}

