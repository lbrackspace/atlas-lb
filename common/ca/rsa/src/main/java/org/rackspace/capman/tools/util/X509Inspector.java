package org.rackspace.capman.tools.util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.rackspace.capman.tools.ca.PemUtils;
import org.rackspace.capman.tools.ca.exceptions.PemException;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.X500Name;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.rackspace.capman.tools.ca.CertUtils;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderDecodeException;
import org.rackspace.capman.tools.ca.exceptions.X509ReaderNoSuchExtensionException;
import sun.security.provider.certpath.OCSP;
import sun.security.x509.AccessDescription;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

public class X509Inspector {

    private static final String X500NameFormat = "RFC2253";
    private static final String SubjKeyIdOid = "2.5.29.14";
    private static final String AuthKeyIdOid = "2.5.29.35";
    private X509CertificateObject x509obj;

    static {
        RsaConst.init();
    }

    public X509Inspector(X509CertificateObject x509obj) throws NotAnX509CertificateException {
        if (x509obj == null) {
            String msg = "Resusing to accept null object in "
                    + "call to X509Inspector(X509CertificateObject) constructor";
            throw new NotAnX509CertificateException();
        }
        this.x509obj = x509obj;
    }

    public static X509Inspector newX509Inspector(String x509PemString) throws X509ReaderDecodeException, NotAnX509CertificateException {
        String msg;
        Object obj;
        X509CertificateObject x509obj;
        try {
            obj = PemUtils.fromPemString(x509PemString);
        } catch (PemException ex) {
            throw new X509ReaderDecodeException("Error decoding pemString", ex);
        }
        try {
            x509obj = (X509CertificateObject) obj;
        } catch (ClassCastException ex) {
            msg = String.format("Error casting %s to %s", obj.getClass().getName(), "X509CertificateObject");
            throw new X509ReaderDecodeException(msg, ex);
        }
        return new X509Inspector(x509obj);
    }

    public static X509Inspector newX509Inspector(X509Certificate x509Cert) throws CertificateEncodingException, CertificateParsingException, NotAnX509CertificateException {
        byte[] encoded = x509Cert.getEncoded();
        X509CertificateStructure x509Struct = X509CertificateStructure.getInstance(encoded);
        X509CertificateObject x509obj = new X509CertificateObject(x509Struct);
        X509Inspector x509Reader = new X509Inspector(x509obj);
        return x509Reader;
    }

    // Acts as a don't repeat your self base method. <Rolls Eyes>
    private String getCN(X500Principal x500principal) throws IOException {
        X500Name x500name = new X500Name(x500principal.getName(X500NameFormat));
        String commonName = x500name.getCommonName();
        return commonName;
    }

    public String getSubjectCN() {
        X500Principal x500p = x509obj.getSubjectX500Principal();
        String cn;
        try {
            cn = getCN(x500p);
        } catch (IOException ex) {
            return null;
        }
        return cn;
    }

    public URI getOCSPUri() {
        URI uri = OCSP.getResponderURI((X509Certificate) x509obj);
        if (uri == null) {
            return null;
        }
        return uri;
    }

    public URI getOCSPCaUri() {
        X509CertImpl x509i;
        ObjectIdentifier caOid = AccessDescription.Ad_CAISSUERS_Id;
        try {
            x509i = X509CertImpl.toImpl((X509Certificate) x509obj);
        } catch (CertificateException ex) {
            return null;
        }
        AuthorityInfoAccessExtension aiae = x509i.getAuthorityInfoAccessExtension();
        if (aiae == null) {
            return null;
        }
        for (AccessDescription des : aiae.getAccessDescriptions()) {
            if (des.getAccessMethod().equals(caOid)) {
                sun.security.x509.GeneralName generalName = des.getAccessLocation();
                if (generalName.getType() == GeneralNameInterface.NAME_URI) {
                    URIName uriName = (URIName) generalName.getName();
                    return uriName.getURI();
                }
            }
        }
        return null;
    }

    public String getIssuerName() {
        String issuer = x509obj.getIssuerX500Principal().getName(X500NameFormat);
        return issuer;
    }

    public String getSubjectName() {
        String subject = x509obj.getSubjectX500Principal().getName(X500NameFormat);
        return subject;
    }

    public String getIssuerCN() {
        X500Principal x500p = x509obj.getIssuerX500Principal();
        String cn;
        try {
            cn = getCN(x500p);
        } catch (IOException ex) {
            return null;
        }
        return cn;
    }

    public X509CertificateObject getX509CertificateObject() {
        return x509obj;
    }

    public X509Certificate getX509Certificate() {
        X509Certificate x509Certificate = (X509Certificate) x509obj;
        return x509Certificate;
    }

    public BigInteger getPubModulus() {
        JCERSAPublicKey pubKey = (JCERSAPublicKey) x509obj.getPublicKey();
        BigInteger pubMod = pubKey.getModulus();
        return pubMod;
    }

    public int getPubModulusSize() {
        JCERSAPublicKey pubKey = (JCERSAPublicKey) x509obj.getPublicKey();
        BigInteger pubMod = pubKey.getModulus();
        return pubMod.bitLength();
    }

    public BigInteger getSerial() {
        BigInteger serial = x509obj.getSerialNumber();
        return serial;
    }

    public String getSubjKeyId() {
        SubjectKeyIdentifierStructure subjKIS;
        byte[] keyIdBytes;
        try {
            subjKIS = getSubjectKeyIdentifierStructure();
        } catch (X509ReaderNoSuchExtensionException ex) {
            return null;
        } catch (X509ReaderDecodeException ex) {
            return null;
        }
        keyIdBytes = subjKIS.getKeyIdentifier();
        String out = StaticHelpers.bytes2hex(keyIdBytes);
        return out;
    }

    public BigInteger getAuthKeyIdSerial() {
        BigInteger serial = BigInteger.ZERO;
        AuthorityKeyIdentifierStructure authKIS = getAKISNoExcept();
        if (authKIS == null) {
            return null;
        }
        serial = authKIS.getAuthorityCertSerialNumber();
        return serial;
    }

    public String getAuthKeyIdDirname() {
        String dirName = null;
        AuthorityKeyIdentifierStructure authKIS = getAKISNoExcept();
        if (authKIS == null) {
            return null;
        }
        GeneralNames genNames = authKIS.getAuthorityCertIssuer();
        if (genNames == null) {
            return null;
        }
        GeneralName[] nameObjs = genNames.getNames();
        for (int i = 0; i < nameObjs.length; i++) {
            if (nameObjs[i].getTagNo() == 4) {
                X509Name name = (X509Name) nameObjs[i].getName();
                dirName = name.toString();
                break;
            }
        }
        return dirName;
    }

    public String getAuthKeyId() {
        AuthorityKeyIdentifierStructure authKIS = getAKISNoExcept();
        if (authKIS == null) {
            return null;
        }
        byte[] authIdBytes;
        authIdBytes = authKIS.getKeyIdentifier();
        if (authIdBytes == null) {
            return null;
        }
        String out = StaticHelpers.bytes2hex(authIdBytes);
        return out;
    }

    private AuthorityKeyIdentifierStructure getAKISNoExcept() {
        AuthorityKeyIdentifierStructure authKIS;
        try {
            authKIS = getAuthorityKeyIdentifierStructure();
        } catch (X509ReaderNoSuchExtensionException ex) {
            return null;
        } catch (X509ReaderDecodeException ex) {
            return null;
        }
        return authKIS;
    }

    private SubjectKeyIdentifierStructure getSubjectKeyIdentifierStructure() throws X509ReaderNoSuchExtensionException, X509ReaderDecodeException {
        byte[] subjKeyIdBytes = x509obj.getExtensionValue(SubjKeyIdOid);
        if (subjKeyIdBytes == null) {
            throw new X509ReaderNoSuchExtensionException("SubjectKeyIdentifier");
        }
        SubjectKeyIdentifierStructure subjKeyId;
        try {
            subjKeyId = new SubjectKeyIdentifierStructure(subjKeyIdBytes);
        } catch (IOException ex) {
            throw new X509ReaderDecodeException("Unable to decode SubjectKeyIdentifier extension from Cert", ex);
        }
        return subjKeyId;
    }

    public PublicKey getPublicKey() {
        PublicKey pubKey = x509obj.getPublicKey();
        return pubKey;
    }

    private AuthorityKeyIdentifierStructure getAuthorityKeyIdentifierStructure() throws X509ReaderNoSuchExtensionException, X509ReaderDecodeException {
        byte[] authKeyIdBytes = x509obj.getExtensionValue(AuthKeyIdOid);
        if (authKeyIdBytes == null) {
            throw new X509ReaderNoSuchExtensionException("AuthorityKeyIdentifier");
        }
        AuthorityKeyIdentifierStructure authKeyId;
        try {
            authKeyId = new AuthorityKeyIdentifierStructure(authKeyIdBytes);
        } catch (IOException ex) {
            throw new X509ReaderDecodeException("Unable to decode AuthorityKeyIdentifier from Cert", ex);
        }
        return authKeyId;
    }

    public Calendar getNotBefore() {
        Date nb = x509obj.getNotBefore();
        if (nb == null) {
            return null;
        }
        return StaticHelpers.dateToCalendar(nb);
    }

    public Calendar getNotAfter() {
        Date na = x509obj.getNotAfter();
        if (na == null) {
            return null;
        }
        return StaticHelpers.dateToCalendar(na);
    }

    public boolean isExpired() {
        return isExpired(null);
    }

    public boolean isPremature() {
        return isPremature(null);
    }

    public boolean isExpired(Date date) {
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        return CertUtils.isCertExpired(x509obj, date);
    }

    public boolean isPremature(Date date) {
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        return CertUtils.isCertPremature(x509obj, date);
    }

    public boolean isDateValid() {
        return isDateValid(null);
    }

    public boolean isDateValid(Date date) {
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        return CertUtils.isCertDateValid(x509obj, date);

    }
}
