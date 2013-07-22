

package org.rackspace.capman.tools.ca.primitives.bcextenders;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import org.bouncycastle.jce.provider.JCERSAPublicKey;

public class JDKRsaFactoryExtender extends org.bouncycastle.jce.provider.JDKKeyFactory.RSA{
    public JCERSAPublicKey getPublicKeyFromSpec(KeySpec ks) throws InvalidKeySpecException{
        return (JCERSAPublicKey) engineGeneratePublic(ks);
    }

}
