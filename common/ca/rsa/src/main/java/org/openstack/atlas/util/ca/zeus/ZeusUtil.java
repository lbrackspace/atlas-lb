package org.openstack.atlas.util.ca.zeus;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.jce.provider.HackedProviderAccessor;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.exceptions.ConversionException;
import org.openstack.atlas.util.ca.exceptions.NullKeyException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.primitives.RsaPair;

public class ZeusUtil {

    private static final String USASCII = "US-ASCII";
    private static final String KEYREQUIRED = "Key Required";
    private static final String CERTREQUIRED = "Cert Required";
    private static final String MISSINGUSASCII = String.format("Missing charset %s", USASCII);
    private static final String ERRORDECODINGKEY = "Error Decoding Key";
    private static final String ERRORDECODINGCERT = "Error Decoding Cert";

    public static ZeusCertFile getCertFile(String key, String cert, String chain) {
        StringBuilder certSB = new StringBuilder(RsaConst.PAGESIZE);
        String fmt;
        String msg;
        ZeusCertFile zcf = new ZeusCertFile();
        byte[] keyPem = null;
        byte[] certPem = null;
        byte[] chainPem = null;
        PemBlock subjectBlock;
        PemBlock issuerBlock;
        List<PemBlock> pemBlocks;
        RsaPair rp = null;
        KeyPair kp = null;
        List<String> errorList = zcf.getErrorList();
        List<String> currErrors = new ArrayList<String>();
        X509CertificateObject issuerCrt = null;
        X509CertificateObject subjectCrt = null;
        String decodedStr = null;
        int i;

        if (key == null || key.length() == 0) {
            errorList.add(KEYREQUIRED);

        } else {
            try {
                keyPem = key.getBytes(USASCII);
            } catch (UnsupportedEncodingException ex) {
                errorList.add(MISSINGUSASCII);
                return zcf;
            }
        }

        if (cert == null || cert.length() == 0) {
            errorList.add(CERTREQUIRED);
        } else {
            try {
                certPem = cert.getBytes(USASCII);
            } catch (UnsupportedEncodingException ex) {
                errorList.add(MISSINGUSASCII);
                return zcf;
            }
        }

        if (chain != null && chain.length() > 0) {
            try {
                chainPem = chain.getBytes(USASCII);
            } catch (UnsupportedEncodingException ex) {
                errorList.add(MISSINGUSASCII);
            }
        }
        if (keyPem != null) {
            try {
                Object pemObj =  PemUtils.fromPem(keyPem);
                // Incase the object is returned as a JCERSAPrivateCrtKey instead of KeyPair
                if(pemObj instanceof JCERSAPrivateCrtKey){
                    pemObj = HackedProviderAccessor.newKeyPair((JCERSAPrivateCrtKey)pemObj);
                }
                kp = (KeyPair) pemObj;
                if(kp == null){
                    errorList.add(ERRORDECODINGKEY);
                    return zcf;
                }
                rp = new RsaPair(kp);
            } catch (ConversionException ex) {
                errorList.add(ERRORDECODINGKEY);
            } catch (PemException ex) {
                errorList.add(ERRORDECODINGKEY);
            } catch (ClassCastException ex) {
                errorList.add(ERRORDECODINGKEY);
            }
        }

        if (certPem != null) {
            try {
                subjectCrt = (X509CertificateObject) PemUtils.fromPem(certPem);
                if(subjectCrt == null){
                    errorList.add(ERRORDECODINGCERT);
                    return zcf;
                }
            } catch (PemException ex) {
                errorList.add(ERRORDECODINGCERT);
            } catch (ClassCastException ex) {
                errorList.add(ERRORDECODINGCERT);
            }
        }

        // verify Key matches cert
        if (rp != null && subjectCrt != null) {
            currErrors = RSAKeyUtils.verifyKeyAndCert(rp, subjectCrt);
            errorList.addAll(currErrors);
        }

        if (errorList.size() < 1) {
            zcf.setPrivate_key(key);
            zcf.setPublic_cert(cert);
        }

        if (cert != null) {
            certSB.append(cert);
        }


        if (chainPem != null) {
            pemBlocks = PemUtils.parseMultiPem(chainPem);
            if (subjectCrt == null) {
                errorList.add("Error chain cert found but no main certificate specified");
            } else {
                // Check if cert at top of chain file signs main certificate
                if (pemBlocks.size() > 0) {
                    issuerBlock = pemBlocks.get(0);
                    if (!isCert(issuerBlock.getDecodedObject())) {
                        errorList.add(String.format("Object at line %d in chain cert is not an X509 certificate(Does not sign Main certificate)", pemBlocks.get(0).getLineNum()));
                    } else {
                        issuerCrt = (X509CertificateObject) issuerBlock.getDecodedObject();
                        currErrors = CertUtils.verifyIssuerAndSubjectCert(issuerCrt, subjectCrt);
                        if (currErrors.size() > 0) {
                            fmt = "Error the certificate at line %d of the chain file ";
                            fmt += "does not sign the main certificate";
                            msg = String.format(fmt, issuerBlock.getLineNum());
                            errorList.add(msg);
                            errorList.addAll(currErrors);
                        }

                        try {
                            decodedStr = new String(issuerBlock.getPemData(), USASCII);
                            certSB.append(decodedStr);
                        } catch (UnsupportedEncodingException ex) {
                            errorList.add(MISSINGUSASCII);
                        }
                    }
                }
            }

            // Check if the certs in the chain sign each other in asscending order
            for (i = 1; i < pemBlocks.size(); i++) {
                subjectBlock = pemBlocks.get(i - 1);
                issuerBlock = pemBlocks.get(i);
                if (!isCert(subjectBlock.getDecodedObject())) {
                    fmt = "Object at line %d is not an X509 Certificate(Not readable as a Subject certificate when testing signature)";
                    msg = String.format(fmt, subjectBlock.getLineNum());
                    errorList.add(msg);
                    continue;
                }
                if (!isCert(issuerBlock.getDecodedObject())) {
                    fmt = "Object at line %d is not an X509 Certificate(Not readable as an Issuing certificate when testing signature)";
                    msg = String.format(fmt, issuerBlock.getLineNum());
                    errorList.add(msg);
                    continue;
                }
                subjectCrt = (X509CertificateObject) subjectBlock.getDecodedObject();
                issuerCrt = (X509CertificateObject) issuerBlock.getDecodedObject();
                try {
                    decodedStr = new String(issuerBlock.getPemData(), USASCII);
                    certSB.append(decodedStr);
                } catch (UnsupportedEncodingException ex) {
                    errorList.add(MISSINGUSASCII);
                }
                currErrors = CertUtils.verifyIssuerAndSubjectCert(issuerCrt, subjectCrt);
                if (currErrors.size() > 0) {
                    fmt = "Cert at line %d does not sign cert at line %d";
                    msg = String.format(fmt, issuerBlock.getLineNum(), subjectBlock.getLineNum());
                    errorList.add(msg);
                    errorList.addAll(currErrors);
                }
            }
        }

        if (rp != null) {
            try {
                byte[] priv = rp.getPrivAsPem();
                decodedStr = new String(priv, "US-ASCII");
                zcf.setPrivate_key(decodedStr);
            } catch (NullKeyException ex) {
                Logger.getLogger(ZeusUtil.class.getName()).log(Level.FINE, null, ex);
                errorList.add(ERRORDECODINGKEY);
            } catch (PemException ex) {
                Logger.getLogger(ZeusUtil.class.getName()).log(Level.FINE, null, ex);
                errorList.add(ERRORDECODINGKEY);
            } catch (UnsupportedEncodingException ex) {
                errorList.add(MISSINGUSASCII);
                return zcf;
            }
        }

        zcf.setPublic_cert(certSB.toString());
        return zcf;
    }

    public static boolean isCert(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof X509CertificateObject) {
            return true;
        } else {
            return false;
        }
    }
}
