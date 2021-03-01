/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstack.atlas.api.mgmt.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.validators.ResourceValidator;
import org.openstack.atlas.api.validation.validators.ValidatorUtilities;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.SslCipherProfile;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class SslCipherProfilesValidator implements ResourceValidator<SslCipherProfile> {

    private final Validator<SslCipherProfile> validator;

    public SslCipherProfilesValidator() {
        validator = build(new ValidatorBuilder<SslCipherProfile>(SslCipherProfile.class) {

            {
                result(validationTarget().getName()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide a valid cipher profile name");
                result(validationTarget().getCiphers()).must().not().beEmptyOrNull().forContext(POST).withMessage("Must provide ciphers text");
                result(validationTarget().getId()).must().not().exist().forContext(POST).withMessage("Must not include ID for this request");
            }
        });

    }

    @Override
    public ValidatorResult validate(SslCipherProfile sslCipherProfile, Object ctx) {
        ValidatorResult result = validator.validate(sslCipherProfile, ctx);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<SslCipherProfile> getValidator() {
        return validator;
    }
}
