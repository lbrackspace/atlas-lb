package org.rackspace.capman.tools.util;

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
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.rackspace.capman.tools.ca.CertUtils;
import org.rackspace.capman.tools.ca.CsrUtils;
import org.rackspace.capman.tools.ca.RSAKeyUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;

import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.ca.exceptions.X509PathBuildException;

// Use this one instead of X509Chainer
public class X509PathBuilder<E extends X509Certificate> {

    private Set<E> rootCAs;
    private Set<E> intermediates;
    private static final BigInteger serial = new BigInteger("2");

    static {
        RsaConst.init();
    }

    public X509PathBuilder() {
        rootCAs = new HashSet<E>();
        intermediates = new HashSet<E>();
    }

    public X509PathBuilder(Set<E> rootCAs, Set<E> intermediates) {
        this.rootCAs = new HashSet<E>(rootCAs);
        this.intermediates = new HashSet<E>(intermediates);
    }

    public void clear() {
        rootCAs = new HashSet<E>();
        intermediates = new HashSet<E>();
    }

    public X509BuiltPath<E> buildPath(E userCrt) throws X509PathBuildException {
        return buildPath(userCrt, null);
    }

    public X509BuiltPath<E> buildPath(E userCrt, Date date) throws X509PathBuildException {
        List<E> discoveredPath = new ArrayList<E>();

        // Build Crt Store
        Set<E> colStoreCrts = new HashSet<E>();
        colStoreCrts.addAll(intermediates);
        colStoreCrts.add(userCrt); // Don't forget to add the End cert
        colStoreCrts.removeAll(rootCAs); // rootCAs are the endpoint so remove them incase they are in the intermediates
        CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters(colStoreCrts);
        CertStore crtStore;
        try {
            crtStore = CertStore.getInstance("Collection", ccsp, "SUN");
        } catch (InvalidAlgorithmParameterException ex) {
            throw new X509PathBuildException("InvalidAlgorithmParemeter when initializing CollectionStore", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new X509PathBuildException("NoSuchAlgorithmException when initializing CollectionStore", ex);
        } catch (NoSuchProviderException ex) {
            throw new X509PathBuildException("NoSuchProviderException when initializing CollectionStore", ex);
        }
        X509CertSelector userCrtSelector = new X509CertSelector();
        userCrtSelector.setCertificate(userCrt);

        // Build trusted roots;
        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
        for (E x509 : rootCAs) {
            TrustAnchor ta = new TrustAnchor(x509, null);
            anchors.add(ta);
        }

        // Setup the path builder
        PKIXBuilderParameters pbp;
        try {
            pbp = new PKIXBuilderParameters(anchors, userCrtSelector);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new X509PathBuildException("InvalidAlgorithmParameter when initializing PKIXBuilderParameters", ex);
        }
        pbp.addCertStore(crtStore);
        pbp.setRevocationEnabled(false);
        pbp.setMaxPathLength(25);
        pbp.setDate(date);

        CertPathBuilder pathBuilder;
        try {
            pathBuilder = CertPathBuilder.getInstance("PKIX", "SUN");
        } catch (NoSuchAlgorithmException ex) {
            throw new X509PathBuildException("NoSuchAlgorithmException when initializing pathBuilder", ex);
        } catch (NoSuchProviderException ex) {
            throw new X509PathBuildException("NoSuchProviderException when initializing pathBuilder", ex);
        }
        PKIXCertPathBuilderResult buildResponse;
        try {
            buildResponse = (PKIXCertPathBuilderResult) pathBuilder.build(pbp);
        } catch (CertPathBuilderException ex) {
            throw new X509PathBuildException(ex);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new X509PathBuildException(ex);
        }
        CertPath builtCrtPath = buildResponse.getCertPath();
        Iterator crtIterator = builtCrtPath.getCertificates().iterator();
        while (crtIterator.hasNext()) {
            Object obj = crtIterator.next();
            if (!(obj instanceof X509Certificate)) {
                String fmt = "Object of type %s does not appear to be a of X509Certificate or a subtype";
                String msg = String.format(fmt, obj.getClass().getSimpleName());
                throw new IllegalStateException(msg);
            } else {
                discoveredPath.add((E) obj);
            }
        }
        TrustAnchor topAnchor;
        TrustAnchor mostTrustedAnchor = buildResponse.getTrustAnchor();
        Object obj = mostTrustedAnchor.getTrustedCert();
        E topCrt = (E) mostTrustedAnchor.getTrustedCert();
        X509BuiltPath<E> builtPath = new X509BuiltPath<E>(discoveredPath, topCrt);
        return builtPath;
    }

    // Usefull for pretending to be a CA when you want to test a Chain
    public static List<X509ChainEntry> newChain(List<String> subjNames, int keySize, Date notBefore, Date notAfter) throws NotAnX509CertificateException, RsaException {
        List<X509ChainEntry> chain = new ArrayList<X509ChainEntry>();
        String rootSubj = subjNames.get(0);
        KeyPair rootKey = RSAKeyUtils.genKeyPair(keySize);
        PKCS10CertificationRequest rootCsr = CsrUtils.newCsr(rootSubj, rootKey, true);
        Object obj = CertUtils.selfSignCsrCA(rootCsr, rootKey, notBefore, notAfter);
        if (!(obj instanceof X509CertificateObject)) {
            String fmt = "Could not generate X509CertificateObject for subj \"%s\"";
            String msg = String.format(fmt, rootSubj);
            throw new NotAnX509CertificateException(msg);
        }
        X509CertificateObject rootCrt = (X509CertificateObject)obj;
        chain.add(new X509ChainEntry(rootKey,rootCsr,rootCrt));
        List<String> subjNamesForSubChain = new ArrayList<String>();
        for (int i = 1; i < subjNames.size(); i++) {
            subjNamesForSubChain.add(subjNames.get(i));
        }
        chain.addAll(newChain(rootKey,rootCrt,subjNamesForSubChain,keySize,notBefore,notAfter));
        return chain;
    }

    // Usefull for pretending to be a CA when you want to test a Chain
    public static List<X509ChainEntry> newChain(KeyPair rootKey, X509CertificateObject rootCert, List<String> subjNames, int keySize, Date notBefore, Date notAfter) throws NotAnX509CertificateException, RsaException {
        List<X509ChainEntry> chain = new ArrayList<X509ChainEntry>();
        X509CertificateObject caCrt = rootCert;
        X509CertificateObject crt;
        Object obj;
        PKCS10CertificationRequest csr;
        KeyPair caKey = rootKey;
        KeyPair key;

        for (String subjName : subjNames) {
            key = RSAKeyUtils.genKeyPair(keySize);
            csr = CsrUtils.newCsr(subjName, key, true);
            obj = CertUtils.signCSR(csr, caKey, caCrt, notBefore, notAfter, serial);
            if (!(obj instanceof X509CertificateObject)) {
                String fmt = "Could not generate X509CertificateObject for subj \"%s\"";
                String msg = String.format(fmt, subjName);
                throw new NotAnX509CertificateException(msg);
            }
            crt = (X509CertificateObject) obj;
            chain.add(new X509ChainEntry(key, csr, crt));
            caCrt = crt;
            caKey = key;
        }
        return chain;
    }

    public Set<E> getRootCAs() {
        return rootCAs;
    }

    public void setRootCAs(Set<E> rootCAs) {
        this.rootCAs = rootCAs;
    }

    public Set<E> getIntermediates() {
        return intermediates;
    }

    public void setIntermediates(Set<E> intermediates) {
        this.intermediates = intermediates;
    }
}
