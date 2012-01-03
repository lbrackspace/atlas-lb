package org.openstack.atlas.util.ca;

import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.primitives.RsaPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.x509.Attribute;
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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.openstack.atlas.util.ca.exceptions.NullKeyException;
import org.openstack.atlas.util.ca.exceptions.RsaException;

public class CertUtils {

    public static X509Certificate signCSR(PKCS10CertificationRequest req,
            RsaPair keys, X509Certificate caCrt, int days, BigInteger serial) throws NullKeyException, RsaException {
        long nowMillis;
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

        KeyPair kp = keys.toJavaSecurityKeyPair();
        caPub = kp.getPublic();
        caPriv = kp.getPrivate();

        nowMillis = System.currentTimeMillis();
        notBefore = new Date(nowMillis);
        notAfter = new Date((long) days * 24 * 60 * 60 * 1000 + nowMillis);


        try {
            crtPub = req.getPublicKey();
        } catch (GeneralSecurityException ex) {
            throw new RsaException("Unable to fetch public key from CSR", ex);
        }
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder(RsaConst.SIGNATURE_ALGO);
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
        subject = new X500Principal(req.getCertificationRequestInfo().getSubject().toString());
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

    public static X509Certificate selfSignCsrCA(PKCS10CertificationRequest req, RsaPair keys, int days) throws RsaException {
        long nowMillis = System.currentTimeMillis();
        Date notBefore = new Date(nowMillis);
        Date notAfter = new Date((long) days * 24 * 60 * 60 * 1000 + nowMillis);
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
        X500Name subj = X500Name.getInstance(req.getCertificationRequestInfo().getSubject());
        X500Name issuer = X500Name.getInstance(req.getCertificationRequestInfo().getSubject());
        KeyPair kp = keys.toJavaSecurityKeyPair();
        priv = kp.getPrivate();
        pub = kp.getPublic();
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder(RsaConst.SIGNATURE_ALGO);
        sigBuilder.setProvider("BC");
        ContentSigner signer;
        try {
            signer = sigBuilder.build(priv);
        } catch (OperatorCreationException ex) {
            throw new RsaException("Error creating signature", ex);
        }
        BigInteger serial = BigInteger.ONE;
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subj, pub);
        ASN1Set attrs = req.getCertificationRequestInfo().getAttributes();
        if (attrs != null) {
            for (i = 0; i < attrs.size(); i++) {
                Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
                if (attr.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    X509Extensions exts = X509Extensions.getInstance(attr.getAttrValues().getObjectAt(0));
                    for (ASN1ObjectIdentifier oid : exts.getExtensionOIDs()) {
                        X509Extension ext = exts.getExtension(oid);
                        certBuilder.addExtension(oid, ext.isCritical(), ext.getParsedValue());
                    }
                }
            }
        }
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
}
