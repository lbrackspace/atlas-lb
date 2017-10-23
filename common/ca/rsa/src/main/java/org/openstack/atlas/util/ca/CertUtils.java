package org.openstack.atlas.util.ca;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
//import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.RsaConst;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import org.openstack.atlas.util.ca.zeus.ErrorType;
import org.openstack.atlas.util.ca.CsrUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.primitives.Debug;

public class CertUtils {

    public static final String ISSUER_NOT_AFTER_FAIL = "Issuer Cert Expired";
    public static final String ISSUER_NOT_BEFORE_FAIL = "Sssuer Cert Premature";
    public static final String SUBJECT_NOT_AFTER_FAIL = "Subject Cert Expired";
    public static final String SUBJECT_NOT_BEFORE_FAIL = "subject Cert Premature";
    public static final int DEFAULT_NOT_AFTER_YEARS = 2;
    public static final long DAY_IN_MILLIS = (long) 24 * 60 * 60 * 1000;

    static {
        RsaConst.init();
    }

    public static X500Name getSubjectNameFromCert(X509CertificateHolder xh) {
        return xh.getSubject();
    }

    public static X500Name getIssuerNameFromCert(X509CertificateHolder xh) {
        return xh.getIssuer();
    }

    public static X500Name getSubjectNameFromCert(X509Certificate crt) {
        return X500Name.getInstance(crt.getSubjectX500Principal().getEncoded());
    }

    public static X500Name getIssuerNameFromCert(X509Certificate crt) {
        return X500Name.getInstance(crt.getIssuerX500Principal().getEncoded());
    }

    public static X509CertificateHolder signCSR(PKCS10CertificationRequest req, KeyPair kp, X509CertificateHolder caCrt, int days, BigInteger serial) throws RsaException {
        long nowMillis = System.currentTimeMillis();
        Date notBefore = new Date(nowMillis);
        Date notAfter = new Date((long) days * DAY_IN_MILLIS + nowMillis);
        return signCSR(req, kp, caCrt, notBefore, notAfter, serial);
    }

    public static X509CertificateHolder signCSR(PKCS10CertificationRequest req, KeyPair kp, X509CertificateHolder caCrt, Date notBeforeIn, Date notAfterIn) throws RsaException {
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        return signCSR(req, kp, caCrt, notBeforeIn, notAfterIn, serial);
    }

    public static X509CertificateHolder signCSR(PKCS10CertificationRequest req, KeyPair kp, X509CertificateHolder caCrt, Date notBeforeIn, Date notAfterIn,
            BigInteger serial) throws RsaException {
        int i;
        Date notBefore;
        Date notAfter;
        PublicKey caPub;
        PrivateKey caPriv;
        PublicKey crtPub;
        BigInteger serialNum;
        JcaX509v3CertificateBuilder certBuilder;
        ContentSigner signer;
        X500Name issuer;
        X500Name subject;

        caPub = kp.getPublic();
        caPriv = kp.getPrivate();

        notBefore = notBeforeIn;
        notAfter = notAfterIn;
        try {
            crtPub = CsrUtils.getPubKeyFromCSR(req);
        } catch (RsaException ex) {
            throw new RsaException("Unable to fetch public key from CSR", ex);
        }
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder(RsaConst.DEFAULT_SIGNATURE_ALGO);
        sigBuilder.setProvider("BC");

        try {
            signer = sigBuilder.build(caPriv);
        } catch (OperatorCreationException ex) {
            throw new RsaException("Error creating signature", ex);
        }
        if (!CsrUtils.verifyCSR(req, crtPub)) {
            throw new RsaException("CSR self signature failer");
        }

        // If the user left a blank serial number then use the current time for the serial number
        serialNum = (serial == null) ? BigInteger.valueOf(System.currentTimeMillis()) : serial;
        subject = CsrUtils.getX500SubjectNameFromCSR(req);
        issuer = CertUtils.getIssuerNameFromCert(caCrt);
        //subject = new X500Principal(req.getCertificationRequestInfo().getSubject().toString());
        certBuilder = new JcaX509v3CertificateBuilder(issuer, serialNum,
                notBefore, notAfter, subject, crtPub);

        // Add any x509 extensions from the request
        ASN1Set attrs = req.toASN1Structure().getCertificationRequestInfo().getAttributes();
        Extension ext;
        if (attrs != null) {
            for (i = 0; i < attrs.size(); i++) {
                Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
                if (attr.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    ASN1Encodable extsDer = attr.getAttrValues().getObjectAt(0);
                    Extensions exts = Extensions.getInstance(extsDer);
                    for (ASN1ObjectIdentifier oid : exts.getExtensionOIDs()) {
                        ext = exts.getExtension(oid);
                        try {
                            certBuilder.addExtension(oid, ext.isCritical(), ext.getParsedValue());
                        } catch (CertIOException ex) {
                            String msg = String.format("Unable to add %s extension", oid.getId());
                            throw new PemException(msg, ex);
                        }
                    }
                }
            }
        }
        AuthorityKeyIdentifier authKeyId;
        SubjectKeyIdentifier subKeyId;
        try {
            JcaX509ExtensionUtils xeu = new JcaX509ExtensionUtils();
            authKeyId = xeu.createAuthorityKeyIdentifier(caCrt);
            SubjectPublicKeyInfo subjKeyInfo = req.getSubjectPublicKeyInfo();
            subKeyId = xeu.createSubjectKeyIdentifier(subjKeyInfo);
        } catch (NoSuchAlgorithmException ex) {
            throw new RsaException("Unable to get default hash algo", ex);
        }
        try {
            certBuilder.addExtension(RsaConst.authKeyId, false, authKeyId);
            certBuilder.addExtension(RsaConst.subjectKeyId, false, subKeyId);
            X509CertificateHolder certHolder = certBuilder.build(signer);
            return certHolder;
        } catch (CertIOException ex) {
            String msg = "Unable to add subject and authority keyids";
            throw new RsaException(msg, ex);
        }
    }

    public static X509CertificateHolder selfSignCsrCA(PKCS10CertificationRequest req, KeyPair kp, Date notBefore, Date notAfter) throws RsaException {
        PrivateKey priv;
        PublicKey pub;
        String msg;
        X509Certificate cert = null;
        Extension caCrtExtension;
        priv = kp.getPrivate();
        pub = kp.getPublic();

        if (!CsrUtils.verifyCSR(req, pub)) {
            throw new RsaException("CSR was invalid");
        }
        X500Name subject = req.getSubject();
        X500Name issuer = req.getSubject(); // Self signed means the subject is acting as its own issuer
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
        caCrtExtension = CsrUtils.getCaExt(true);
        try {
            certBuilder.addExtension(caCrtExtension);//This is a CA crt
        } catch (CertIOException ex) {
            msg = "Unable to create ca basic constraint = Trye";
            throw new RsaException(msg, ex);
        }
        SubjectKeyIdentifier subKeyId;
        try {

            JcaX509ExtensionUtils xeu = new JcaX509ExtensionUtils();
            SubjectPublicKeyInfo subjKeyInfo = req.getSubjectPublicKeyInfo();
            subKeyId = xeu.createSubjectKeyIdentifier(subjKeyInfo);
            certBuilder.addExtension(RsaConst.subjectKeyId, false, subKeyId.toASN1Primitive());
        } catch (NoSuchAlgorithmException ex) {
            msg = "Unable to build hash for SubjectPublic key ID";
            throw new RsaException(msg, ex);
        } catch (CertIOException ex) {
            msg = "Unable to add SubjectJeyId extension";
            throw new RsaException(msg, ex);
        }

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return certHolder;
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(X509CertificateHolder issuerCert, X509CertificateHolder subjectCert) {
        return verifyIssuerAndSubjectCert(issuerCert, subjectCert, true);
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(X509CertificateHolder issuerCert, X509CertificateHolder subjectCert, boolean isDateFatal) {
        ErrorEntry errorEntry;
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        PublicKey parentPub = null;
        X509Certificate subjectCrtXC;
        X509Certificate issuerCrtXC;

        try {
            subjectCrtXC = CertUtils.getX509Certificate(subjectCert);
        } catch (NotAnX509CertificateException ex) {
            ErrorEntry entry = new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to convert cert to X509Certificate", true, ex);
            errors.add(entry);
            return errors;
        }

        try {
            issuerCrtXC = CertUtils.getX509Certificate(issuerCert);
        } catch (NotAnX509CertificateException ex) {
            ErrorEntry entry = new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to convert cert to X509Certificate", true, ex);
            errors.add(entry);
            return errors;
        }

        try {
            parentPub = RSAKeyUtils.getBCRSAPublicKey(issuerCert.getSubjectPublicKeyInfo());
        } catch (RsaException ex) {
            ErrorEntry entry = new ErrorEntry(ErrorType.UNREADABLE_KEY, "unable to extract Key from cert", true, ex);
            errors.add(entry);
            return errors;
        }
        try {
            subjectCrtXC.checkValidity();
        } catch (CertificateExpiredException ex) {
            errors.add(new ErrorEntry(ErrorType.EXPIRED_CERT, SUBJECT_NOT_AFTER_FAIL, isDateFatal, ex));
        } catch (CertificateNotYetValidException ex) {
            errors.add(new ErrorEntry(ErrorType.PREMATURE_CERT, SUBJECT_NOT_BEFORE_FAIL, isDateFatal, ex));
        }
        try {
            issuerCrtXC.checkValidity();
        } catch (CertificateExpiredException ex) {
            errors.add(new ErrorEntry(ErrorType.EXPIRED_CERT, ISSUER_NOT_AFTER_FAIL, isDateFatal, ex));
        } catch (CertificateNotYetValidException ex) {
            errors.add(new ErrorEntry(ErrorType.PREMATURE_CERT, ISSUER_NOT_BEFORE_FAIL, isDateFatal, ex));
        }
        if (parentPub == null) {
            return errors; // Can't test anyfuther if we failed to extract the parent pubKey
        }
        try {
            subjectCrtXC.verify(parentPub);
        } catch (CertificateException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "signature Algo mismatch", true, ex));
        } catch (NoSuchAlgorithmException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unrecognized signature Algo", true, ex));
        } catch (InvalidKeyException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Signing key mismatch", true, ex));
        } catch (NoSuchProviderException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Application doesn't know how to verify signature", true, ex));
        } catch (SignatureException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Signature does not match", true, ex));
        }
        return errors;
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(byte[] issuerCertPem, byte[] subjectCertPem) {
        return verifyIssuerAndSubjectCert(issuerCertPem, subjectCertPem, true);
    }

    public static List<ErrorEntry> verifyIssuerAndSubjectCert(byte[] issuerCertPem, byte[] subjectCertPem, boolean isDateFatal) {
        List<ErrorEntry> errorList = new ArrayList<ErrorEntry>();
        ErrorEntry errorEntry;
        X509CertificateHolder issuerCert = null;
        X509CertificateHolder subjectCert = null;
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
            issuerCert = (X509CertificateHolder) PemUtils.fromPemBytes(issuerCertPem);
        } catch (PemException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing issuer Cert data to X509Certificate", true, ex));
        } catch (ClassCastException ex) {
            errorList.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Error decodeing issuer Cert data to X509Certificate", true, ex));
        }

        try {
            subjectCert = (X509CertificateHolder) PemUtils.fromPemBytes(subjectCertPem);
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
        StringBuilder sb = new StringBuilder();
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
        sb.append(String.format("pubModSize = %d\n", n.bitLength()));
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
            cert = (X509Certificate) PemUtils.fromPemBytes(certPem);
        } catch (PemException ex) {
            return false;
        } catch (ClassCastException ex) {
            return false;
        }
        return isSelfSigned(cert);
    }

    public static boolean isCertExpired(X509CertificateHolder x509, Date date) {
        Date dateObj = date;
        if (date == null) {
            dateObj = new Date(System.currentTimeMillis());
        }
        Date x509after = x509.getNotAfter();
        boolean isExpiredCert = x509after.before(dateObj);
        return isExpiredCert;
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

    public static boolean isCertPremature(X509CertificateHolder x509, Date date) {
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

    public static X509CertificateHolder quickSelfSign(KeyPair kpIn, String subjectName, int daysBefore, int daysAfter) throws RsaException {
        long nowMillis = System.currentTimeMillis();
        long daysBeforeMillis = nowMillis - 24L * 60L * 60L * (long) daysBefore;
        long daysAfterMillis = nowMillis + 24L * 60L * 60L * (long) daysAfter;
        Date notBefore = new Date(daysBeforeMillis);
        Date notAfter = new Date(daysAfterMillis);
        return quickSelfSign(kpIn, subjectName, notBefore, notAfter);
    }

    public static X509CertificateHolder quickSelfSign(KeyPair kpIn, String subjectName, Date notBefore, Date notAfter) throws RsaException {
        KeyPair kp;
        kp = kpIn;
        if (kp == null) {
            kp = RSAKeyUtils.genKeyPair(RSAKeyUtils.DEFAULT_KEY_SIZE);  // If you pass in null you'll never see the key again
        }
        PKCS10CertificationRequest csr = CsrUtils.newCsr(subjectName, kp, true);
        X509CertificateHolder x509 = CertUtils.selfSignCsrCA(csr, kp, notBefore, notAfter);
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

    public static List<ErrorEntry> validateKeyMatchesCrt(KeyPair kp, X509CertificateHolder xh) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        PublicKey pubKey = kp.getPublic();
        if (pubKey instanceof BCRSAPublicKey) {
            return validateKeyMatchesCrt((BCRSAPublicKey) pubKey, xh);
        } else {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Could not retrieve public key from keypair", true, null));
            return errors;
        }

    }

    public static List<ErrorEntry> validateKeyMatchesCrt(BCRSAPublicKey key, X509CertificateHolder xh) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        BCRSAPublicKey certPub;
        PublicKey pubKey;
        SubjectPublicKeyInfo spki = xh.getSubjectPublicKeyInfo();
        try {
            certPub = RSAKeyUtils.getBCRSAPublicKey(spki);
        } catch (RsaException ex) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to extract public RSA key from x509 certificate", true, null));
            return errors;
        }
        if (!(certPub.getModulus().equals(key.getModulus()))) {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Modulus between user cert and key did not match", true, null));
        }

        if (!(certPub.getPublicExponent().equals(key.getPublicExponent()))) {
            errors.add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, "Cert and key public exponent do not match", true, null));
        }
        return errors;
    }

    public static List<ErrorEntry> validateKeySignsCert(KeyPair kp, X509CertificateHolder xh) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        PublicKey pubKey = kp.getPublic();
        if (pubKey instanceof BCRSAPublicKey) {
            BCRSAPublicKey bcPubKey = (BCRSAPublicKey) pubKey;
            return validateKeySignsCert(bcPubKey, xh);
        } else {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Could not retrieve public key from keypair", true, null));
            return errors;
        }
    }

    public static List<ErrorEntry> validateKeySignsCert(BCRSAPublicKey key, X509CertificateHolder x509obj) {
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();
        JCERSAPublicKey certPub;
        SubjectPublicKeyInfo spki = x509obj.getSubjectPublicKeyInfo();
        try {
            RSAKeyUtils.getBCRSAPublicKey(spki);
        } catch (RsaException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unable to extract public RSA key from x509 certificate", true, null));
            return errors;
        }
        try {
            X509Certificate xc = getX509Certificate(x509obj);
            xc.verify(key, new BouncyCastleProvider());
        } catch (CertificateException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unknown CertificateException", true, ex));
        } catch (NoSuchAlgorithmException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Unknown signing Algorithm", true, ex));
        } catch (InvalidKeyException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Key does not match certificate", true, ex));
        } catch (SignatureException ex) {
            errors.add(new ErrorEntry(ErrorType.SIGNATURE_ERROR, "Ivalid Signature", true, ex));
        } catch (NotAnX509CertificateException ex) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unreadable crt during sig verify", true, ex));
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

    public static X509Certificate getX509Certificate(X509CertificateObject xo) {
        return (X509Certificate) xo;
    }

    public static X509CertificateHolder getX509CertificateHolder(X509CertificateObject x509obj) throws NotAnX509CertificateException {
        X509CertificateHolder x509holder;
        if (x509obj == null) {
            return null;
        }
        try {
            x509holder = new JcaX509CertificateHolder((X509Certificate) x509obj);
        } catch (CertificateEncodingException ex) {
            throw new NotAnX509CertificateException("Unable to decode x509 certificate");
        }
        return x509holder;
    }

    public static X509Certificate getX509Certificate(X509CertificateHolder xh) throws NotAnX509CertificateException {
        if (xh == null) {
            return null;
        }
        try {
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider("BC");
            return converter.getCertificate(xh);
        } catch (CertificateException ex) {
            String msg = Debug.castExceptionMessage(xh.getClass(), JcaX509CertificateConverter.class);
            throw new NotAnX509CertificateException(msg, ex);
        }
    }

    public static X509CertificateHolder getX509CertificateHolder(X509Certificate xc) throws NotAnX509CertificateException {
        if (xc == null) {
            return null;
        }
        JcaX509CertificateHolder xh;
        try {
            xh = new JcaX509CertificateHolder(xc);
            return xh;
        } catch (CertificateEncodingException ex) {
            String msg = Debug.castExceptionMessage(xc.getClass(), JcaX509CertificateHolder.class);
            throw new NotAnX509CertificateException(msg, ex);
        }
    }

    @Deprecated
    public static X509CertificateObject getX509CertificateObject(X509CertificateHolder xh) throws NotAnX509CertificateException {
        if (xh == null) {
            return null;
        }
        Certificate cert = xh.toASN1Structure();
        try {
            X509CertificateObject xo = new X509CertificateObject(cert);
            return xo;
        } catch (CertificateParsingException ex) {
            String msg = Debug.castExceptionMessage(xh.getClass(), X509CertificateObject.class);
            throw new NotAnX509CertificateException(msg, ex);
        }
    }

    @Deprecated
    public static X509CertificateObject getX509CertificateObject(X509Certificate xc) throws NotAnX509CertificateException {
        X509CertificateHolder xh = getX509CertificateHolder(xc);
        X509CertificateObject xo = getX509CertificateObject(xh);
        return xo;
    }
}
