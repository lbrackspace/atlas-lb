package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class SslTerminationValidator implements ResourceValidator<SslTermination> {

    private final Validator<SslTermination> validator;

    public SslTerminationValidator() {
        validator = build(new ValidatorBuilder<SslTermination>(SslTermination.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Id field cannot be modified.");

                // POST EXPECTATIONS
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if (ssl.getCertificate() == null || ssl.getPrivatekey() == null) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Please include both a certificate and a key.");
            }
        });
    }

    @Override
    public ValidatorResult validate(SslTermination ssl, Object type) {
        ValidatorResult result = validator.validate(ssl, type);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<SslTermination> getValidator() {
        return validator;
    }
}
