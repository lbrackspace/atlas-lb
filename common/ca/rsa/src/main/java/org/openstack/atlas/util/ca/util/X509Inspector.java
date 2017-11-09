package org.openstack.atlas.util.ca.util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AuthorityInfoAccessExtension;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.exceptions.X509ReaderDecodeException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderNoSuchExtensionException;
import org.openstack.atlas.util.ca.primitives.Debug;
import sun.security.provider.certpath.OCSP;
import sun.security.x509.AccessDescription;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

public class X509Inspector {

    private static final String X500NameFormat = "RFC2253";
    private static final String SubjKeyIdOid = "2.5.29.14";
    private static final String AuthKeyIdOid = "2.5.29.35";
    private X509CertificateHolder x509holder;

    static {
        RsaConst.init();
    }

    public X509Inspector(X509CertificateHolder x509holder) {
        this.x509holder = x509holder;
    }

    public X509Inspector(X509Certificate x509) throws NotAnX509CertificateException {
        x509holder = CertUtils.getX509CertificateHolder(x509);
    }

    @Deprecated
    public X509Inspector(X509CertificateObject x509obj) throws NotAnX509CertificateException {
        x509holder = CertUtils.getX509CertificateHolder(x509obj);
    }
    
    public X509CertificateHolder getX509CertificateHolder(){
        return x509holder;
    }

    public static X509Inspector newX509Inspector(X509CertificateHolder xh){
        return new X509Inspector(xh);
    }
    
    public static X509Inspector newX509Inspector(String x509PemString) throws X509ReaderDecodeException, NotAnX509CertificateException {
        String msg;
        Object obj;
        X509Certificate x509;
        try {
            obj = PemUtils.fromPemString(x509PemString);
        } catch (PemException ex) {
            throw new X509ReaderDecodeException("Error decoding pemString", ex);
        }
        if(obj == null){
            throw new X509ReaderDecodeException("Pem object decoded to null");
        }
        if (!(obj instanceof X509CertificateHolder)) {
            msg = Debug.castExceptionMessage(obj.getClass(), X509CertificateHolder.class);
            throw new X509ReaderDecodeException(msg);
        }
        X509CertificateHolder x509holder = (X509CertificateHolder) obj;
        return new X509Inspector(x509holder);

    }

    public static X509Inspector newX509Inspector(X509Certificate x509Cert) throws CertificateEncodingException, CertificateParsingException, NotAnX509CertificateException {
        return new X509Inspector(x509Cert);
    }

    public String getIssuerCN() {
        try {
            String cn = IETFUtils.valueToString(x509holder.getIssuer().getRDNs(BCStyle.CN)[0].getFirst().getValue());
            return cn;
        } catch (Exception ex) {
            return null;
        }

    }

    public String getSubjectCN() {
        try {
            String cn = IETFUtils.valueToString(x509holder.getSubject().getRDNs(BCStyle.CN)[0].getFirst().getValue());
            return cn;
        } catch (Exception ex) {
            return null;
        }
    }

    public URI getOCSPUri() throws NotAnX509CertificateException {
        URI uri = OCSP.getResponderURI((X509Certificate) getX509Certificate());
        if (uri == null) {
            return null;
        }
        return uri;
    }

    public URI getOCSPCaUri() {
        X509CertificateObject obj;
        X509CertImpl x509i;
        X509Certificate x509cert;
        ObjectIdentifier caOid = AccessDescription.Ad_CAISSUERS_Id;
        try {
            x509cert = getX509Certificate();
            x509i = X509CertImpl.toImpl(x509cert);
        } catch (CertificateException ex) {
            return null;
        } catch (NotAnX509CertificateException ex) {
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
        return x509holder.getIssuer().toString();
    }

    public String getSubjectName() {
        return x509holder.getSubject().toString();
    }

    public X509Certificate getX509Certificate() throws NotAnX509CertificateException {
        return CertUtils.getX509Certificate(x509holder);
    }

    public BigInteger getPubModulus() throws RsaException {
        try {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
            SubjectPublicKeyInfo pubKeyInfo = x509holder.getSubjectPublicKeyInfo();
            BCRSAPublicKey pubKey = (BCRSAPublicKey) converter.getPublicKey(x509holder.getSubjectPublicKeyInfo());
            BigInteger pubMod = pubKey.getModulus();
            return pubMod;
        } catch (PEMException ex) {
            throw new RsaException("Unable to retrieve modulus");
        }
    }

    public int getPubModulusSize() throws RsaException {
        BigInteger pubMod;
        pubMod = getPubModulus();
        return pubMod.bitLength();
    }

    public BigInteger getSerial() {
        return x509holder.getSerialNumber();
    }

    public String getSubjKeyId() {
        Extensions exts = x509holder.getExtensions();
        SubjectKeyIdentifier subjKeyId = SubjectKeyIdentifier.fromExtensions(exts);
        if (subjKeyId == null) {
            return null;
        }
        byte[] octets = subjKeyId.getKeyIdentifier();
        String out = StaticHelpers.bytes2hex(octets);
        return out;
    }

    public String getAuthKeyId() {
        Extensions exts = x509holder.getExtensions();
        AuthorityKeyIdentifier authKeyId = AuthorityKeyIdentifier.fromExtensions(exts);

        if (authKeyId == null) {
            return null;
        }
        byte[] octets = authKeyId.getKeyIdentifier();
        String out = StaticHelpers.bytes2hex(octets);
        return out;
    }

    public BigInteger getAuthKeyIdSerial() {
        Extensions exts = x509holder.getExtensions();
        AuthorityKeyIdentifier authKeyId = AuthorityKeyIdentifier.fromExtensions(exts);
        if(authKeyId == null){
            return null;
        }
        BigInteger serial = authKeyId.getAuthorityCertSerialNumber();
        return serial;
    }

    public List<String> getAuthKeyIdDirname() {
        List<String> dirNames = new ArrayList<String>();
        AuthorityKeyIdentifier authKeyId = AuthorityKeyIdentifier.fromExtensions(x509holder.getExtensions());
        if (authKeyId == null) {
            return null;
        }
        GeneralNames genNames = authKeyId.getAuthorityCertIssuer();
        if (genNames == null) {
            return null;
        }
        GeneralName[] nameObjs = genNames.getNames();
        for (int i = 0; i < nameObjs.length; i++) {
            if (nameObjs[i].getTagNo() == 4) {
                dirNames.add(nameObjs[i].toString());
            }
        }
        return dirNames;
    }

    public PublicKey getPublicKey() throws RsaException {
        JcaPEMKeyConverter conv = new JcaPEMKeyConverter().setProvider("BC");
        try {
            return conv.getPublicKey(x509holder.getSubjectPublicKeyInfo());
        } catch (PEMException ex) {
            throw new RsaException("Unable to get public key from x509 certificate", ex);
        }
    }

    public Calendar getNotBefore() {
        return StaticHelpers.dateToCalendar(x509holder.getNotBefore());
    }

    public Calendar getNotAfter() {
        return StaticHelpers.dateToCalendar(x509holder.getNotAfter());
    }

    public boolean isExpired() throws NotAnX509CertificateException {
        return isExpired(null);
    }

    public boolean isPremature() throws NotAnX509CertificateException {
        return isPremature(null);
    }

    public boolean isExpired(Date date) throws NotAnX509CertificateException {
        X509Certificate x509 = getX509Certificate();
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        return CertUtils.isCertExpired(x509, date);
    }

    public boolean isPremature(Date date) throws NotAnX509CertificateException {
        X509Certificate x509 = getX509Certificate();
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        return CertUtils.isCertPremature(x509, date);
    }

    public boolean isDateValid() throws NotAnX509CertificateException {
        return isDateValid(null);
    }

    public boolean isDateValid(Date date) throws NotAnX509CertificateException {
        Date dateObj = (date == null) ? new Date(System.currentTimeMillis()) : date;
        X509Certificate x509 = getX509Certificate();
        return CertUtils.isCertDateValid(x509, date);
    }
    
    @Deprecated
    public X509CertificateObject getX509CertificateObject() throws NotAnX509CertificateException{
        return CertUtils.getX509CertificateObject(x509holder);
    }

}
