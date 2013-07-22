package org.rackspace.capman.tools.ca;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.rackspace.capman.tools.ca.primitives.RsaConst;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import org.rackspace.capman.tools.ca.exceptions.RsaException;

public class CsrUtils {

    public static final String PASSED;
    public static final String FAILED;

    static {
        RsaConst.init();
        PASSED = "PASSED";
        FAILED = "FAILED";
    }

    public static DERSet getCaExt(boolean isCa) {
        Vector extOids = new Vector();
        Vector extVals = new Vector();

        extOids.add(X509Extension.basicConstraints);
        BasicConstraints basicConstraints = new BasicConstraints(isCa);
        DEROctetString basicConstraintOctets = new DEROctetString(
                basicConstraints);
        X509Extension basicConstraintsExt = new X509Extension(true,
                basicConstraintOctets);
        extVals.add(basicConstraintsExt);
        X509Extensions exts = new X509Extensions(extOids, extVals);
        DERSet extDerSet = new DERSet(exts);
        Attribute attr = new Attribute(
                PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extDerSet);
        DERSet ca = new DERSet(attr);
        return ca;
    }

    public static PKCS10CertificationRequest newCsr(String subjStr,
            KeyPair kp, boolean isCa) throws RsaException {
        String fmt;
        String msg;
        PKCS10CertificationRequest req;
        PrivateKey priv = kp.getPrivate();
        PublicKey pub = kp.getPublic();
        X500Principal subj = new X500Principal(subjStr);
        DERSet extensions = null;
        extensions = getCaExt(isCa);
        try {
            req = new PKCS10CertificationRequest(RsaConst.DEFAULT_SIGNATURE_ALGO, subj, pub, extensions, priv);
        } catch (GeneralSecurityException ex) {
            throw new RsaException("Error creating CSR", ex);
        }
        return req;
    }

    public static String csrToStr(PKCS10CertificationRequest req) {
        int i;
        String fmt;
        String msg;
        StringBuilder sb = new StringBuilder(RsaConst.PAGESIZE);
        CertificationRequestInfo reqInfo = req.getCertificationRequestInfo();
        String validStr;
        JCERSAPublicKey jPub;
        X509Name x509Name = reqInfo.getSubject();
        X500Name x500Name = X500Name.getInstance(x509Name);
        String version = reqInfo.getVersion().getValue().toString();
        try {
            jPub = (JCERSAPublicKey) req.getPublicKey();
            String pubStr = RSAKeyUtils.objToString(jPub);
            sb.append(String.format("%s", pubStr));
        } catch (GeneralSecurityException ex) {
            sb.append("Public Key: Could not parse public key");
        }
        try {
            validStr = (req.verify()) ? PASSED : FAILED;
        } catch (GeneralSecurityException ex) {
            fmt = "Could not validate due to exception\n%s\n";
            msg = String.format(fmt, StringUtils.getEST(ex));
            validStr = msg;
        }
        String sigName;
        DERObjectIdentifier algoId =
                (DERObjectIdentifier) req.getSignatureAlgorithm().getAlgorithm();
        sb.append(String.format("Version: %s\n", version));
        sb.append(String.format("Subject Name: %s\n", x500Name.toString()));
        sb.append(String.format("CSR validation: %s\n", validStr));
        boolean oidFound = RsaConst.oids.containsKey(algoId.toString());
        if (oidFound) {
            sigName = (String) RsaConst.oids.get(algoId.toString());
        } else {
            sigName = req.getSignatureAlgorithm().getAlgorithm().getId();
        }
        sb.append(String.format("SignatureAlgo = %s\n", sigName));
        ASN1Set attrs = req.getCertificationRequestInfo().getAttributes();
        if (attrs != null) {
            for (i = 0; i < attrs.size(); i++) {
                Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
                if (attr.getAttrType().equals(
                        PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    // we found extensions on this CSR
                    X509Extensions exts = X509Extensions.getInstance(
                            attr.getAttrValues().getObjectAt(0));
                    X509Extension basicConstraintExt = exts.getExtension(
                            X509Extension.basicConstraints);
                    if (basicConstraintExt != null) {
                        BasicConstraints basicConstraints =
                                BasicConstraints.getInstance(basicConstraintExt);
                        fmt = "BasicConstraints: CA=%s\n";
                        msg = (basicConstraints.isCA()) ? "True" : "False";
                        sb.append(String.format(fmt, msg));
                    }
                }
            }
        }

        return sb.toString();
    }
}
