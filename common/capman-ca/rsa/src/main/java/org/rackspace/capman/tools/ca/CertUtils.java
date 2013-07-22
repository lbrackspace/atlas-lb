package org.rackspace.capman.tools.ca;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.RSAKeyUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnRSAKeyException;
import org.rackspace.capman.tools.ca.exceptions.RsaException;
import org.rackspace.capman.tools.ca.zeus.primitives.ErrorEntry;
import org.rackspace.capman.tools.ca.zeus.primitives.ErrorType;

public class CertUtils {

    public static final String ISSUER_NOT_AFTER_FAIL = "issuer Cert Not After Fail";
    public static final String ISSUER_NOT_BEFORE_FAIL = "issuer Cert Not Before Fail";
    public static final String SUBJECT_NOT_AFTER_FAIL = "subject Cert Not After Fail";
    public static final String SUBJECT_NOT_BEFORE_FAIL = "subject Cert Not Before Fail";
    public static final int DEFAULT_NOT_AFTER_YEARS = 2;
    public static final long DAY_IN_MILLIS = (long) 24 * 60 * 60 * 1000;

    static {
        RsaConst.init();
    }

    public static X509Certificate signCSR(PKCS10CertificationRequest req, KeyPair kp, X509Certificate caCrt, int days, BigInteger serial) throws RsaException {
        long nowMillis = System.currentTimeMillis();
        Date notBefore = new Date(nowMillis);
        Date notAfter = new Date((long) days * DAY_IN_MILLIS + nowMillis);
        return signCSR(req, kp, caCrt, notBefore, notAfter, serial);
    }

    public static X509Certificate signCSR(PKCS10CertificationRequest req, KeyPair kp, X509Certificate caCrt, Date notBeforeIn, Date notAfterIn,
            BigInteger serial) throws RsaException {
        int i;
        Date notBefore;
        Date notAfter;
        PublicKey caPub;
        PrivateKey caPriv;
        PublicKey crtPub;
        BigInteger serialNum;
        X509Certificate crt = null;
        JcaX509v3CertificateBuilder certBuilder;
        ContentSigner signer;
        X500Principal issuer;
        X500Principal subject;

        AuthorityKeyIdentifierStructure authKeyId;
        SubjectKeyIdentifierStructure subjKeyId;

        caPub = kp.getPublic();
        caPriv = kp.getPrivate();

        notBefore = notBeforeIn;
        notAfter = notAfterIn;
        try {
            crtPub = req.getPublicKey();
        } catch (GeneralSecurityException ex) {
            throw new RsaException("Unable to fetch public key from CSR", ex);
        }
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder(RsaConst.DEFAULT_SIGNATURE_ALGO);
        sigBuilder.setProvider("BC");

        try {
            signer = sigBuilder.build(caPriv);
        } catch (OperatorCreationException ex) {
            throw new RsaException("Error creating signature", ex);
        }
        try {
            if (!req.verify()) {
                throw new RsaException("CSR was invalid");
            }
        } catch (GeneralSecurityException ex) {
            throw new RsaException("Could not verify CSR", ex);
        }
        // If the user left a blank serial number then use the current time for the serial number
        serialNum = (serial == null) ? BigInteger.valueOf(System.currentTimeMillis()) : serial;
        byte[] encodedSubject = req.getCertificationRequestInfo().getSubject().toASN1Object().getDEREncoded();
        subject = new X500Principal(encodedSubject);
        //subject = new X500Principal(req.getCertificationRequestInfo().getSubject().toString());
        issuer = caCrt.getSubjectX500Principal();
        certBuilder = new JcaX509v3CertificateBuilder(issuer, serialNum,
                notBefore, notAfter, subject, crtPub);

        // Add any x509 extensions from the request
        ASN1Set attrs = req.getCertificationRequestInfo().getAttributes();
        X509Extension ext;
        if (attrs != null) {
            for (i = 0; i < attrs.size(); i++) {
                Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
                if (attr.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    DEREncodable extsDer = attr.getAttrValues().getObjectAt(0);
                    X509Extensions exts = X509Extensions.getInstance(extsDer);
                    for (ASN1ObjectIdentifier oid : exts.getExtensionOIDs()) {
                        ext = exts.getExtension(oid);
                        certBuilder.addExtension(oid, ext.isCritical(), ext.getParsedValue());
                    }
                }
            }
        }
        try {
            authKeyId = new AuthorityKeyIdentifierStructure(caCrt);
            subjKeyId = new SubjectKeyIdentifierStructure(crtPub);
        } catch (InvalidKeyException ex) {
            throw new RsaException("Ivalid public key when attempting to encode Subjectkey identifier", ex);
        } catch (CertificateParsingException ex) {
            throw new RsaException("Unable to build AuthorityKeyIdentifier", ex);
        }
        certBuilder.addExtension(X509Extension.authorityKeyIdentifier, false, authKeyId.getDERObject());
        certBuilder.addExtension(X509Extension.subjectKeyIdentifier, false, subjKeyId.getDERObject());
        X509CertificateHolder certHolder = certBuilder.build(signer);
        try {
            crt = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
        } catch (CertificateException ex) {
            throw new RsaException("Unable to build Certificate", ex);
        }
        return crt;
    }

    public static X509Certificate selfSignCsrCA(PKCS10CertificationRequest req, KeyPair kp, Date notBefore, Date notAfter) throws RsaException {
        PrivateKey priv;
        PublicKey pub;
        String msg;
        X509Certificate cert = null;
        SubjectKeyIdentifierStructure subjKeyId;
        int i;

        try {
            if (!req.verify()) {
                throw new RsaException("CSR was invalid");
            }
        } catch (GeneralSecurityException ex) {
            throw new RsaException("Could not verify CSR", ex);
        }
        X500Name subject = X500Name.getInstance(req.getCertificationRequestInfo().getSubject());
        X500Name issuer = X500Name.getInstance(req.getCertificationRequestInfo().getSubject());
        priv = kp.getPrivate();
        pub = kp.getPublic();
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder(RsaConst.DEFAULT_SIGNATURE_ALGO);
        sigBuilder.setProvider("BC");
        ContentSigner signer;
        try {
            signer = sigBuilder.build(priv);
        } catch (OperatorCreationException ex) {
            throw new RsaException("Error creating signature", ex);
        }
        BigInteger serial = BigInteger.ONE;
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, pub);

        certBuilder.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(true));//This is a CA crt
        try {
            subjKeyId = new SubjectKeyIdentifierStructure(pub);
        } catch (InvalidKeyException ex) {
            throw new RsaException("Ivalid public key when attempting to encode Subjectkey identifier", ex);
        }
        certBuilder.addExtension(X509Extension.subjectKeyIdentifier, false, subjKeyId.getDERObject());
        X509CertificateHolder certHolder = certBuilder.build(signer);
        try {
            cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
        } catch (CertificateException ex) {
            throw new RsaException("Error generating x509 certificate", ex);
        }
        return cert;
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(X509Certificate issuerCert, X509Certificate subjectCert) {
        return verifyIssuerAndSubjectCert(issuerCert, subjectCert, true);
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(X509Certificate issuerCert, X509Certificate subjectCert, boolean isDateFatal) {
        ErrorEntry errorEntry;
        List<ErrorEntry> errorList = new ArrayList<ErrorEntry>();
        PublicKey parentPub = null;
        try {
            parentPub = (PublicKey) issuerCert.getPublicKey();
        } catch (ClassCastException ex) {
            errorEntry = new ErrorEntry(ErrorType.SIGNATURE_ERROR, "error getting Public key from isser cert", true, ex);
            errorList.add(errorEntry);
        }
        try {
            subjectCert.checkValidity();
        } catch (CertificateExpiredException ex) {
            errorList.add(new ErrorEntry(ErrorType.EXPIRED_CERT, SUBJECT_NOT_AFTER_FAIL, isDateFatal, ex));
        } catch (CertificateNotYetValidException ex) {
            errorList.add(new ErrorEntry(ErrorType.PREMATURE_CERT, SUBJECT_NOT_BEFORE_FAIL, isDateFatal, ex));
        }
        try {
            issuerCert.checkValidity();
        } catch (CertificateExpiredException ex) {
            errorList.add(new ErrorEntry(ErrorType.EXPIRED_CERT, ISSUER_NOT_AFTER_FAIL, isDateFatal, ex));
        } catch (CertificateNotYetValidException ex) {
            errorList.add(new ErrorEntry(ErrorType.PREMATURE_CERT, ISSUER_NOT_BEFORE_FAIL, isDateFatal, ex));
        }

        if (parentPub == null) {
            return errorList; // Can't test anyfuther if we failed to extract the parent pubKey
        }

        try {
            subjectCert.verify(parentPub);
        } catch (CertificateException ex) {
            errorList.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "signature Algo mismatch", true, ex));
        } catch (NoSuchAlgorithmException ex) {
            errorList.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unrecognized signature Algo", true, ex));
        } catch (InvalidKeyException ex) {
            errorList.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Signing key mismatch", true, ex));
        } catch (NoSuchProviderException ex) {
            errorList.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Application doesn't know how to verify signature", true, ex));
        } catch (SignatureException ex) {
            errorList.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Signature does not match", true, ex));
        }

        return errorList;
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(byte[] issuerCertPem, byte[] subjectCertPem) {
        return verifyIssuerAndSubjectCert(issuerCertPem, subjectCertPem, true);
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(byte[] issuerCertPem, byte[] subjectCertPem, boolean isDateFatal) {
        List<ErrorEntry> errorList = new ArrayList<ErrorEntry>();
        ErrorEntry errorEntry;
        X509Certificate issuerCert = null;
        X509Certificate subjectCert = null;
        if (issuerCertPem == null) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "isserCertPem was null", true, null));
        }
        if (subjectCertPem == null) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "SubjectCertPem was null", true, null));
        }
        if (issuerCertPem == null || subjectCertPem == null) {
            return errorList;
        }

        try {
            issuerCert = (X509Certificate) PemUtils.fromPem(issuerCertPem);
        } catch (PemException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing issuer Cert data to X509Certificate", true, ex));
        } catch (ClassCastException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing issuer Cert data to X509Certificate", true, ex));
        }

        try {
            subjectCert = (X509Certificate) PemUtils.fromPem(subjectCertPem);
        } catch (PemException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing subject Cert data to X509Certificate", true, ex));
        } catch (ClassCastException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing issuer Cert data to X509Certificate", true, ex));
        }
        if (issuerCert == null || subjectCert == null) {
            return errorList;
        }
        return verifyIssuerAndSubjectCert(issuerCert, subjectCert, isDateFatal);
    }

    @Deprecated
    public static String certToStr(X509Certificate cert) {
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
        String subjectStr = cert.getSubjectX500Principal().toString();
        String issuerStr = cert.getIssuerX500Principal().toString();
        JCERSAPublicKey pub = (JCERSAPublicKey) cert.getPublicKey();
        String pubKeyClass = pub.getClass().getCanonicalName();
        BigInteger n = pub.getModulus();
        BigInteger e = pub.getPublicExponent();
        sb.append(String.format("subject = \"%s\"\n", subjectStr));
        sb.append(String.format("issuer = \"%s\"\n", issuerStr));
        sb.append(String.format("pubKeyClass = \"%s\"\n", pubKeyClass));
        sb.append(String.format("   n = %s\n", n));
        sb.append(String.format("   e = %s\n", e));
        sb.append(String.format("ShortPub = %s\n", RSAKeyUtils.shortPub(pub)));
        sb.append(String.format("pubModSize = %d\n", RSAKeyUtils.modSize(pub)));
        String valid = "Valid";
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException ex) {
            valid = "Not After Fail";
        } catch (CertificateNotYetValidException ex) {
            valid = "Not Before Fail";
        }
        String selfSigned = (isSelfSigned(cert)) ? "true" : "false";
        sb.append(String.format("isSelfSigned = %s\n", selfSigned));
        return sb.toString();
    }

    public static boolean isSelfSigned(X509Certificate cert) {
        PublicKey pubKey = cert.getPublicKey();
        try {
            cert.verify(pubKey);
        } catch (CertificateException ex) {
            return false;
        } catch (NoSuchAlgorithmException ex) {
            return false;
        } catch (InvalidKeyException ex) {
            return false;
        } catch (NoSuchProviderException ex) {
            return false;
        } catch (SignatureException ex) {
            return false;
        }
        return true;
    }

    public static boolean isSelfSigned(byte[] certPem) {
        X509Certificate cert;
        try {
            cert = (X509Certificate) PemUtils.fromPem(certPem);
        } catch (PemException ex) {
            return false;
        } catch (ClassCastException ex) {
            return false;
        }
        return isSelfSigned(cert);
    }

    public static boolean isCertExpired(X509Certificate x509, Date date) {
        Date dateObj = date;
        if (date == null) {
            dateObj = new Date(System.currentTimeMillis());
        }
        Date x509after = x509.getNotAfter();
        boolean isExpiredCert = x509after.before(dateObj);
        return isExpiredCert;
    }

    public static boolean isCertPremature(X509Certificate x509, Date date) {
        Date dateObj = date;
        if (date == null) {
            dateObj = new Date(System.currentTimeMillis());
        }
        Date x509Before = x509.getNotBefore();
        boolean isPrematureCert = x509Before.after(dateObj);
        return isPrematureCert;
    }

    public static boolean isCertDateValid(X509Certificate x509, Date date) {
        return !isCertPremature(x509, date) && !isCertExpired(x509, date);
    }

    public static X509Certificate quickSelfSign(KeyPair kpIn, String subjectName, Date notBefore, Date notAfter) throws RsaException {
        KeyPair kp;
        kp = kpIn;
        if (kp == null) {
            kp = RSAKeyUtils.genKeyPair(RSAKeyUtils.DEFAULT_KEY_SIZE);  // If you pass in null you'll never see the key again
        }
        PKCS10CertificationRequest csr = CsrUtils.newCsr(subjectName, kp, true);
        X509Certificate x509 = CertUtils.selfSignCsrCA(csr, kp, notBefore, notAfter);
        return x509;
    }

    public static Set<X509Certificate> getExpiredCerts(Set<X509Certificate> certs, Date date) {
        Set<X509Certificate> expiredCerts = new HashSet<X509Certificate>();
        for (X509Certificate x509 : certs) {
            if (isCertExpired(x509, date)) {
                expiredCerts.add(x509);
            }
        }
        return expiredCerts;
    }

    public static List<ErrorEntry> validateKeyMatchesCrt(KeyPair kp, X509CertificateObject x509obj) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        Object obj = kp.getPublic();
        if (!(obj instanceof JCERSAPublicKey)) {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Could not retrieve public key from keypair", true, null));
            return errors;
        }
        JCERSAPublicKey pubKey = (JCERSAPublicKey) obj;
        return validateKeyMatchesCert(pubKey, x509obj);
    }

    public static List<ErrorEntry> validateKeyMatchesCert(JCERSAPublicKey key, X509CertificateObject x509obj) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        JCERSAPublicKey certPub;
        Object obj = x509obj.getPublicKey();
        if (!(obj instanceof JCERSAPublicKey)) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to extract public RSA key from x509 certificate", true, null));
            return errors;
        }
        certPub = (JCERSAPublicKey) obj;

        if (!(certPub.getModulus().equals(key.getModulus()))) {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Modulus between user cert and key did not match", true, null));
        }

        if (!(certPub.getPublicExponent().equals(key.getPublicExponent()))) {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Cert and key public exponent do not match", true, null));
        }
        return errors;
    }

    public static List<ErrorEntry> validateKeySignsCert(KeyPair kp, X509CertificateObject x509obj) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        Object obj = kp.getPublic();
        if (!(obj instanceof JCERSAPublicKey)) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Could not retrieve public key from keypair", true, null));
            return errors;
        }
        JCERSAPublicKey pubKey = (JCERSAPublicKey) obj;
        return validateKeySignsCert(pubKey, x509obj);

    }

    public static List<ErrorEntry> validateKeySignsCert(JCERSAPublicKey key, X509CertificateObject x509obj) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        JCERSAPublicKey certPub;
        Object obj = x509obj.getPublicKey();
        if (!(obj instanceof JCERSAPublicKey)) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unable to extract public RSA key from x509 certificate", true, null));
            return errors;
        }
        try {
            x509obj.verify(key);
        } catch (CertificateException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unknown CertificateException", true, ex));
        } catch (NoSuchAlgorithmException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unknown signing Algorithm", true, ex));
        } catch (InvalidKeyException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Key does not match certificate", true, ex));
        } catch (NoSuchProviderException ex) {
            errors.add(new ErrorEntry(ErrorType.UNKNOWN, "Could not find BouncyCastle provider", true, ex));
        } catch (SignatureException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Ivalid Signature", true, ex));
        }
        return errors;
    }

    public static Set<X509Certificate> getPrematureCerts(Set<X509Certificate> certs, Date date) {
        Set<X509Certificate> prematureCerts = new HashSet<X509Certificate>();
        for (X509Certificate x509 : certs) {
            if (isCertPremature(x509, date)) {
                prematureCerts.add(x509);
            }
        }
        return prematureCerts;
    }

    public static Set<X509Certificate> getValidDateCerts(Set<X509Certificate> certs, Date date) {
        Set<X509Certificate> validDateCerts = new HashSet<X509Certificate>();
        for (X509Certificate x509 : certs) {
            if (isCertDateValid(x509, date)) {
                validDateCerts.add(x509);
            }
        }
        return validDateCerts;
    }
}
