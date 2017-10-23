package org.openstack.atlas.util.ca.zeus;

import org.openstack.atlas.util.ca.primitives.bcextenders.HackedProviderAccessor;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.util.ResponseWithExcpetions;
import org.openstack.atlas.util.ca.util.StaticHelpers;
import org.openstack.atlas.util.ca.util.X509BuiltPath;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.ca.util.X509ReaderWriter;

public class ZeusUtils {

    private static final String USASCII = "US-ASCII";
    private static final String KEYREQUIRED = "Key Required";
    private static final String CERTREQUIRED = "Cert Required";
    private static final String MISSINGUSASCII = String.format("Missing charset %s", USASCII);
    private static final String ERRORDECODINGKEY = "Error Decoding Key";
    private static final String ERRORDECODINGCERT = "Error Decoding Cert";

    static {
        RsaConst.init();
    }
    private Set<X509CertificateHolder> roots;

    public ZeusUtils() {
        roots = new HashSet<X509CertificateHolder>();
    }

    public ZeusUtils(Set<X509CertificateHolder> roots) {
        this.roots = roots;
    }

    public ZeusCrtFile buildZeusCrtFileLbassValidation(String userKeyStr, String userCrtStr, String intermediates) {
        ZeusCrtFile zcf = new ZeusCrtFile();
        List<ErrorEntry> errors = zcf.getErrors();
        Map<X509CertificateHolder, Integer> lineMap = new HashMap<X509CertificateHolder, Integer>();
        String zkey = "";
        String zcrt = "";
        String msg;

        KeyPair userKey = parseKey(userKeyStr, zcf.getErrors());
        X509CertificateHolder userCrt = decodeCrt(userCrtStr, zcf);
        List<X509CertificateHolder> imdCrts = decodeImd(lineMap, intermediates, zcf);

        // Verify key matches cert if both are Present
        if (!zcf.containsErrorTypes(ErrorType.UNREADABLE_CERT, ErrorType.UNREADABLE_KEY)) {
            List<ErrorEntry> keyMatchErrors;
            keyMatchErrors = CertUtils.validateKeyMatchesCrt(userKey, userCrt);
            errors.addAll(keyMatchErrors);
        }

        // Verify user Crt is in valid date range
        Date now = new Date(System.currentTimeMillis());
        if (userCrt != null && CertUtils.isCertExpired(userCrt, now)) {
            Date after = userCrt.getNotAfter();
            String errorMsg = invalidDateMessage("User cert expired on", after);
            errors.add(new ErrorEntry(ErrorType.EXPIRED_CERT, errorMsg, false, null));
        }

        if (userCrt != null && CertUtils.isCertPremature(userCrt, now)) {
            Date before = userCrt.getNotBefore();
            String errorMsg = invalidDateMessage("User cert isn't valid till", before);
            errors.add(new ErrorEntry(ErrorType.PREMATURE_CERT, errorMsg, false, null));
        }

        // If their is a chain don't bother validating the chain order.
        // If there where no errors build the full ZCF object
        if (!ErrorEntry.hasFatal(errors)) {
            StringBuilder sb = new StringBuilder(4096);
            try {
                zkey = PemUtils.toPemString(userKey);
            } catch (PemException ex) {
                errors.add(new ErrorEntry(ErrorType.COULDENT_ENCODE_KEY, "Error encodeing users key", true, ex));
            }
            try {
                sb.append(PemUtils.toPemString(userCrt));
            } catch (PemException ex) {
                errors.add(new ErrorEntry(ErrorType.COULDENT_ENCODE_CERT, "Error encodeing users Crt", true, ex));
            }
            for (X509CertificateHolder x509obj : imdCrts) {
                try {
                    String x509Str = PemUtils.toPemString(x509obj);
                    sb.append(x509Str);
                } catch (PemException ex) {
                    if (lineMap.containsKey(x509obj)) {
                        int x509lineNum = lineMap.get(x509obj).intValue();
                        msg = String.format("Error encodeing chain crt at line %d", x509lineNum);
                        errors.add(new ErrorEntry(ErrorType.COULDENT_ENCODE_CERT, msg, true, ex));
                    } else {
                        errors.add(new ErrorEntry(ErrorType.COULDENT_ENCODE_CERT, "Error encodeing chain crt", true, ex));
                    }
                }
            }

            // Also append a NO_PATH_TO_ROOT error if the cert has no known root
            // If there are still no errors we can set the key and crt
            if (!ErrorEntry.hasFatal(errors)) {
                zcrt = sb.toString();
                zcf.setPrivate_key(zkey);
                zcf.setPublic_cert(zcrt);
            }
        }
        return zcf;
    }

    public Set<X509CertificateHolder> getRoots() {
        return roots;
    }

    public void setRoots(Set<X509CertificateHolder> roots) {
        this.roots = roots;
    }

    public static KeyPair parseKey(String keyIn, List<ErrorEntry> errors) {
        KeyPair kp = null;
        List<PemBlock> blocks = PemUtils.parseMultiPem(keyIn);
        Object obj;
        if (blocks.size() < 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, KEYREQUIRED, true, null));
            return null;
        }

        if (blocks.size() > 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Multiple pem blocks used in Key", true, null));
            return null;
        }
        obj = blocks.get(0).getDecodedObject();
        if (obj == null) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Unable to parse pemblock to RSA Key", true, null));
            return null;
        }
        if (obj instanceof BCRSAPrivateCrtKey) {
            try {
                obj = RSAKeyUtils.getPemKeyPair((BCRSAPrivateCrtKey) obj);
            } catch (PemException ex) {
                errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Error while attempting to convert key from PKCS8 to PKCS1", true, ex));
                return null;
            }
        }
        if (!(obj instanceof KeyPair)) {
            String msg = String.format("%s keyobject was an unstance of %s but was expecting a %s",
                    ERRORDECODINGKEY, obj.getClass().getName(),
                    KeyPair.class.getCanonicalName());
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            return null;
        }
        kp = (KeyPair) obj;
        if (!(kp.getPublic() instanceof BCRSAPublicKey)) {
            String msg = String.format("%s Error decoding public portion of key. Objected decoded to class %s but was expecting %s",
                    ERRORDECODINGKEY, obj.getClass().getName(),
                    BCRSAPublicKey.class.getCanonicalName());
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            kp = null;
            return kp;
        }
        if (!(kp.getPrivate() instanceof BCRSAPrivateCrtKey)) {
            String msg = String.format("%s Error decoding private portion of key. Object decoded to class %s but was expecting %s",
                    ERRORDECODINGKEY, obj.getClass().getName(),
                    BCRSAPrivateCrtKey.class.getCanonicalName());
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            kp = null;
            return kp;
        }
        return kp;
    }

    public static X509CertificateHolder parseCert(String certIn, List<ErrorEntry> errors) {
        X509CertificateHolder x509obj = null;
        List<PemBlock> blocks = PemUtils.parseMultiPem(certIn);
        Object obj;
        if (blocks.size() < 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, CERTREQUIRED, true, null));
            return x509obj;
        }

        if (blocks.size() > 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "userCrt contains more then one pem block", true, null));
            return x509obj;
        }
        obj = blocks.get(0).getDecodedObject();
        if ((obj == null) || !(obj instanceof X509CertificateHolder)) {
            String msg = String.format("%s Certificate decoded to class %s but was expecting %s",
                    ERRORDECODINGCERT, obj.getClass().getName(),
                    X509CertificateHolder.class.getCanonicalName());
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null));
            return x509obj;
        }
        x509obj = (X509CertificateHolder) obj;
        return x509obj;
    }

    public static Set<X509CertificateHolder> parseIntermediateCerts(String intermediates, List<ErrorEntry> errors) {
        ResponseWithExcpetions<Set<X509CertificateHolder>> resp = X509ReaderWriter.readSet(intermediates);
        Set<X509CertificateHolder> intermediateCerts = resp.getReturnObject();

        for (Throwable th : resp.getExceptions()) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, th.getMessage(), false, th));
        }

        return intermediateCerts;
    }

    private static String invalidDateMessage(String premsg, Date dateEdge) {
        String edge = StaticHelpers.getDateString(dateEdge);
        String msg = String.format("%s %s", premsg, edge);
        return msg;
    }

    public static List<X509CertificateHolder> decodeImd(Map<X509CertificateHolder, Integer> lineMap, String imdStr, ZeusCrtFile zcf) {
        List<ErrorEntry> errors = zcf.getErrors();
        ErrorEntry errorEntry;
        List<X509CertificateHolder> imdCrts = new ArrayList<X509CertificateHolder>();
        X509CertificateHolder xh;
        List<PemBlock> blocks = PemUtils.parseMultiPem(imdStr);
        String msg;
        if (imdStr == null || imdStr.length() == 0) {
            return imdCrts;
        }
        for (PemBlock block : blocks) {
            Object obj = block.getDecodedObject();
            if (obj == null) {
                msg = String.format("Empty object at line starting at line %d", block.getLineNum());
                errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null);
                errors.add(errorEntry);
                continue;
            }
            if (!(obj instanceof X509CertificateHolder)) {
                msg = String.format("Object at line %d decoded to class %s but was expecting %s",
                        block.getLineNum(), obj.getClass().getName(),
                        X509CertificateHolder.class.getCanonicalName());
                errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null);
                errors.add(errorEntry);
                continue;
            }
            xh = (X509CertificateHolder) obj;
            imdCrts.add(xh);
            lineMap.put(xh, new Integer(block.getLineNum()));
        }
        return imdCrts;
    }

    private static X509CertificateHolder decodeCrt(String crtStr, ZeusCrtFile zcf) {
        X509CertificateHolder crt;
        ErrorEntry errorEntry;
        List<ErrorEntry> errors = zcf.getErrors();
        try {
            Object obj = PemUtils.fromPemString(crtStr);
            if (!(obj instanceof X509CertificateHolder)) {
                String msg = String.format("crt object decoded to class %s but was expecting X509CertificateHolder", obj.getClass().getName(), X509CertificateHolder.class.getName());
                errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null);
                errors.add(errorEntry);
                return null;
            }
            crt = (X509CertificateHolder) obj;
        } catch (PemException ex) {
            errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to read userCrt", true, ex);
            errors.add(errorEntry);
            return null;
        }
        return crt;
    }
}
