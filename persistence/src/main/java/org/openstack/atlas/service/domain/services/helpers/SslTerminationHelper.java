package org.openstack.atlas.service.domain.services.helpers;

import java.math.BigInteger;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.util.StringUtilities;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.StyledEditorKit.BoldAction;
import javax.ws.rs.core.Response.Status;
import org.openstack.atlas.docs.loadbalancers.api.v1.SuggestedCaPath;
import org.openstack.atlas.docs.loadbalancers.api.v1.SuggestedCaPathList;
import org.openstack.atlas.docs.loadbalancers.api.v1.X509Description;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.NotAnX509CertificateException;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;
import org.openstack.atlas.util.ca.primitives.RootIntermediateContainer;
import org.openstack.atlas.util.ca.util.X509BuiltPath;
import org.openstack.atlas.util.ca.util.X509Inspector;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

public final class SslTerminationHelper {

    protected static final Log LOG = LogFactory.getLog(SslTerminationHelper.class);
    public static final String CA_ENCODE_ERROR = "error encoding root CA to x509description";
    public static final String X509_ENCODE_ERROR = "error encoding pathX509 to x509description";

    public static boolean isModifingSslAttrsOnly(SslTermination apiSslTermination, LoadBalancer dbLoadBalancer) throws BadRequestException {
        //Validator let it through, now verify the request is for update of attributes only, skip cert validation...
        //Otherwise inform user that there is no ssl termination to update values for...
        if (apiSslTermination.getCertificate() == null && apiSslTermination.getPrivatekey() == null) {
            if (dbLoadBalancer.hasSsl()) {
                LOG.info("Updating attributes only, skipping certificate validation.");
                return true;
            } else {
                LOG.error("Cannot update values for non-existent ssl termination object...");
                throw new BadRequestException("No ssl termination to update values for.");
            }
        }
        return false;
    }

    public static boolean isProtocolSecure(LoadBalancer loadBalancer) throws BadRequestException {
        LoadBalancerProtocol protocol = loadBalancer.getProtocol();
        if (protocol == LoadBalancerProtocol.HTTPS || protocol == LoadBalancerProtocol.IMAPS
                || protocol == LoadBalancerProtocol.LDAPS || protocol == LoadBalancerProtocol.POP3S) {
            throw new BadRequestException("Can not create ssl termination on a load balancer using a secure protocol.");
        }
        if (loadBalancer.getProtocol().equals(LoadBalancerProtocol.DNS_UDP) || loadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP) || loadBalancer.getProtocol().equals(LoadBalancerProtocol.UDP_STREAM)) {
            throw new BadRequestException("Protocol UDP, UDP_STREAM and DNS_UDP cannot be configured with ssl termination. ");
        }
        return true;
    }

    public static boolean verifyUserIsNotSettingNonSecureTrafficAndReEncryption(SslTermination apiTermination, org.openstack.atlas.service.domain.entities.SslTermination dbTermination) {
        boolean isUnSettingSecureTraffic = apiTermination.isSecureTrafficOnly() != null && !apiTermination.isSecureTrafficOnly();
        boolean isSettingSecureTraffic = apiTermination.isSecureTrafficOnly() != null && apiTermination.isSecureTrafficOnly();
        boolean isSettingReencryption = apiTermination.isReEncryptionEnabled() != null && apiTermination.isReEncryptionEnabled();
        boolean isUnSettingReencryption = apiTermination.isReEncryptionEnabled() != null && !apiTermination.isReEncryptionEnabled();
        boolean dbHadRencryption = dbTermination != null && dbTermination.isReEncryptionEnabled();
        boolean dbHadSecureOnly = dbTermination != null && dbTermination.isSecureTrafficOnly();

        if (isUnSettingReencryption) {
            return true;
        }

        if (isUnSettingSecureTraffic && dbHadRencryption && !isUnSettingReencryption) {
            return false; // Rejects the case where the user is unsetting Secure traffic on a previesly reencrypting lb
        }

        if (isSettingReencryption && !dbHadSecureOnly && !isSettingSecureTraffic) {
            return false; // Reject the case where the user is adding  reencryption to
        }

        return true;


    }

    public static boolean verifyPortSecurePort(LoadBalancer loadBalancer, SslTermination apiSslTermination, Map<Integer, List<LoadBalancer>> vipPorts, Map<Integer, List<LoadBalancer>> vip6Ports) {
        LOG.info("Verifying port and secure port are unique for loadbalancer: " + loadBalancer.getId());
        if (apiSslTermination != null && apiSslTermination.getSecurePort() != null) {
            if (loadBalancer.hasSsl()
                    && loadBalancer.getSslTermination().getSecurePort() == apiSslTermination.getSecurePort()) {
                return true;
            }

            if (!vipPorts.isEmpty()) {

                if (vipPorts.containsKey(apiSslTermination.getSecurePort())) {
                    return false;
                }
            }

            if (!vip6Ports.isEmpty()) {
                if ((vip6Ports.containsKey(apiSslTermination.getSecurePort()))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void verifyCertificationCredentials(ZeusCrtFile zeusCrtFile) throws BadRequestException {
        if (zeusCrtFile.getFatalErrorList().size() > 0) {
            String errors = StringUtilities.buildDelemtedListFromStringArray(zeusCrtFile.getFatalErrorList().toArray(new String[zeusCrtFile.getFatalErrorList().size()]), ",");
            LOG.error(String.format("There was an error(s) while updating ssl termination: '%s'", errors));
            throw new BadRequestException(errors);
        }
    }

    public static org.openstack.atlas.service.domain.entities.SslTermination verifyAndApplyAttributes(SslTermination queTermination, org.openstack.atlas.service.domain.entities.SslTermination dbTermination) {
        if (dbTermination == null) {
            dbTermination = new org.openstack.atlas.service.domain.entities.SslTermination();
        }

        //Set fields to updated values
        if (queTermination.isEnabled() != null) {
            dbTermination.setEnabled(queTermination.isEnabled());
        }

        if (queTermination.isSecureTrafficOnly() != null) {
            if ((queTermination.isEnabled() != null && !queTermination.isEnabled()) || (!dbTermination.isEnabled()) && (queTermination.isSecureTrafficOnly() || dbTermination.isSecureTrafficOnly())) {
                dbTermination.setSecureTrafficOnly(false);
            } else {
                dbTermination.setSecureTrafficOnly(queTermination.isSecureTrafficOnly());
            }
        }

        if (queTermination.getSecurePort() != null) {
            dbTermination.setSecurePort(queTermination.getSecurePort());
        }

        //The certificates are either null or populated, no updating.
        if (queTermination.getCertificate() != null) {
            dbTermination.setCertificate(queTermination.getCertificate());
        }

        if (queTermination.getIntermediateCertificate() != null) {
            dbTermination.setIntermediateCertificate(queTermination.getIntermediateCertificate());
        } else {
            if (queTermination.getCertificate() != null && queTermination.getCertificate() != null) {
                dbTermination.setIntermediateCertificate(null);
            }
        }

        if (queTermination.getPrivatekey() != null) {
            dbTermination.setPrivatekey(queTermination.getPrivatekey());
        }

        if (queTermination.isReEncryptionEnabled() != null) {
            dbTermination.setReEncryptionEnabled(queTermination.isReEncryptionEnabled());
        }

        return dbTermination;
    }

    public static void cleanSSLCertKeyEntries(org.openstack.atlas.service.domain.entities.SslTermination sslTermination) {
        final String cleanRegex = "(?m)^[ \t]*\r?\n";

        String dirtyKey = sslTermination.getPrivatekey();
        String dirtyCert = sslTermination.getCertificate();
        String dirtyChain = sslTermination.getIntermediateCertificate();

        if (dirtyKey != null) {
            dirtyKey = dirtyKey.replaceAll(cleanRegex, "");
            sslTermination.setPrivatekey(dirtyKey.trim());
        }

        if (dirtyCert != null) {
            dirtyCert = dirtyCert.replaceAll(cleanRegex, "");
            sslTermination.setCertificate(dirtyCert.trim());
        }

        if (dirtyChain != null) {
            dirtyChain = dirtyChain.replaceAll(cleanRegex, "");
            sslTermination.setIntermediateCertificate(dirtyChain.trim());
        }
    }

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

    public static X509PathBuilder<X509Certificate> newPathBuilder(String rootsStr, String imdStr, List<String> errors, boolean loadDefaultCas) {
        Set<X509Certificate> rootCaSet = new HashSet<X509Certificate>();
        Set<X509Certificate> imdSet = new HashSet<X509Certificate>();

        List<ErrorEntry> zErrors = new ArrayList<ErrorEntry>();
        List<X509Certificate> rootX509List = ZeusUtils.decodeX509s(rootsStr, zErrors);
        for (ErrorEntry ee : zErrors) {
            if (ee.isFatal()) {
                errors.add(ee.getErrorDetail());
            }
        }
        for (X509Certificate x509 : rootX509List) {
            rootCaSet.add(x509);
        }
        rootCaSet.addAll(rootX509List);
        if (loadDefaultCas) {
            rootCaSet.addAll(RootCAHelper.getRootCASet());
        }
        if (errors.size() > 0) {
            return null;
        }


        List<X509Certificate> imdX509List = new ArrayList<X509Certificate>();
        if (imdStr != null && !imdStr.isEmpty()) {
            zErrors.clear();
            imdX509List = ZeusUtils.decodeX509s(imdStr, zErrors);
            for (ErrorEntry ee : zErrors) {
                if (ee.isFatal()) {
                    errors.add(ee.getErrorDetail());
                }
            }
            for (X509Certificate x509 : imdX509List) {
                imdSet.add(x509);
            }
        }
        X509PathBuilder<X509Certificate> builder = new X509PathBuilder<X509Certificate>(rootCaSet, imdSet);
        return builder;
    }

    public static SuggestedCaPathList suggestCaPaths(SslTermination sslTerm) {
        SuggestedCaPath suggestedPath = new SuggestedCaPath();
        SuggestedCaPathList suggestedPathList = new SuggestedCaPathList();
        List<String> errors = suggestedPath.getErrors();
        List<ErrorEntry> zErrors = new ArrayList<ErrorEntry>();
        suggestedPathList.setPathFound(Boolean.FALSE);
        X509BuiltPath path;
        X509PathBuilder<X509Certificate> pathBuilder;
        pathBuilder = newPathBuilder(null, sslTerm.getIntermediateCertificate(), errors, true);
        RootIntermediateContainer<X509Certificate> rootImdContainer = new RootIntermediateContainer<X509Certificate>();
        boolean atLeastOneCaPathFound = false;
        if (pathBuilder == null) {
            suggestedPath.getErrors().add("Unable initialize PKIX Path Builder");
            suggestedPathList.getSuggestedCaPaths().add(suggestedPath);
            return suggestedPathList;
        }
        if (errors.size() > 0) {
            suggestedPath.getErrors().addAll(errors);
            suggestedPathList.getSuggestedCaPaths().add(suggestedPath);
            return suggestedPathList;
        }
        String userCrtStr = sslTerm.getCertificate();
        if (userCrtStr == null || userCrtStr.isEmpty()) {
            suggestedPath.getErrors().add("User cert is empty");
            suggestedPathList.getSuggestedCaPaths().add(suggestedPath);
            return suggestedPathList;
        }

        X509Certificate usrCrt = ZeusUtils.decodeCrt(userCrtStr, zErrors);
        List<String> zErrorAsStrings = applyFatalErrorEntriesToString(zErrors);
        if (zErrorAsStrings.size() > 0) {
            suggestedPath.getErrors().addAll(zErrorAsStrings);
            suggestedPathList.getSuggestedCaPaths().add(suggestedPath);
            return suggestedPathList;
        }

        rootImdContainer = new RootIntermediateContainer<X509Certificate>();
        rootImdContainer.getRootCAs().addAll(pathBuilder.getRootCAs());
        rootImdContainer.getIntermediates().addAll(pathBuilder.getIntermediates());
        Date now = StaticDateTimeUtils.toDate(Calendar.getInstance());
        now = null;
        int numberOfRootsThatSignedCert = 0;
        while (true) {
            try {
                pathBuilder = new X509PathBuilder<X509Certificate>(rootImdContainer.getRootCAs(), rootImdContainer.getIntermediates());
                path = pathBuilder.buildPath(usrCrt, now);
                suggestedPath = toSuggestedPath(path);
                suggestedPathList.getSuggestedCaPaths().add(suggestedPath);
                atLeastOneCaPathFound = true;
                rootImdContainer.getRootCAs().remove(path.getRoot());
                if (numberOfRootsThatSignedCert > 25) {
                    throw new X509PathBuildException("To many signers found");
                }
                numberOfRootsThatSignedCert++;
            } catch (X509PathBuildException ex) {
                String exMsg = Debug.getExtendedStackTrace(ex);
                break;
            }
        }
        suggestedPathList.setPathFound(numberOfRootsThatSignedCert > 0);
        return suggestedPathList;
    }

    public static SuggestedCaPath toSuggestedPath(X509BuiltPath<X509Certificate> pojoPath) {
        SuggestedCaPath apiPath = new SuggestedCaPath();
        List<X509Certificate> x509Paths = pojoPath.getPath();
        X509Certificate ca = pojoPath.getRoot();
        try {
            X509Description rootCaDescription = toX509Description(ca);
            apiPath.setRootCa(rootCaDescription);
        } catch (X509ReaderException ex) {
            apiPath.getErrors().add(CA_ENCODE_ERROR);
        }
        int lsize = pojoPath.getPath().size();
        for (int i = 0; i < lsize; i++) {
            try {
                X509Description x509Description = toX509Description(pojoPath.getPath().get(i));
                apiPath.getPaths().add(x509Description);
            } catch (X509ReaderException ex) {
                apiPath.getErrors().add(X509_ENCODE_ERROR);
            }
        }
        return apiPath;
    }

    private static List<String> applyFatalErrorEntriesToString(List<ErrorEntry> zErrors) {
        List<String> errors = new ArrayList<String>();
        for (ErrorEntry zError : zErrors) {
            if (zError.isFatal()) {
                errors.add(zError.getErrorDetail());
            }
        }
        return errors;
    }

    private static boolean applyErrors(SuggestedCaPath path, List<ErrorEntry> errors) {
        boolean hasFatalErrors = false;
        path.getErrors().clear();
        for (ErrorEntry errorEntry : errors) {
            if (errorEntry.isFatal()) {
                hasFatalErrors = true;
                path.getErrors().add(errorEntry.getErrorDetail());
            }
        }
        return hasFatalErrors;
    }
}
