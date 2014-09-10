package org.openstack.atlas.api.validation.validators;

import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class CertificateMappingValidator implements ResourceValidator<CertificateMapping> {

    private final Validator<CertificateMapping> validator;

    public CertificateMappingValidator() {
        validator = build(new ValidatorBuilder<CertificateMapping>(CertificateMapping.class) {
            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Certificate mapping id field cannot be modified.");
                result(validationTarget().getPrivateKey()).if_().exist().then().must().adhereTo(new PrivateKeyVerifier()).withMessage("Private key is invalid. Please provide a valid private key.");
                result(validationTarget().getCertificate()).if_().exist().then().must().adhereTo(new CertificateVerifier()).withMessage("Certificate is invalid. Please provide a valid certificate.");
                result(validationTarget().getIntermediateCertificate()).if_().exist().then().must().adhereTo(new IntermediateCertificateVerifier()).withMessage("Intermediate certificate is invalid. Please provide a valid intermediate certificate.");
                result(validationTarget().getHostName()).if_().exist().then().must().adhereTo(new HostNameVerifier()).withMessage("Host name is invalid. Please provide a valid host name.");

                // POST EXPECTATIONS
                result(validationTarget().getPrivateKey()).must().exist().forContext(POST).withMessage("Must provide a private key for the certificate mapping.");
                result(validationTarget().getCertificate()).must().exist().forContext(POST).withMessage("Must provide a certificate for the certificate mapping.");
                result(validationTarget().getHostName()).must().exist().forContext(POST).withMessage("Must provide a host name for the certificate mapping.");

                // PUT EXPECTATIONS
                must().adhereTo(new Verifier<CertificateMapping>() {
                    @Override
                    public VerifierResult verify(CertificateMapping obj) {
                        return new VerifierResult(obj.getCertificate() != null || obj.getPrivateKey() != null || obj.getIntermediateCertificate() != null || obj.getHostName() != null);
                    }
                }).forContext(PUT).withMessage("The certificate mapping must have at least one of the following to update: certificate, privateKey, intermediateCertificate, or hostName.");
            }
        });
    }

    @Override
    public ValidatorResult validate(CertificateMapping certificateMapping, Object httpRequestType) {
        ValidatorResult result = validator.validate(certificateMapping, httpRequestType);
        return ValidatorUtilities.removeEmptyMessages(result);
    }

    @Override
    public Validator<CertificateMapping> getValidator() {
        return validator;
    }
}
