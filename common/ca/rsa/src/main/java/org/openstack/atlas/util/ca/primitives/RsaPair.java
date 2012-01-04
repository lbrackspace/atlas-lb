package org.openstack.atlas.util.ca.primitives;

import java.security.KeyPair;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.security.Provider;

import org.bouncycastle.jce.provider.HackedProviderAccessor;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.ConversionException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.NoSuchAlgorithmException;
import org.openstack.atlas.util.ca.exceptions.NullKeyException;

public class RsaPair {

    private RSAPrivateCrtKeyParameters priv = null;
    private RSAKeyParameters pub = null;

    public RsaPair() {
    }

    public RsaPair(AsymmetricCipherKeyPair acPair) {
        this.priv = (RSAPrivateCrtKeyParameters) acPair.getPrivate();
        this.pub = (RSAKeyParameters) acPair.getPublic();
    }

    public RsaPair(KeyPair jKeyPair) throws ConversionException {
        String msg;
        String fmt;
        String jPrivClass;
        String jPubClass;
        PrivateKey jPriv = jKeyPair.getPrivate();
        PublicKey jPub = jKeyPair.getPublic();

        jPrivClass = jPriv.getClass().getCanonicalName();
        jPubClass = jPub.getClass().getCanonicalName();
        try {
            this.priv = HackedProviderAccessor.newRSAPrivateCrtKeyParameters((JCERSAPrivateCrtKey) jPriv);
            this.pub =  HackedProviderAccessor.newRSAKeyParameters((JCERSAPublicKey) jPub);
        } catch (ClassCastException ex) {
            fmt = "Could not convert (%s,%s) to types (RSAPrivateCrtKeyParameters,RSAKeyParameters)";
            msg = String.format(fmt,jPrivClass,jPubClass);
            throw new ConversionException(msg,ex);
        }
    }

    public RsaPair(RSAPrivateCrtKeyParameters priv, RSAKeyParameters pub) {
        this.priv = priv;
        this.pub = pub;
    }

    public static RsaPair getInstance(int bits, int certainty) throws NoSuchAlgorithmException {
        RSAKeyPairGenerator gen;
        SecureRandom sr;
        RSAKeyGenerationParameters params;
        AsymmetricCipherKeyPair pair;
        gen = new RSAKeyPairGenerator();
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException("No such Lowerlevel Algo", ex);
        }
        params = new RSAKeyGenerationParameters(RsaConst.E, sr, bits, certainty);
        gen.init(params);
        AsymmetricCipherKeyPair ac = gen.generateKeyPair();
        RsaPair keys = new RsaPair(ac);
        return keys;
    }

    public static RsaPair getInstance(int bits) throws NoSuchAlgorithmException {
        return getInstance(bits, RsaConst.DEFAULT_PRIME_CERTAINTY);
    }

    public byte[] getPrivAsPem() throws NullKeyException, PemException {
        byte[] out;
        PrivateKey jPriv;
        if (priv == null) {
            throw new NullKeyException("No priv key in RsaPair");
        }
        jPriv = HackedProviderAccessor.newJCERSAPrivateCrtKey(priv);
        out = PemUtils.toPem(jPriv);
        return out;
    }

    public RSAPrivateCrtKeyParameters getPriv() {
        return priv;
    }

    public RSAKeyParameters getPub() {
        return pub;
    }

    public void setPriv(RSAPrivateCrtKeyParameters priv) {
        this.priv = priv;
    }

    public void setPub(RSAKeyParameters pub) {
        this.pub = pub;
    }

    public String getPubAsString() {
        StringBuilder sb = new StringBuilder(RsaConst.INIT_BUFFER_SIZE);
        RSAPublicKeyStructure pubStruct = getPubAsStruct();
        if (pubStruct == null) {
            return "";
        }
        BigInteger n = pubStruct.getModulus();
        BigInteger e = pubStruct.getPublicExponent();
        sb.append("PubKey:\n");
        sb.append(String.format("    n=%s\n", n.toString()));
        sb.append(String.format("    e=%s\n", e.toString()));
        sb.append(String.format("    shortPub = %s\n",RSAKeyUtils.shortPub(pubStruct)));
        return sb.toString();
    }

    public String getPrivAsString() {
        StringBuilder sb = new StringBuilder(RsaConst.INIT_BUFFER_SIZE);
        RSAPrivateKeyStructure privStruct = getPrivAsStruct();
        if (privStruct == null) {
            return null;
        }
        BigInteger n = privStruct.getModulus();
        BigInteger e = privStruct.getPublicExponent();
        BigInteger d = privStruct.getPrivateExponent();
        BigInteger q = privStruct.getPrime1();
        BigInteger p = privStruct.getPrivateExponent();
        BigInteger dP = privStruct.getExponent1();
        BigInteger dQ = privStruct.getExponent2();
        BigInteger qInv = privStruct.getCoefficient();
        sb.append("PrivKey:\n");
        sb.append(String.format("    n=%s\n", n.toString()));
        sb.append(String.format("    e=%s\n", e.toString()));
        sb.append(String.format("    d=%s\n", d.toString()));
        sb.append(String.format("    q=%s\n", q.toString()));
        sb.append(String.format("    p=%s\n", p.toString()));
        sb.append(String.format("    dP=%s\n", dP.toString()));
        sb.append(String.format("    dQ=%s\n", dQ.toString()));
        sb.append(String.format("    qInv=%s\n", qInv.toString()));
        BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        BigInteger totient = pMinus1.multiply(qMinus1);
        sb.append(String.format("    \u03A6(n)=%s\n", totient));


        return sb.toString();
    }

    public RSAPublicKeyStructure getPubAsStruct() {
        RSAPublicKeyStructure pubStruct;
        if (pub == null) {
            return null;
        }
        BigInteger n = pub.getModulus();
        BigInteger e = pub.getExponent();
        pubStruct = new RSAPublicKeyStructure(n, e);
        return pubStruct;
    }

    public RSAPrivateKeyStructure getPrivAsStruct() {
        RSAPrivateKeyStructure privStruct;
        if (priv == null) {
            return null;
        }
        BigInteger n = priv.getModulus();
        BigInteger e = priv.getPublicExponent();
        BigInteger d = priv.getExponent();
        BigInteger p = priv.getP();
        BigInteger q = priv.getQ();
        BigInteger dP = priv.getDP();
        BigInteger dQ = priv.getDQ();
        BigInteger qInv = priv.getQInv();

        privStruct = new RSAPrivateKeyStructure(n, e, d, p, q, dP, dQ, qInv);
        return privStruct;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(RsaConst.INIT_BUFFER_SIZE);
        sb.append(getPubAsString());
        sb.append("\n");
        sb.append(getPrivAsString());
        return sb.toString();
    }

    // returns an implementation of java.security.KeyPair for pem
    // incodeing since the bouncy openssl PemWriter expects it.
    public KeyPair toJavaSecurityKeyPair() throws NullKeyException {
        PrivateKey jPriv;
        PublicKey jPub;
        if (priv == null && pub == null) {
            throw new NullKeyException("keypair has no public or private key");
        } else if (priv == null) {
            throw new NullKeyException("keypair has not private key");
        } else if (pub == null) {
            throw new NullKeyException("keypair has not public key");
        }
        jPriv = HackedProviderAccessor.newJCERSAPrivateCrtKey(this.priv);
        jPub = HackedProviderAccessor.newJCERSAPublicKey(pub);
        return new KeyPair(jPub, jPriv);
    }
}
