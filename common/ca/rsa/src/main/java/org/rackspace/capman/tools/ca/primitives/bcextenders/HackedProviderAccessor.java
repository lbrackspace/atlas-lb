package org.rackspace.capman.tools.ca.primitives.bcextenders;

import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPair;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.JDKKeyFactory;
import org.bouncycastle.jce.provider.JDKKeyFactory.RSA;

public class HackedProviderAccessor {

    public static KeyPair newKeyPair(JCERSAPrivateCrtKey jrpck) throws InvalidKeySpecException {
        PrivateKey privKey = (PrivateKey) jrpck;
        PublicKey pubKey = newJCERSAPublicKey(jrpck);
        KeyPair kp = new KeyPair(pubKey, privKey);
        return kp;
    }

    public static RSAKeyParameters newRSAKeyParameters(JCERSAPublicKey jPub) {
        RSAKeyParameters pub;

        BigInteger n = jPub.getModulus();
        BigInteger e = jPub.getPublicExponent();
        boolean isPrivate = false;

        pub = new RSAKeyParameters(isPrivate, n, e);
        return pub;
    }

    public static JCERSAPublicKey newJCERSAPublicKey(JCERSAPrivateCrtKey privKey) throws InvalidKeySpecException {
        BigInteger mod = privKey.getModulus();
        BigInteger pubExp = privKey.getPublicExponent();
        RSAPublicKeySpec rsaPubKeySpec = new RSAPublicKeySpec(mod, pubExp);
        JDKRsaFactoryExtender rsaFactory = new JDKRsaFactoryExtender();
        JCERSAPublicKey publicKey = (JCERSAPublicKey) rsaFactory.getPublicKeyFromSpec(rsaPubKeySpec);
        return publicKey;
    }
}
