package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.api.validation.verifiers.Ipv6VipVersionVerifier;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.verifiers.PublicVipTypeVerifier;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.ServicenetVip6TypeVerifier;
import org.openstack.atlas.api.validation.verifiers.Verifier;
import org.openstack.atlas.api.validation.verifiers.VerifierResult;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.VirtualIpContext.POST_IPV6;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class VirtualIpValidator implements ResourceValidator<VirtualIp> {

    private final Validator<VirtualIp> validator;

    public VirtualIpValidator() {
        validator = build(new ValidatorBuilder<VirtualIp>(VirtualIp.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getAddress()).must().not().exist().withMessage("Virtual ip address field cannot be modified.");
                must().adhereTo(new Verifier<VirtualIp>() {
                    @Override
                    public VerifierResult verify(VirtualIp virtualIp) {
                        return new VerifierResult(virtualIp.getId() != null || virtualIp.getType() != null || virtualIp.getIpVersion() != null);
                    }
                }).withMessage("The virtual ip must have at least one of the following specified: id, type, ipVersion.");

                // POST CONTEXT (LOADBALANCER CREATE)
                must().adhereTo(new ServicenetVip6TypeVerifier()).forContext(POST).withMessage("Must specify a valid IP type. IPv6 supports the 'PUBLIC' type only.");
                must().adhereTo(new Verifier<VirtualIp>() {
                    @Override
                    public VerifierResult verify(VirtualIp virtualIp) {
                        if (virtualIp.getId() != null && (virtualIp.getType() != null || virtualIp.getIpVersion() != null)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).forContext(POST).withMessage("If sharing a virtual ip please only specify the id.");
                must().adhereTo(new Verifier<VirtualIp>() {
                    @Override
                    public VerifierResult verify(VirtualIp virtualIp) {
                        if (virtualIp.getId() != null && (virtualIp.getType() != null || virtualIp.getIpVersion() != null)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).forContext(POST_IPV6).withMessage("If sharing a virtual ip please only specify the id.");
                must().adhereTo(new Verifier<VirtualIp>() {
                    @Override
                    public VerifierResult verify(VirtualIp virtualIp) {
                        if (virtualIp.getId() == null && virtualIp.getType() == null) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).forContext(POST_IPV6).withMessage("Must specify a valid ip type");
                result(validationTarget().getIpVersion()).if_().exist().then().must().adhereTo(new MustBeInArray(IpVersion.values())).withMessage("Must specify a valid IP version.");


                // VIRTUAL IP POST CONTEXT (PUBLIC ADD VIP)
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
                result(validationTarget().getType()).if_().exist().then().must().adhereTo(new PublicVipTypeVerifier()).forContext(POST_IPV6).withMessage("Must specify a valid IP type. IPv6 supports the 'PUBLIC' type only.");
                result(validationTarget().getIpVersion()).if_().exist().then().must().adhereTo(new Ipv6VipVersionVerifier()).forContext(POST_IPV6).withMessage("Must specify a valid IP version, currently only IPv6 is supported for this operation.");
            }
        });
    }

    @Override
    public ValidatorResult validate(VirtualIp virtualIp, Object httpRequestType) {
        ValidatorResult result = validator.validate(virtualIp, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<VirtualIp> getValidator() {
        return validator;
    }
}
