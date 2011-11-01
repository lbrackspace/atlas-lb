package org.openstack.atlas.api.validation.validator.builder;

import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.MustBeInArray;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.core.api.v1.IpVersion;
import org.openstack.atlas.core.api.v1.VipType;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

@Component
@Scope("request")
public class VirtualIpValidatorBuilder extends ValidatorBuilder<VirtualIp> {

    public VirtualIpValidatorBuilder() {
        super(VirtualIp.class);

        // SHARED EXPECTATIONS
        result(validationTarget().getAddress()).must().not().exist().withMessage("Virtual ip address field cannot be modified.");
        result(validationTarget().getIpVersion()).if_().exist().then().must().adhereTo(new MustBeInArray(IpVersion.values())).withMessage("Must specify a valid IP version.");
        result(validationTarget().getType()).if_().exist().then().must().adhereTo(new MustBeInArray(VipType.values())).withMessage("Must specify a valid IP type.");
        must().adhereTo(new Verifier<VirtualIp>() {
            @Override
            public VerifierResult verify(VirtualIp virtualIp) {
                return new VerifierResult(virtualIp.getId() != null || virtualIp.getType() != null || virtualIp.getIpVersion() != null);
            }
        }).withMessage("The virtual ip must have at least one of the following specified: id, type, ipVersion.");

        // POST CONTEXT
        must().adhereTo(new Verifier<VirtualIp>() {
            @Override
            public VerifierResult verify(VirtualIp virtualIp) {
                if (virtualIp.getId() != null && (virtualIp.getType() != null || virtualIp.getIpVersion() != null)) {
                    return new VerifierResult(false);
                }
                return new VerifierResult(true);
            }
        }).forContext(POST).withMessage("If sharing a virtual ip please only specify the id.");
    }
}
