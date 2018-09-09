package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.mgmt.validation.verifiers.SharedOrNewVipVerifier;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.verifiers.MustBeInArray;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.mgmt.validation.contexts.VirtualIpContext.*;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class VirtualIpValidator implements ResourceValidator<VirtualIp> {

    private final Validator<VirtualIp> validator;

    public VirtualIpValidator() {
        validator = build(new ValidatorBuilder<VirtualIp>(VirtualIp.class) {
            {
                // POST EXPECTATIONS
                result(validationTarget().getAddress()).must().exist().forContext(POST).withMessage("Must provide a valid IP address for the virtual IP.");
                result(validationTarget().getAddress()).if_().exist().then().must().match(ValidatorUtilities.IPV4_REGEX).withMessage("Must provide a valid IP address.");

                // SHARED EXPECTATIONS
                result(validationTarget().getType()).must().exist().then().must().adhereTo(new MustBeInArray(VipType.values())).forContexts(new Object[]{POST, PUT}).withMessage("Virtual ip type is invalid. Please specify a valid type.");
                result(validationTarget().getLoadBalancerId()).must().not().exist().forContexts(new Object[]{POST, PUT}).withMessage("Must not include the LoadBalancerId.");
                result(validationTarget().getId()).must().not().exist().forContexts(new Object[]{POST, PUT}).withMessage("Must not provide a Virtual IP Id.");
                result(validationTarget().getClusterId()).must().not().exist().forContexts(new Object[]{POST, PUT}).withMessage("Must not provide a valid ClusterId.");

                // IMMUTABLE FOR VIRTUALIP POST CONTEXT
                result(validationTarget().getAddress()).must().not().exist().forContext(VIPS_POST).withMessage("Must not provide an IP address for a Load balancer virtual IP.");
                result(validationTarget().getTicket()).must().exist().forContext(VIPS_POST).withMessage("Must provide a ticket for a Load balancer virtual IP.");
                result(validationTarget().getTicket()).must().delegateTo(new TicketValidator().getValidator(), HttpRequestType.POST).forContext(VIPS_POST);
                must().adhereTo(new SharedOrNewVipVerifier()).forContext(VIPS_POST).withMessage("Must specify either a shared or new virtual ip.");
            }
        });
    }

    @Override
    public ValidatorResult validate(VirtualIp vip, Object ctx) {
        ValidatorResult result = validator.validate(vip, ctx);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<VirtualIp> getValidator() {
        return validator;
    }
}
