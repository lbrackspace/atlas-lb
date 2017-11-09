package org.openstack.atlas.util.ca.util;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.CsrUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;

import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.primitives.Debug;

// Use this one instead of X509Chainer
public class X509PathBuilder {

    static {
        RsaConst.init();
    }

    public X509PathBuilder() {
    }

    // Usefull for pretending to be a CA when you want to test a Chain
    public static List<X509ChainEntry> newChain(List<String> subjNames, int keySize, Date notBefore, Date notAfter) throws NotAnX509CertificateException, RsaException {
        List<X509ChainEntry> chain = new ArrayList<X509ChainEntry>();
        String rootSubj = subjNames.get(0);
        KeyPair rootKey = RSAKeyUtils.genKeyPair(keySize);
        PKCS10CertificationRequest rootCsr = CsrUtils.newCsr(rootSubj, rootKey, true);
        Object obj = CertUtils.selfSignCsrCA(rootCsr, rootKey, notBefore, notAfter);
        if (!(obj instanceof X509CertificateHolder)) {
            String fmt = "Could not generate X509CertificateObject for subj \"%s\"";
            String msg = String.format(fmt, rootSubj);
            throw new NotAnX509CertificateException(msg);
        }
        X509CertificateHolder rootCrt = (X509CertificateHolder) obj;
        chain.add(new X509ChainEntry(rootKey, rootCsr, rootCrt));
        List<String> subjNamesForSubChain = new ArrayList<String>();
        for (int i = 1; i < subjNames.size(); i++) {
            subjNamesForSubChain.add(subjNames.get(i));
        }
        chain.addAll(newChain(rootKey, rootCrt, subjNamesForSubChain, keySize, notBefore, notAfter));
        return chain;
    }

    // Usefull for pretending to be a CA when you want to test a Chain
    public static List<X509ChainEntry> newChain(KeyPair rootKey, X509CertificateHolder rootCert, List<String> subjNames, int keySize, Date notBefore, Date notAfter) throws NotAnX509CertificateException, RsaException {
        List<X509ChainEntry> chain = new ArrayList<X509ChainEntry>();
        X509Certificate caCrtXC = CertUtils.getX509Certificate(rootCert);
        X509CertificateHolder crt;
        X509CertificateHolder currCa = rootCert;
        KeyPair currSigningKey = rootKey;
        Object obj;
        PKCS10CertificationRequest csr;
        KeyPair caKey = rootKey;
        KeyPair key;

        for (String subjName : subjNames) {
            key = RSAKeyUtils.genKeyPair(keySize);
            csr = CsrUtils.newCsr(subjName, key, true);
            obj = CertUtils.signCSR(csr, currSigningKey, currCa, notBefore, notAfter, null);
            if (!(obj instanceof X509CertificateHolder)) {
                String fmt = "Could not generate X509CertificateObject for subj \"%s\"";
                String msg = String.format(fmt, subjName);
                throw new NotAnX509CertificateException(msg);
            }
            crt = (X509CertificateHolder) obj;
            chain.add(new X509ChainEntry(key, csr, crt));
            currSigningKey = key;
            currCa = crt;
        }
        return chain;
    }
}
