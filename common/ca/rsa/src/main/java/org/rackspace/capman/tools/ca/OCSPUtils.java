package org.rackspace.capman.tools.ca;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.ocsp.BasicOCSPResp;
import org.bouncycastle.ocsp.CertificateID;
import org.bouncycastle.ocsp.OCSPException;
import org.bouncycastle.ocsp.OCSPReq;
import org.bouncycastle.ocsp.OCSPReqGenerator;
import org.bouncycastle.ocsp.OCSPResp;
import org.rackspace.capman.tools.ca.exceptions.NotAnX509CertificateException;
import org.rackspace.capman.tools.ca.primitives.OCSPResponseEvent;
import org.rackspace.capman.tools.ca.primitives.OCSPResponseContainer;
import org.rackspace.capman.tools.util.StaticHelpers;
import org.rackspace.capman.tools.util.X509Inspector;
import sun.security.provider.certpath.OCSP;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.Reason;
import sun.security.provider.certpath.OCSPResponse;

public class OCSPUtils {

    private static final SecureRandom sr;
    private static final int PAGESIZE = 4096;

    static {
        SecureRandom srTmp;
        try {
            srTmp = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException ex) {
            srTmp = new SecureRandom();
        } catch (NoSuchProviderException ex) {
            srTmp = new SecureRandom();
        }
        sr = srTmp;
    }

    public static OCSPReq newOCSPReq(X509Certificate issuerCrt, BigInteger subjectSerial) throws OCSPException {
        SecureRandom sr = new SecureRandom();
        CertificateID crtId = new CertificateID(CertificateID.HASH_SHA1, issuerCrt, subjectSerial);
        OCSPReqGenerator gen = new OCSPReqGenerator();
        gen.addRequest(crtId);
        byte[] randBytes = new byte[8];
        sr.nextBytes(randBytes);
        Vector oids = new Vector();
        Vector vals = new Vector();
        oids.add(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);
        X509Extension ext = new X509Extension(false, new DEROctetString(randBytes));
        vals.add(ext);
        X509Extensions exts = new X509Extensions(oids, vals);
        gen.setRequestExtensions(exts);
        OCSPReq req = gen.generate();
        return req;
    }

    public static RevocationStatus check(X509Certificate usrX509, X509Certificate caX509) throws IOException, CertPathValidatorException {
        return OCSP.check(usrX509, caX509);
    }

    public static RevocationStatus check(X509CertificateObject usr509obj, X509CertificateObject caX509obj) throws IOException, CertPathValidatorException {
        return check((X509Certificate) usr509obj, (X509Certificate) caX509obj);
    }

    @Deprecated
    public static OCSPResponseContainer checkCertOCSP(X509CertificateObject userCrt, X509CertificateObject caCrt) {
        OCSPResponseContainer resp = new OCSPResponseContainer();
        BigInteger userSerial = userCrt.getSerialNumber();
        OCSPReq req;
        try {
            req = newOCSPReq(userCrt, userSerial);
        } catch (OCSPException ex) {
            resp.setException(ex);
            return resp;
        }
        X509Inspector xi;
        HttpURLConnection con;
        try {
            xi = new X509Inspector(userCrt);
        } catch (NotAnX509CertificateException ex) {
            resp.setException(ex);
            resp.setOcspResponseEvent(OCSPResponseEvent.USER_CRT_WAS_NULL);
            return resp;
        }
        URI uri = xi.getOCSPUri();
        if (uri == null) {
            resp.setOcspResponseEvent(OCSPResponseEvent.NO_OCSP_URI_FOUND);
            return resp;
        }
        URL url;
        try {
            url = uri.toURL();
        } catch (MalformedURLException ex) {
            resp.setException(ex);
            resp.setOcspResponseEvent(OCSPResponseEvent.OCSP_URI_MALFORMED);
            return resp;
        }
        String uriScheme = uri.getScheme();

        if (uriScheme.equals("http")) {
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                resp.setException(ex);
                resp.setOcspResponseEvent(OCSPResponseEvent.IO_EXCEPTION_OPENING_URL);
                return resp;
            }
        } else if (uriScheme.equals("https")) {
            try {
                con = (HttpsURLConnection) url.openConnection();
            } catch (IOException ex) {
                resp.setException(ex);
                resp.setOcspResponseEvent(OCSPResponseEvent.IO_EXCEPTION_OPENING_URL);
                return resp;
            }
        } else {
            resp.setOcspResponseEvent(OCSPResponseEvent.CANT_HANDLE_SCHEMA_ON_OCSP_URI);
            return resp;
        }
        con.setRequestProperty("content-type", "application/ocsp-request");
        con.setRequestProperty("accept", "application/ocsp-response");
        con.setDoOutput(true);
        OutputStream ostream;
        DataOutputStream das;
        InputStream in;
        OCSPResp ocspResp;
        try {
            ostream = con.getOutputStream();
            das = new DataOutputStream(new BufferedOutputStream(ostream, PAGESIZE * 8));
            das.write(req.getEncoded());
            das.flush();
            das.close();
            if (con.getResponseCode() / 100 != 2) {
                resp.setOcspResponseEvent(OCSPResponseEvent.BAD_HTTP_STATUS_CODE_CALLING_OSCP_URI);
                resp.setOcspHttpResponseCode(con.getResponseCode());
            }
            in = (InputStream) con.getContent();
            ocspResp = new OCSPResp(in);
            resp.setRawOCSPResponse(ocspResp);

        } catch (IOException ex) {
            resp.setException(ex);
            resp.setOcspResponseEvent(OCSPResponseEvent.IO_EXCEPTION_SENDING_REQUEST);
            return resp;
        }
        int statusCode = ocspResp.getStatus();
        statusCode = ocspResp.getStatus();
        return resp;
    }

    public String getOCSPStatusMsg(OCSPResponseContainer rc) throws OCSPException {
        StringBuilder sb = new StringBuilder(PAGESIZE * 4);
        OCSPResp rr = rc.getRawOCSPResponse();
        BasicOCSPResp br = (BasicOCSPResp) rr.getResponseObject();

        return sb.toString();

    }

    public static String getRevocationStatusMsg(RevocationStatus revStatus) {
        StringBuilder sb = new StringBuilder(PAGESIZE * 4);
        sb.append("{");
        CertStatus certStatus = revStatus.getCertStatus();
        Reason reason = revStatus.getRevocationReason();
        Date revokeDate;
        try {
            revokeDate = revStatus.getRevocationTime();
        } catch (NullPointerException ex) {
            // If the revocationTime field is null then the accessor to getRevocationTime()
            // triggers a nullpointer bug as it attempts to cal Date.clone on a null object
            // at least as of Java 1.6
            //
            // public java.util.Date getRevocationTime();
            //  Code:
            //0:	aload_0
            //1:	getfield	#175; //Field revocationTime:Ljava/util/Date;
            //4:	invokevirtual	#192; //Method java/util/Date.clone:()Ljava/lang/Object;
            //7:	checkcast	#103; //class java/util/Date
            //10:	areturn
            revokeDate = null;
        }
        sb.append("status=");
        sb.append(certStatus.name());
        sb.append(",RevocationDate=");
        if (revokeDate == null) {
            sb.append("null");
        } else {
            Calendar cal = StaticHelpers.dateToCalendar(revokeDate);
            String revokedDateString = StaticHelpers.getCalendarString(cal);
            sb.append(revokedDateString);
        }
        sb.append(",reason=");
        if (reason == null) {
            return "null";
        } else {
            sb.append(reason.name());
        }
        sb.append("}");
        return sb.toString();
    }
}
