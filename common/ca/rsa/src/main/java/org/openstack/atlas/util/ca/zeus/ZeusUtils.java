package org.openstack.atlas.util.ca.zeus;

import org.openstack.atlas.util.ca.primitives.bcextenders.HackedProviderAccessor;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.jce.provider.JCERSAPrivateCrtKey;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.primitives.UserCrtAndImds;
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
    private Set<X509CertificateObject> roots;

    public ZeusUtils() {
        roots = new HashSet<X509CertificateObject>();
    }

    public ZeusUtils(Set<X509CertificateObject> roots) {
        this.roots = roots;
    }

    public ZeusCrtFile buildZeusCrtFileLbassValidation(String userKeyStr, String userCrtStr, String intermediates) {
        ZeusCrtFile zcf = new ZeusCrtFile();
        List<ErrorEntry> errors = zcf.getErrors();
        String zkey = "";
        String zcrt = "";
        String msg;
        X509CertificateObject userCrt = null;
        List<X509CertificateObject> imdCrts = new ArrayList<X509CertificateObject>();
        KeyPair userKey = parseKey(userKeyStr, zcf.getErrors());
        if (!zcf.hasFatalErrors()) {
            UserCrtAndImds certsMatched = findUserCert(userKey, userCrtStr, intermediates, zcf);
            imdCrts = certsMatched.getImds();
            userCrt = certsMatched.getUserCert();
        } else {
            userCrt = decodeCrt(userCrtStr, zcf);
        }

        // Make sure the user cert not accidently in the imd field by reordering
        // if the user cert is not on top


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
            for (X509CertificateObject x509obj : imdCrts) {
                try {
                    String x509Str = PemUtils.toPemString(x509obj);
                    sb.append(x509Str);
                } catch (PemException ex) {
                    errors.add(new ErrorEntry(ErrorType.COULDENT_ENCODE_CERT, "Error encodeing chain crt", true, ex));
                }
            }

            // Also append a NO_PATH_TO_ROOT error if the cert has no known root
            if (!ErrorEntry.hasFatal(errors)) {
                Set<X509CertificateObject> imdSet = new HashSet<X509CertificateObject>(imdCrts);
                X509PathBuilder<X509CertificateObject> pathBuilder = new X509PathBuilder<X509CertificateObject>(roots, imdSet);
                try {
                    X509BuiltPath<X509CertificateObject> builtPath = pathBuilder.buildPath(userCrt);
                } catch (X509PathBuildException ex) {
                    // Make NO PATH to ROOT a  non fatal error
                    errors.add(new ErrorEntry(ErrorType.NO_PATH_TO_ROOT, "Chain has no path to root", false, ex));
                }
            }

            // If there are still no errors we can set the key and crt
            if (!ErrorEntry.hasFatal(errors)) {
                zcrt = sb.toString();
                zcf.setPrivate_key(zkey);
                zcf.setPublic_cert(zcrt);
            }
        }
        return zcf;
    }

    public Set<X509CertificateObject> getRoots() {
        return roots;
    }

    public void setRoots(Set<X509CertificateObject> roots) {
        this.roots = roots;
    }

    public static KeyPair parseKey(String keyIn, List<ErrorEntry> errors) {
        KeyPair kp = null;
        List<PemBlock> blocks = PemUtils.parseMultiPem(keyIn);
        Object obj;
        if (blocks.size() < 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, KEYREQUIRED, true, null));
            return kp;
        }

        if (blocks.size() > 1) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Multiple pem blocks used in Key", true, null));
            return kp;
        }
        obj = blocks.get(0).getDecodedObject();
        if (obj == null) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Unable to parse pemblock to RSA Key", true, null));
            return kp;
        }
        if (obj instanceof JCERSAPrivateCrtKey) {
            try {
                obj = HackedProviderAccessor.newKeyPair((JCERSAPrivateCrtKey) obj);
            } catch (InvalidKeySpecException ex) {
                errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, "Error while attempting to convert key from PKCS8 to PKCS1", true, ex));
            }
        }
        if (!(obj instanceof KeyPair)) {
            String msg = String.format("%s keyobject was an unstance of %s but was expecting a %s", ERRORDECODINGKEY, obj.getClass().getName(), className(KeyPair.class));
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            return kp;
        }

        kp = (KeyPair) obj;
        if (!(kp.getPublic() instanceof JCERSAPublicKey)) {
            String msg = String.format("%s Error decoding public portion of key. Objected decoded to class %s but was expecting %s", ERRORDECODINGKEY, obj.getClass().getName(), className(JCERSAPublicKey.class));
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            kp = null;
            return kp;
        }
        if (!(kp.getPrivate() instanceof JCERSAPrivateCrtKey)) {
            String msg = String.format("%s Error decoding private portion of key. Object decoded to class %s but was expecting %s", ERRORDECODINGKEY, obj.getClass().getName(), className(JCERSAPrivateCrtKey.class));
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_KEY, msg, true, null));
            kp = null;
            return kp;
        }
        return kp;
    }

    public static X509CertificateObject parseCert(String certIn, List<ErrorEntry> errors) {
        X509CertificateObject x509obj = null;
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
        if ((obj == null) || !(obj instanceof X509CertificateObject)) {
            String msg = String.format("%s Certificate decoded to class %s but was expecting %s", ERRORDECODINGCERT, obj.getClass().getName(), className(X509CertificateObject.class));
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null));
            return x509obj;
        }
        x509obj = (X509CertificateObject) obj;
        return x509obj;
    }

    public static Set<X509CertificateObject> parseIntermediateCerts(String intermediates, List<ErrorEntry> errors) {
        ResponseWithExcpetions<Set<X509CertificateObject>> resp = X509ReaderWriter.readSet(intermediates);
        Set<X509CertificateObject> intermediateCerts = resp.getReturnObject();

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

    public static List<X509CertificateObject> decodeImd(Map<X509CertificateObject, Integer> lineMap, String imdStr, ZeusCrtFile zcf) {
        List<ErrorEntry> errors = zcf.getErrors();
        ErrorEntry errorEntry;
        List<X509CertificateObject> imdCrts = new ArrayList<X509CertificateObject>();
        X509CertificateObject x509obj;
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
            if (!(obj instanceof X509CertificateObject)) {
                msg = String.format("Object at line %d decoded to class %s but was expecting %s", block.getLineNum(), obj.getClass().getName(), className(X509CertificateObject.class));
                errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null);
                errors.add(errorEntry);
                continue;
            }
            x509obj = (X509CertificateObject) obj;
            imdCrts.add(x509obj);
            lineMap.put(x509obj, new Integer(block.getLineNum()));
        }
        return imdCrts;
    }

    private static X509CertificateObject decodeCrt(String crtStr, ZeusCrtFile zcf) {
        X509CertificateObject crt;
        ErrorEntry errorEntry;
        List<ErrorEntry> errors = zcf.getErrors();
        Object obj;
        try {
            obj = PemUtils.fromPemString(crtStr);
            if (!(obj instanceof X509CertificateObject)) {
                String msg = String.format("crt object decoded to class %s but was expecting X509CertificateObject", obj.getClass().getName(), className(X509CertificateObject.class));
                errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, msg, true, null);
                errors.add(errorEntry);
                return null;
            }
            crt = (X509CertificateObject) obj;
            return crt;
        } catch (PemException ex) {
            errorEntry = new ErrorEntry(ErrorType.UNREADABLE_CERT, "Unable to read userCrt", true, ex);
            errors.add(errorEntry);
            return null;
        }
    }

    private static String className(Class clazz) {
        if (clazz == null) {
            return null;
        }
        return clazz.getName();
    }

    private UserCrtAndImds findUserCert(KeyPair userKey, String userCrtStr, String imdsStr, ZeusCrtFile zcf) {
        X509CertificateObject userCrt = null;
        List<X509CertificateObject> imdCrts = new ArrayList<X509CertificateObject>();
        StringBuilder sb = new StringBuilder();
        if (userCrtStr == null) {
            userCrtStr = "";
        }
        if (imdsStr == null) {
            imdsStr = "";
        }
        String mashedUpCrts = userCrtStr + "\n" + imdsStr + "\n";
        List<PemBlock> blocks = PemUtils.parseMultiPem(mashedUpCrts);
        if (userKey != null) {
            for (PemBlock block : blocks) {
                Object obj = block.getDecodedObject();
                if (obj == null || !(obj instanceof X509CertificateObject)) {
                    continue;
                }
                X509CertificateObject x509 = (X509CertificateObject) obj;
                List<ErrorEntry> errors = CertUtils.validateKeyMatchesCrt(userKey, x509);
                if (errors.isEmpty()) {
                    userCrt = x509;
                } else {
                    imdCrts.add(x509);
                }
            }
        } else {
            // no userKey found can't match
            String errorMessage = "No private key found can't match against certs";
            zcf.getErrors().add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, errorMessage, true, null));
        }
        if (userCrt == null) {
            String errorMessage = "Unable to locate users certificate";
            zcf.getErrors().add(new ErrorEntry(ErrorType.KEY_CERT_MISMATCH, errorMessage, true, null));
        }
        if (userCrt == null && imdCrts.isEmpty()) {
            String errorMessage = "No certs could be found";
            zcf.getErrors().add(new ErrorEntry(ErrorType.UNREADABLE_CERT, errorMessage, true, null));
        }
        return new UserCrtAndImds(userCrt, imdCrts);
    }
}
