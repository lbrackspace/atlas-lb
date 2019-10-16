package org.openstack.atlas.util.ca;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.math.BigInteger;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.openstack.atlas.util.ca.exceptions.NotAnRSAKeyException;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.Debug;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.primitives.bcextenders.HackedProviderAccessor;
import sun.security.rsa.RSAPublicKeyImpl;

public class RSAKeyUtils {

    public static final int DEFAULT_KEY_SIZE = 2048;

    static {
        RsaConst.init();
    }
    private static final BigInteger m16bit = new BigInteger("10001", 16);

    public static List<String> verifyKeyAndCert(KeyPair kp, X509CertificateHolder cert) {
        List<String> errorList = new ArrayList<String>();
        BCRSAPublicKey certPub = null;
        BCRSAPublicKey keyPub = null;
        try {
            Object obj = kp.getPublic();
            String objInfo = Debug.classLoaderInfo(obj.getClass());
            String jpkInfo = Debug.classLoaderInfo(JCERSAPublicKey.class);
            keyPub = (BCRSAPublicKey) obj;
        } catch (ClassCastException ex) {
            errorList.add("privateKey pair did not decode correctly");
        }
        try {
            certPub = (BCRSAPublicKey) RSAKeyUtils.getBCRSAPublicKey(cert.getSubjectPublicKeyInfo());
        } catch (ClassCastException | RsaException ex) {
            errorList.add("Error could not retrieve public key from Cert");
            return errorList;
        }

        if (!certPub.getModulus().equals(keyPub.getModulus())) {
            errorList.add("Error cert and key Modulus mismatch");
        }

        if (!certPub.getPublicExponent().equals(keyPub.getPublicExponent())) {
            errorList.add("Error cert and key public exponents mismatch");
        }
        try {
            X509Certificate x509 = CertUtils.getX509Certificate(cert);
            x509.checkValidity();
        } catch (CertificateExpiredException ex) {
            errorList.add("Error cert Expired");
        } catch (CertificateNotYetValidException ex) {
            errorList.add("Error cert not yet valid");
        } catch (RuntimeException ex) {
            errorList.add("Unable to check date validity of Cert");
        } catch (NotAnX509CertificateException ex) {
            errorList.add("Unable to convert X509CertificateHolder to X509Certificate");
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
        } else if (obj instanceof BCRSAPublicKey) {
            BCRSAPublicKey rs = (BCRSAPublicKey) obj;
            n = rs.getModulus().mod(m16bit);
            e = rs.getPublicExponent();
            return String.format("(%s,%s)", e, n);
        } else {
            return String.format("(%s,%s)", "None", "None");
        }
    }

    public static KeyPair genKeyPair(int keySize) throws RsaException {
        SecureRandom sr;
        KeyPairGenerator kpGen;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new RsaException("Could not generate RSA Key", ex);
        }
        try {
            kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new RsaException("No such Algo RSA");
        } catch (NoSuchProviderException ex) {
            throw new RsaException("No such Provider BC");
        }
        kpGen.initialize(keySize);
        KeyPair kp = kpGen.generateKeyPair();
        return kp;
    }

    public static String shortKey(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof BCRSAPublicKey) {
            BCRSAPublicKey pubKey;
            pubKey = (BCRSAPublicKey) obj;
            String shortMod = pubKey.getModulus().mod(m16bit).toString(16);
            String shortE = pubKey.getPublicExponent().mod(m16bit).toString(16);
            return String.format("(%s,%s)", shortMod, shortE);
        } else if (obj instanceof JCERSAPrivateCrtKey) {
            JCERSAPrivateCrtKey privKey = (JCERSAPrivateCrtKey) obj;
            BCRSAPublicKey pubKey;
            try {
                pubKey = HackedProviderAccessor.newJCERSAPublicKey(privKey);
            } catch (InvalidKeySpecException ex) {
                return "Nak";
            }
            return shortKey(pubKey);
        } else if (obj instanceof BCRSAPrivateKey) {
            BCRSAPrivateKey privKey;
            privKey = (BCRSAPrivateKey) obj;
            String shortMod = privKey.getModulus().mod(m16bit).toString(16);
            String privateExponent = privKey.getPrivateExponent().mod(m16bit).toString(16);
            return String.format("(%s,%s)", shortMod, privateExponent);
        } else if (obj instanceof KeyPair) {
            KeyPair kp = (KeyPair) obj;
            return shortKey(kp.getPrivate());
        }
        return "NaK";
    }

    public static JCERSAPublicKey newJCERSAPublicKey(Object obj) throws NotAnRSAKeyException {
        if (obj == null) {
            throw new NotAnRSAKeyException("Key was null");
        } else if (obj instanceof JCERSAPublicKey) {
            // already a JCERSAPublicKey
            return (JCERSAPublicKey) obj;
        } else if (obj instanceof JCERSAPrivateCrtKey) {
            try {
                return newJCERSAPublicKey(HackedProviderAccessor.newJCERSAPublicKey((JCERSAPrivateCrtKey) obj));
            } catch (InvalidKeySpecException ex) {
                throw new NotAnRSAKeyException("Could not retrieve Public Key from incomming object", ex);
            }
        } else if (obj instanceof KeyPair) {
            return newJCERSAPublicKey(((KeyPair) obj).getPublic());
        } else {
            throw new NotAnRSAKeyException(String.format("Object was of class %s", obj.getClass().getName()));
        }
    }

    public static BigInteger getPublicExponent(Object obj) throws NotAnRSAKeyException {
        if (obj instanceof KeyPair) {
            return getPublicExponent(((KeyPair) obj).getPublic());
        } else if (obj instanceof BCRSAPublicKey) {
            BCRSAPublicKey key = (BCRSAPublicKey) obj;
            return key.getPublicExponent();
        } else if (obj instanceof BCRSAPrivateCrtKey) {
            BCRSAPrivateCrtKey key = (BCRSAPrivateCrtKey) obj;
            return key.getPublicExponent();
        }
        String fmt = "could not cast object %s to BCSRSA keytype";
        String msg = String.format(fmt, obj.getClass().getCanonicalName());
        throw new NotAnRSAKeyException(msg);

    }

    public static BigInteger getModulus(Object obj) throws NotAnRSAKeyException {
        if (obj instanceof KeyPair) {
            return getModulus(((KeyPair) obj).getPrivate());
        } else if (obj instanceof BCRSAPublicKey) {
            BCRSAPublicKey key = (BCRSAPublicKey) obj;
            return key.getModulus();
        } else if (obj instanceof BCRSAPrivateCrtKey) {
            BCRSAPrivateCrtKey key = (BCRSAPrivateCrtKey) obj;
            return key.getModulus();
        }
        String fmt = "could not cast object %s to BCSRSA keytype";
        String msg = String.format(fmt, obj.getClass().getCanonicalName());
        throw new NotAnRSAKeyException(msg);
    }

    public static PemObject toPKCS8(KeyPair kp) throws java.security.NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        BCRSAPrivateCrtKey privKey = (BCRSAPrivateCrtKey) kp.getPrivate();
        PrivateKeyInfo privKeyInfo = PrivateKeyInfo.getInstance(privKey.getEncoded());
        PKCS8Generator gen = new PKCS8Generator(privKeyInfo, null);
        PemObject out = gen.generate();
        return out;
    }

    public static String objToString(PublicKey jPub) throws NotAnRSAKeyException {
        if (jPub instanceof BCRSAPublicKey) {
            BCRSAPublicKey pubKey = (BCRSAPublicKey) jPub;
            BigInteger mod = getModulus(jPub);
            BigInteger pubExponent = getPublicExponent(jPub);
            String modStr = mod.toString();
            String pubExpStr = pubExponent.toString();
            return String.format("{n=%s\n, e=%s\n}", modStr, pubExpStr);
        }
        String fmt = "Class %s not implemented";
        return String.format(fmt, jPub.getClass().getCanonicalName());
    }

    public static BCRSAPrivateCrtKey getBCRSAPrivteKey(KeyPair kp) throws RsaException {
        PrivateKey privKey = kp.getPrivate();
        BCRSAPrivateCrtKey bcPrivKey;
        try {
            bcPrivKey = (BCRSAPrivateCrtKey) privKey;
        } catch (ClassCastException ex) {
            String msg = String.format("Could not convert %s to BCRSAPrivateCrtKey",
                    Debug.findClassPath(privKey.getClass()));
            throw new RsaException(msg, ex);
        }
        return bcPrivKey;
    }

    public static KeyPair getKeyPair(PEMKeyPair pkp) throws PemException {
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
        KeyPair kp;
        try {
            kp = converter.getKeyPair(pkp);
            return kp;
        } catch (PEMException ex) {
            String msg = String.format("Unable to convert %s to %s",
                    Debug.findClassPath(pkp.getClass()),
                    Debug.findClassPath(KeyPair.class));
            throw new PemException(msg, ex);
        }
    }

    public static BCRSAPublicKey getBCRSAPublicKey(KeyPair kp) throws RsaException {
        PublicKey obj = kp.getPublic();
        if (!(obj instanceof BCRSAPublicKey)) {
            String msg = Debug.castExceptionMessage(obj.getClass(), BCRSAPublicKey.class);
            throw new RsaException(msg);
        }
        return (BCRSAPublicKey) obj;
    }

    public static BCRSAPublicKey getBCRSAPublicKey(SubjectPublicKeyInfo pkinfo) throws RsaException {
        JcaPEMKeyConverter conv;
        if (pkinfo == null) {
            return null;
        }
        try {
            conv = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
            return (BCRSAPublicKey) conv.getPublicKey(pkinfo);
        } catch (PEMException ex) {
            String msg = Debug.castExceptionMessage(pkinfo.getClass(),
                    BCRSAPublicKey.class);
            throw new PemException(msg, ex);
        }
    }

    public static PEMKeyPair getPemKeyPair(KeyPair kp) {
        byte[] pubEncoded = kp.getPublic().getEncoded();
        byte[] privEncoded = kp.getPrivate().getEncoded();
        PrivateKeyInfo privInfo = PrivateKeyInfo.getInstance(privEncoded);
        SubjectPublicKeyInfo pubInfo = SubjectPublicKeyInfo.getInstance(pubEncoded);
        PEMKeyPair pkp = new PEMKeyPair(pubInfo, privInfo);
        return pkp;
    }

    public static PEMKeyPair getPemKeyPair(BCRSAPrivateCrtKey privKey) throws PemException {
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
        byte[] privKeyBytes = PemUtils.toPemBytes(privKey);
        KeyPair kp = null;
        try {
            kp = (KeyPair) PemUtils.fromPemBytes(privKeyBytes);
        } catch (UnsupportedEncodingException e) {
            throw new PemException(e.getMessage());
        }
        return getPemKeyPair(kp);
    }

}
