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
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.util.ca.exceptions.ConversionException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.RsaPair;
import org.openstack.atlas.util.ca.exceptions.NoSuchAlgorithmException;

public class RSAKeyUtils {

    private static final int SB_INIT_CAPACITY = 4096;

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
        try {
            cert.verify(keyPub);
        } catch (CertificateException ex) {
            errorList.add("Error certificate signature was invalid");
        } catch (java.security.NoSuchAlgorithmException ex) {
            errorList.add("Error application does not recognize signature algo when attempting to verify signature");
        } catch (InvalidKeyException ex) {
            errorList.add("Error invalid Key when verifying signature of cert");
        } catch (NoSuchProviderException ex) {
            errorList.add("Error application does not support verification of this signature");
        } catch (SignatureException ex) {
            errorList.add("Error signature was not valid for this cert");
        }
        return errorList;
    }
}
