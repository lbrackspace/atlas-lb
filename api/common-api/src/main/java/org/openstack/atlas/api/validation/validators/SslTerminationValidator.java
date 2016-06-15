package org.openstack.atlas.api.validation.validators;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolName;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.api.validation.Validator;
import org.openstack.atlas.api.validation.ValidatorBuilder;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.validation.verifiers.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet;
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringException;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

import static org.openstack.atlas.api.validation.ValidatorBuilder.build;
import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;
import static org.openstack.atlas.api.validation.context.HttpRequestType.PUT;

public class SslTerminationValidator implements ResourceValidator<SslTermination> {

    private final Validator<SslTermination> validator;
    private String errMessage = "";

    public SslTerminationValidator() {
        validator = build(new ValidatorBuilder<SslTermination>(SslTermination.class) {

            {
                // SHARED EXPECTATIONS
                result(validationTarget().getId()).must().not().exist().withMessage("Id field cannot be modified.");

                // PUT EXPECTATIONS
                //If user supplies nothing, cert key and port or security protocols must exist as bare minimum
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() == null || ssl.getPrivatekey() == null || ssl.getIntermediateCertificate() == null) && ((ssl.isEnabled() != null && (ssl.isSecureTrafficOnly() != null) && ssl.getIntermediateCertificate() != null && ssl.getSecurePort() != null))) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination.");

                //If user supplies nothing, maybe some attrs.. cert key and port must exist as bare minimum
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() == null && ssl.getPrivatekey() == null && ssl.getIntermediateCertificate() == null) && (ssl.isEnabled() == null && (ssl.isSecureTrafficOnly() == null) && !hasSecurityProtocols(ssl) && ssl.getIntermediateCertificate() == null && ssl.getSecurePort() == null)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination.");

                //If cert or key is null, and no other attrs are present
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() == null || ssl.getPrivatekey() == null && ssl.getIntermediateCertificate() == null) && (ssl.isEnabled() == null && (ssl.isSecureTrafficOnly() == null) && ssl.getSecurePort() == null && !hasSecurityProtocols(ssl))) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination.");

                //If cert is present then a key must be present as well and vice versa XOR
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() != null || ssl.getPrivatekey() != null)) {
                            if (ssl.getCertificate() == null || ssl.getPrivatekey() == null) {
                                return new VerifierResult(false);
                            }
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination cert and or key values.");

                //If cert/key null and icert !null, other attrs are present
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if (ssl.getIntermediateCertificate() != null) {
                            if (ssl.getCertificate() == null || ssl.getPrivatekey() == null) {
                                return new VerifierResult(false);
                            }
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination cert and or key values.");

                //If cert or key and secure port is null, and no other attrs are present
                must().adhereTo(new Verifier<SslTermination>() {

                    @Override
                    public VerifierResult verify(SslTermination ssl) {
                        if ((ssl.getCertificate() != null && ssl.getPrivatekey() != null && ssl.getSecurePort() == null)) {
                            return new VerifierResult(false);
                        }
                        return new VerifierResult(true);
                    }
                }).withMessage("Must supply certificates(s), key and secure port for updating ssl termination.");

                // Don't allow duplicate Security Protocols to be listed
                {

                    must().adhereTo(new Verifier<SslTermination>() {

                        @Override
                        public VerifierResult verify(SslTermination ssl) {
                            boolean isSuccess = true;
                            List<String> dups = new ArrayList<String>();
                            Map<SecurityProtocolName, Integer> names = new HashMap<SecurityProtocolName, Integer>();
                            for (SecurityProtocol sp : ssl.getSecurityProtocols()) {
                                SecurityProtocolName protName = sp.getSecurityProtocolName();
                                if (!names.containsKey(protName)) {
                                    names.put(protName, 0);
                                }
                                int n = names.get(protName);
                                n++;
                                names.put(protName, n);
                            }
                            for (Map.Entry<SecurityProtocolName, Integer> e : names.entrySet()) {
                                SecurityProtocolName name = e.getKey();
                                int n = e.getValue();
                                if (n > 1) {
                                    isSuccess = false;
                                    dups.add(name.value());
                                }
                            }
                            errMessage = String.format("Found duplicate Security protocols: %s", StaticStringUtils.collectionToString(dups, ","));
                            return new VerifierResult(isSuccess);
                        }
                    }).withMessage(errMessage);
                }
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

    private boolean hasSecurityProtocols(SslTermination ssl) {
        boolean out = ssl.getSecurityProtocols().size() > 0;
        return out;
    }
}
