package org.rackspace.capman.tools.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.rackspace.capman.tools.ca.exceptions.PrivKeyDecodeException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderDecodeException;
import org.rackspace.capman.tools.ca.primitives.bcextenders.HackedProviderAccessor;

public class PrivKeyReader {

    static {
        RsaConst.init();
    }
    private JCERSAPrivateCrtKey privKey;

    public PrivKeyReader(JCERSAPrivateCrtKey privKey) {
        this.privKey = privKey;
    }

    public BigInteger getN() {
        return privKey.getModulus();
    }

    public BigInteger getP() {
        return privKey.getPrimeP();
    }

    public BigInteger getQ() {
        return privKey.getPrimeQ();
    }

    public BigInteger getE() {
        return privKey.getPublicExponent();
    }

    public BigInteger getD() {
        return privKey.getPrivateExponent();
    }

    public BigInteger getdP() {
        return privKey.getPrimeExponentP();
    }

    public BigInteger getdQ() {
        return privKey.getPrimeExponentQ();
    }

    public BigInteger getQinv() {
        return privKey.getCrtCoefficient();
    }

    public BigInteger getT() {
        return privKey.getPrimeP().subtract(BigInteger.ONE).
                multiply(privKey.getPrimeQ().
                subtract(BigInteger.ONE));
    }

    public static PrivKeyReader newPrivKeyReader(String pemString) throws PrivKeyDecodeException {
        JCERSAPrivateCrtKey privKey;
        Object obj;
        String msg;
        try {
            obj = PemUtils.fromPemString(pemString);
        } catch (PemException ex) {
            throw new PrivKeyDecodeException("Error decoding Key", ex);
        }
        if (obj instanceof KeyPair) {
            KeyPair kp = (KeyPair) obj;
            privKey = (JCERSAPrivateCrtKey) kp.getPrivate();
            return new PrivKeyReader(privKey);
        }
        try {
            privKey = (JCERSAPrivateCrtKey) obj;
        } catch (ClassCastException ex) {
            msg = String.format("Error casting %s to %s", obj.getClass().getName(), "JCERSAPrivateCrtKey");
            throw new PrivKeyDecodeException(msg, ex);
        }
        return new PrivKeyReader(privKey);
    }

    public KeyPair toKeyPair() throws InvalidKeySpecException {
        KeyPair kp = HackedProviderAccessor.newKeyPair(privKey);
        return kp;
    }

    public JCERSAPrivateCrtKey getPrivKey() {
        return privKey;
    }

    public void setPrivKey(JCERSAPrivateCrtKey privKey) {
        this.privKey = privKey;
    }

    public static String getPubKeyHash(PublicKey pubKey) {
        SubjectKeyIdentifierStructure skis;
        try {
            skis = new SubjectKeyIdentifierStructure(pubKey);
        } catch (InvalidKeyException ex) {
            return null;
        }
        byte[] keyIdBytes = skis.getKeyIdentifier();
        if (keyIdBytes == null) {
            return null;
        }
        String out = StaticHelpers.bytes2hex(keyIdBytes);
        return out;

    }
}
