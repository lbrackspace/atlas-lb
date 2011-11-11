package org.openstack.atlas.rax.api.validation.validator.builder;

import org.openstack.atlas.api.validation.validator.builder.VirtualIpValidatorBuilder;
import org.openstack.atlas.api.validation.verifier.Ipv6VipVersionVerifier;
import org.openstack.atlas.api.validation.verifier.PublicVipTypeVerifier;
import org.openstack.atlas.api.validation.verifier.Verifier;
import org.openstack.atlas.api.validation.verifier.VerifierResult;
import org.openstack.atlas.core.api.v1.VirtualIp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.openstack.atlas.rax.api.validation.context.VirtualIpContext.POST_IPV6;

@Primary
@Component
@Scope("request")
public class RaxVirtualIpValidatorBuilder extends VirtualIpValidatorBuilder {

    public RaxVirtualIpValidatorBuilder() {
        super();

        // POST_IPV6 CONTEXT
        result(validationTarget().getId()).must().not().exist().forContext(POST_IPV6).withMessage("Cannot add a shared virtual ip.");
        must().adhereTo(new Verifier<VirtualIp>() {
            @Override
            public VerifierResult verify(VirtualIp virtualIp) {
                if (virtualIp.getId() == null && (virtualIp.getType() == null || virtualIp.getIpVersion() == null)) {
                    return new VerifierResult(false);
                }
                return new VerifierResult(true);
            }
        }).forContext(POST_IPV6).withMessage("Must specify a valid ip type and a valid ip version.");
        result(validationTarget().getType()).if_().exist().then().must().adhereTo(new PublicVipTypeVerifier()).forContext(POST_IPV6).withMessage("Must specify a valid IP type. IPv6 currently supports the 'PUBLIC' type only.");
        result(validationTarget().getIpVersion()).if_().exist().then().must().adhereTo(new Ipv6VipVersionVerifier()).forContext(POST_IPV6).withMessage("Must specify a valid IP version. Currently only IPv6 is supported for this operation.");
    }
}
