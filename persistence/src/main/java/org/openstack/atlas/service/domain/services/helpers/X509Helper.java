package org.openstack.atlas.service.domain.services.helpers;

import java.math.BigInteger;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstack.atlas.docs.loadbalancers.api.v1.X509Description;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderDecodeException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public class X509Helper {

    public static X509Description toX509Description(String x509str) throws X509ReaderException {
        X509Inspector xi = X509Inspector.newX509Inspector(x509str);
        return toX509Description(xi);
    }

    public static X509Description toX509Description(X509Certificate x509) throws X509ReaderException {
        X509Inspector xi;
        try {
            xi = X509Inspector.newX509Inspector(x509);
        } catch (CertificateEncodingException ex) {
            throw new X509ReaderException(ex);
        } catch (CertificateParsingException ex) {
            throw new X509ReaderException(ex);
        } catch (NotAnX509CertificateException ex) {
            throw new X509ReaderException(ex);
        }
        return toX509Description(xi);
    }

    public static X509Description toX509Description(X509Inspector xi) {
        X509Description xd = new X509Description();
        String tmpStr;
        BigInteger bi;
        Calendar cal;
        URI uri;
        tmpStr = xi.getIssuerName();
        if (tmpStr != null) {
            xd.setIssuerName(tmpStr);
        }
        tmpStr = xi.getSubjectName();
        if (tmpStr != null) {
            xd.setSubjName(tmpStr);
        }
        bi = xi.getSerial();
        if (bi != null) {
            xd.setSerialNumber(bi.toString(16));
        }
        tmpStr = xi.getAuthKeyIdDirname();
        if (tmpStr != null) {
            xd.setAuthKeyDirName(tmpStr);
        }
        tmpStr = xi.getAuthKeyId();
        if (tmpStr != null) {
            xd.setAuthKeyId(tmpStr);
        }
        tmpStr = xi.getSubjKeyId();
        if (tmpStr != null) {
            xd.setSubjKeyId(tmpStr);
        }
        uri = xi.getOCSPCaUri();
        if (uri != null) {
            xd.setOcspUri(uri.toString());
        }
        uri = xi.getOCSPCaUri();
        if (uri != null) {
            xd.setOcspCaUri(uri.toString());
        }
        try {
            String pem = PemUtils.toPemString(xi.getX509Certificate());
            xd.setPem(pem);
        } catch (PemException ex) {
        }

        cal = xi.getNotBefore();
        if (cal != null) {
            tmpStr = StaticDateTimeUtils.toIso(cal);
            if (tmpStr != null) {
                xd.setNotBefore(tmpStr);
            }
        }

        cal = xi.getNotAfter();
        if (cal != null) {
            tmpStr = StaticDateTimeUtils.toIso(cal);
            if (tmpStr != null) {
                xd.setNotAfter(tmpStr);
            }
        }

        return xd;
    }

    public List<X509Certificate> decodeX509s(String x509Strs) {
        List<X509Certificate> x509s = new ArrayList<X509Certificate>();
        return x509s;
    }
}
