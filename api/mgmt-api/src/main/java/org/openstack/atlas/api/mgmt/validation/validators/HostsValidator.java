/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.LOADBALANCER_PUT;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.PUT;
import static org.openstack.atlas.api.mgmt.validation.contexts.HostContext.POST;
import static org.openstack.atlas.api.validation.ValidatorBuilder.build;

public class HostsValidator implements ResourceValidator<Hosts> {

    private final Validator<Hosts> validator;

    public HostsValidator() {
        validator = build(new ValidatorBuilder<Hosts>(Hosts.class) {

            {
                result(validationTarget().getHosts()).must().not().beEmptyOrNull().forContext(PUT).withMessage("Must provide at least one Host");
                result(validationTarget().getHosts()).if_().exist().then().must().delegateTo(new HostValidator().getValidator(), PUT).forContext(PUT);
                result(validationTarget().getHosts()).if_().exist().then().must().delegateTo(new HostValidator().getValidator(), POST).forContext(POST);
                result(validationTarget().getHosts()).if_().exist().then().must().delegateTo(new HostValidator().getValidator(), LOADBALANCER_PUT).forContext(LOADBALANCER_PUT);
            }
        });

    }

    @Override
    public ValidatorResult validate(Hosts hosts, Object ctx) {
        ValidatorResult result = validator.validate(hosts, ctx);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<Hosts> getValidator() {
        return validator;
    }
}
