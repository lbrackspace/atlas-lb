package org.openstack.atlas.util.ca;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Set;
//import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.openstack.atlas.util.ca.exceptions.ConversionException;
import org.openstack.atlas.util.ca.exceptions.RsaCsrException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.Debug;

public class CsrUtils {

    public static final String PASSED;
    public static final String FAILED;

    static {
        RsaConst.init();
        PASSED = "PASSED";
        FAILED = "FAILED";
    }

    public static Extension getCaExt(boolean isCa) throws RsaException {
        Vector extOids = new Vector();
        Vector extVals = new Vector();

        extOids.add(Extension.basicConstraints);
        BasicConstraints basicConstraints = new BasicConstraints(isCa);
        DEROctetString basicConstraintOctets;
        try {
            basicConstraintOctets = new DEROctetString(basicConstraints);
        } catch (IOException ex) {
            String fmt = "Unable to create new BasicConstrain CA=%s";
            throw new ConversionException(String.format(fmt, isCa), ex);
        }
        Extension basicConstraintExt = new Extension(Extension.basicConstraints,
                true, basicConstraintOctets);
        return basicConstraintExt;
    }

    public static PKCS10CertificationRequest newCsr(String subjStr,
            KeyPair kp, boolean isCa) throws RsaException {
        String fmt;
        String msg;
        PrivateKey priv = kp.getPrivate();
        PublicKey pub = kp.getPublic();
        X500Name subj = new X500Name(subjStr);
        //DERSet extensions = getCaExt(isCa);
        //req = new PKCS10CertificationRequest(RsaConst.DEFAULT_SIGNATURE_ALGO, subj, pub, extensions, priv);
        Extensions exts = new Extensions(new Extension[]{getCaExt(isCa)});
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subj, pub);
        builder = builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, exts);
        JcaContentSignerBuilder sigBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME);
        ContentSigner csBuilder;
        try {
            csBuilder = sigBuilder.build(priv);
        } catch (OperatorCreationException ex) {
            throw new ConversionException("Error creating CSR", ex);
        }
        PKCS10CertificationRequest req = builder.build(csBuilder);
        return req;
    }
    
    public static X500Name getX500SubjectNameFromCSR(PKCS10CertificationRequest req){
        CertificationRequest cr = req.toASN1Structure();
        //CertificationRequestInfo cri = cr.getCertificationRequestInfo();
        //X500Name x5n = cri.getSubject();
        X500Name x5n = req.getSubject();
        return x5n;
    }
    
    public static PublicKey getPubKeyFromCSR(PKCS10CertificationRequest req) throws RsaException {
        try {
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider(RsaConst.BC);
            SubjectPublicKeyInfo pkinfo = req.getSubjectPublicKeyInfo();
            PublicKey pubKey = keyConverter.getPublicKey(req.getSubjectPublicKeyInfo());
            return pubKey;
        } catch (IOException ex) {
            String msg = "Unable to extract pubkey from csr";
            throw new RsaException(msg, ex);
        }
    }

    public static boolean verifyCSR(PKCS10CertificationRequest req, PublicKey jPub) throws RsaCsrException {
        boolean out;
        String fmt;
        String msg;
        try {
            JcaContentVerifierProviderBuilder verifierConverter = new JcaContentVerifierProviderBuilder().setProvider(RsaConst.BC);
            ContentVerifierProvider cs = verifierConverter.build(jPub);
            return req.isSignatureValid(cs);
        } catch (OperatorCreationException ex) {
            fmt = "Could not validate due to exception\n%s\n";
            msg = String.format(fmt, StringUtils.getEST(ex));
            throw new RsaCsrException(msg, ex);

        } catch (PKCSException ex) {
            fmt = "Could not validate due to exception\n%s\n";
            msg = String.format(fmt, Debug.getExtendedStackTrace(ex));
            throw new RsaCsrException(msg, ex);
        }
    }

    public static String csrToStr(PKCS10CertificationRequest req) throws RsaException {
        int i;
        String fmt;
        String msg;
        StringBuilder sb = new StringBuilder();
        CertificationRequestInfo reqInfo = req.toASN1Structure().getCertificationRequestInfo();
        String validStr;
        PublicKey jPub;
        X500Name x500Name = req.getSubject();

        String version = reqInfo.getVersion().getValue().toString();
        jPub = getPubKeyFromCSR(req);
        //req.getSubjectPublicKeyInfo().parsePublicKey().          
        String pubStr = RSAKeyUtils.objToString(jPub);
        sb.append(String.format("%s", pubStr));
        validStr = verifyCSR(req, jPub) ? PASSED : FAILED;
        String sigName;
        ASN1ObjectIdentifier algoId;
        algoId = req.getSignatureAlgorithm().getAlgorithm();
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
        ASN1Set attrs = req.toASN1Structure().getCertificationRequestInfo().getAttributes();
        if (attrs != null) {
            for (i = 0; i < attrs.size(); i++) {
                Attribute attr = Attribute.getInstance(attrs.getObjectAt(i));
                if (attr.getAttrType().equals(
                        PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    // we found extensions on this CSR
                    Extensions exts = Extensions.getInstance(
                            attr.getAttrValues().getObjectAt(0));
                    Extension basicConstraintExt = exts.getExtension(
                            Extension.basicConstraints);
                    if (basicConstraintExt != null) {
                        BasicConstraints basicConstraints
                                = BasicConstraints.getInstance(basicConstraintExt);
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
