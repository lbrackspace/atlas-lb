package org.openstack.atlas.api.mgmt.helpers;


import org.openstack.atlas.docs.loadbalancers.api.management.v1.CertInfo;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.StringUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderDecodeException;
import org.openstack.atlas.util.ca.primitives.PemBlock;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CertInfoUtils {

    private static final ZeusUtils zu;

    static {
        zu = new ZeusUtils();
    }

    public static CertInfo parseCertInfo(String crt) {
        X509Inspector xi = null;
        CertInfo certInfo = null;
        if (crt == null) {
            return certInfo;
        }
        certInfo = new CertInfo();
        certInfo.setCertPem(crt);
        try {
            xi = X509Inspector.newX509Inspector(crt);
        } catch (X509ReaderDecodeException ex) {
            return certInfo;
        } catch (NotAnX509CertificateException ex) {
            return certInfo;
        }
        certInfo.setCertReadble(Boolean.TRUE);
        certInfo.setSubjectName(xi.getSubjectName());
        certInfo.setIssuerName(xi.getIssuerName());
        certInfo.setNotBefore(xi.getNotBefore());
        Calendar na = xi.getNotAfter();
        certInfo.setNotAfter(na);
        if (na != null) {
            ZonedDateTime now = StaticDateTimeUtils.nowDateTime(true);
            ZonedDateTime naDr = StaticDateTimeUtils.toDateTime(na, true);
            double secs = StaticDateTimeUtils.secondsBetween(now, naDr);
            certInfo.setDaysTillExpires(secs / (24.0 * 60.0 * 60.0));
        }
        return certInfo;
    }

    public static List<String> splitImds(String imdBlob) {
        ZeusCrtFile zcf = new ZeusCrtFile();
        List<String> imds = new ArrayList<String>();
        List<PemBlock> blocks = PemUtils.parseMultiPem(imdBlob);
        for (PemBlock block : blocks) {
            if (block.getDecodedObject() != null) {
                String pemCrt = StringUtils.asciiString(block.getPemData());
                imds.add(pemCrt);
            }
        }
        return imds;
    }
}
