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

                // PUT EXPECTATIONS
                //If user supplies nothing, cert key and port must exist as bare minimum
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() == null && ssl.getPrivatekey() == null && ssl.getIntermediateCertificate() == null) && (ssl.isEnabled() == null && (ssl.isSecureTrafficOnly() == null) && ssl.getPrivatekey() == null && ssl.getSecurePort() == null)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination.");
                //If user supplies intermediateCert and nothing else
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if (ssl.getIntermediateCertificate() != null && (ssl.getCertificate() == null && ssl.getPrivatekey() == null && ssl.getSecurePort() == null && ssl.isEnabled() == null && ssl.isSecureTrafficOnly() == null )) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s) key and secure port if updating the intermediate certificate.");
//                //If user supplies cert and nothing else
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if (ssl.getCertificate() != null && (ssl.getPrivatekey() == null && ssl.getIntermediateCertificate() == null && ssl.getSecurePort() == null && ssl.isEnabled() == null && ssl.isSecureTrafficOnly() == null )) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply key and secure port if updating the certificate(s).");
//                //If user supplies key and nothing else
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if (ssl.getPrivatekey() != null && (ssl.getCertificate() == null && ssl.getIntermediateCertificate() == null && ssl.getSecurePort() == null && ssl.isEnabled() == null && ssl.isSecureTrafficOnly() == null )) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply secure port certificates and/or intermediate certificates if supplying the key.");
//                //If user supplies  cert and/  key with no securePort
                must().adhereTo(new Verifier<SslTermination>() {
                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() != null || ssl.getPrivatekey() != null) && ssl.getSecurePort() == null) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply cert and key to complete this operation.");
//                //If user supplies cert and key but not the secure port
//                must().adhereTo(new Verifier<SslTermination>() {
//                    @Override
//                    public VerifierResult verify(SslTermination ssl) {
//                        if (ssl.getCertificate() != null && ssl.getPrivatekey() != null && (ssl.getSecurePort() == null)) {
//                            return new VerifierResult(false);
//                        }
//                        return new VerifierResult(true);
//                    }
//                }).withMessage("Must supply secure port for updating cert/key credentials.");
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
