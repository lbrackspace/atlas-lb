package org.openstack.atlas.api.mgmt.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.v1.SuggestedCaPath;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfStrings;
import org.openstack.atlas.service.domain.services.helpers.RootCAHelper;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.exceptions.PemException;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.ca.exceptions.X509ReaderException;
import org.openstack.atlas.util.debug.Debug;
import javax.ws.rs.core.MediaType;
import org.bouncycastle.asn1.x509.X509DefaultEntryConverter;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.RootCaList;
import org.openstack.atlas.docs.loadbalancers.api.v1.X509Description;
import org.openstack.atlas.service.domain.services.helpers.X509Helper;
import org.openstack.atlas.util.ca.util.X509BuiltPath;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import org.openstack.atlas.util.ca.zeus.ErrorType;
import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;

// SSL Tools for testing SSL cruft.
public class SSLUtilsResource extends ManagementDependencyProvider {

    private final Log LOG = LogFactory.getLog(SSLUtilsResource.class);

    @POST
    @Path("suggestpath")
    public Response suggestPKIXPath(SslTermination sslTerm) {
        SuggestedCaPath suggestedPath = new SuggestedCaPath();
        Set<X509Certificate> rootCaSet = new HashSet<X509Certificate>();
        Set<X509Certificate> imdSet = new HashSet<X509Certificate>();
        X509Certificate usrCrt;
        List<ErrorEntry> errors = new ArrayList<ErrorEntry>();

        if (sslTerm.getCa() == null || sslTerm.getCa().isEmpty()) {
            rootCaSet = RootCAHelper.getRootCASet();
        } else {
            List<X509Certificate> x509List = ZeusUtils.decodeX509s(sslTerm.getCa(), errors);
            if (ErrorEntry.hasFatal(errors)) {
                applyErrors(suggestedPath, errors);
                return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
            }
            rootCaSet = new HashSet<X509Certificate>(x509List);
        }

        if (sslTerm.getIntermediateCertificate() != null && !sslTerm.getIntermediateCertificate().isEmpty()) {
            List<X509Certificate> x509List = ZeusUtils.decodeX509s(sslTerm.getIntermediateCertificate(), errors);
            if (ErrorEntry.hasFatal(errors)) {
                applyErrors(suggestedPath, errors);
                return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
            }
            imdSet = new HashSet<X509Certificate>(x509List);
        }

        if (sslTerm.getCertificate() == null || sslTerm.getCertificate().isEmpty()) {
            errors.add(new ErrorEntry(ErrorType.UNREADABLE_CERT, "User cert is empty", true, null));
            applyErrors(suggestedPath, errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
        } else {
            X509Certificate x509 = ZeusUtils.decodeCrt(sslTerm.getCertificate(), errors);
            if (ErrorEntry.hasFatal(errors)) {
                applyErrors(suggestedPath, errors);
                return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
            }
            usrCrt = x509;
        }
        if (ErrorEntry.hasFatal(errors)) {
            applyErrors(suggestedPath, errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
        }
        X509PathBuilder<X509Certificate> pathBuilder = new X509PathBuilder(rootCaSet, imdSet);
        X509BuiltPath path;
        try {
            path = pathBuilder.buildPath(usrCrt, StaticDateTimeUtils.toDate(Calendar.getInstance()));
        } catch (X509PathBuildException ex) {
            String exMsg = Debug.getExtendedStackTrace(ex);
            errors.add(new ErrorEntry(ErrorType.NO_PATH_TO_ROOT, "No Path to RootCa found", true, ex));
            applyErrors(suggestedPath, errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
        }
        if (ErrorEntry.hasFatal(errors)) {
            applyErrors(suggestedPath, errors);
            return Response.status(Response.Status.BAD_REQUEST).entity(suggestedPath).build();
        }

        return Response.status(Response.Status.OK).entity(suggestedPath).build();
    }

    @GET
    @Path("roots")
    public Response getRoots() {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        RootCaList caList = new RootCaList();
        List<X509Certificate> rootCas = RootCAHelper.getRootCAList();
        X509Description x509des;
        int nCasOnFile = rootCas.size();
        int nCasEncoded = 0;
        for (X509Certificate rootCA : rootCas) {
            try {
                x509des = X509Helper.toX509Description(rootCA);
                caList.getRootCas().add(x509des);
                nCasEncoded++;
            } catch (X509ReaderException ex) {
            }
        }
        caList.setRootCAsEncoded(nCasEncoded);
        caList.setRootCAsOnFile(nCasOnFile);
        return Response.status(Response.Status.OK).entity(caList).build();
    }

    @GET
    @Path("reloadcas")
    public Response reloadCAs() {
        if (!isUserInRole("ops")) {
            return ResponseFactory.accessDenied();
        }
        try {
            int nRootCas = RootCAHelper.reloadCAs();
            String successMsg = String.format("reloaded %d rootCAs", nRootCas);
            return ResponseFactory.getSuccessResponse(successMsg, 200);
        } catch (IOException ex) {
            String errorMsg = String.format("Error reading file \"%s\"", RootCAHelper.getRootCaFileName());
            String exMsg = Debug.getExtendedStackTrace(ex);
            return ResponseFactory.getErrorResponse(ex, errorMsg, exMsg);
        }

    }

    public static boolean applyErrors(SuggestedCaPath path, List<ErrorEntry> errors) {
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
