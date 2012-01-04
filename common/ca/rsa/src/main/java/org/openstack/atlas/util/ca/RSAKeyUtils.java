package org.openstack.atlas.util.ca;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Base64;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.util.ca.exceptions.ConversionException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.RsaPair;
import org.openstack.atlas.util.ca.exceptions.NoSuchAlgorithmException;

public class RSAKeyUtils {

    private static final int SB_INIT_CAPACITY = 4096;
    private static final BigInteger m16bit = new BigInteger("ffff", 16);

    public static RsaPair genRSAPair(int bits, int certainity) throws NoSuchAlgorithmException {
        return RsaPair.getInstance(bits, certainity);
    }

    public static List<String> verifyKeyAndCert(byte[] keyPem, byte[] certPem) {
        List<String> errorList = new ArrayList<String>();

        KeyPair kp = null;
        JCERSAPublicKey certPub = null;
        JCERSAPublicKey keyPub = null;
        X509CertificateObject cert = null;
        try {
            kp = (KeyPair) PemUtils.fromPem(keyPem);
            keyPub = (JCERSAPublicKey) kp.getPublic();
        } catch (PemException ex) {
            errorList.add("Error decoding Key from Pem Data");
        } catch (ClassCastException ex) {
            errorList.add("Error key Pem Data did not decode to an RSA Private Key");
        }
        try {
            cert = (X509CertificateObject) PemUtils.fromPem(certPem);
            certPub = (JCERSAPublicKey) cert.getPublicKey();
        } catch (PemException ex) {
            errorList.add("Error decoding Cert from Pem Data");
        } catch (ClassCastException ex) {
            errorList.add("Error cert Pem data did not decode to an RSA Private Key");
        }

        if (kp == null || cert == null) {
            return errorList;
        }

        if (!certPub.getModulus().equals(keyPub.getModulus())) {
            errorList.add("Error cert and key Modulus mismatch");
        }

        if (!certPub.getPublicExponent().equals(keyPub.getPublicExponent())) {
            errorList.add("Error cert and key public exponents mismatch");
        }
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException ex) {
            errorList.add("Error cert Expired");
        } catch (CertificateNotYetValidException ex) {
            errorList.add("Error cert not yet valid");
        }

        return errorList;
    }

    public static String shortPub(Object obj) {
        String out = null;
        BigInteger n;
        BigInteger e;
        if (obj instanceof JCERSAPublicKey) {
            JCERSAPublicKey jk = (JCERSAPublicKey) obj;
            n = jk.getModulus().mod(m16bit);
            e = jk.getPublicExponent();
            return String.format("(%s,%s)", e, n);
        } else if (obj instanceof RSAKeyParameters) {
            RSAKeyParameters rp = (RSAKeyParameters) obj;
            n = rp.getModulus().mod(m16bit);
            e = rp.getExponent();
            return String.format("(%s,%s)", e, n);
        } else if (obj instanceof RSAPublicKeyStructure) {
            RSAPublicKeyStructure rs = (RSAPublicKeyStructure) obj;
            n = rs.getModulus().mod(m16bit);
            e = rs.getPublicExponent();
            return String.format("(%s,%s)",e,n);
        } else {
            return String.format("(%s,%s)", "None", "None");
        }
    }
}
