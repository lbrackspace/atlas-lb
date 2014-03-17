package org.openstack.atlas.api.resources;

import java.util.ArrayList;
import org.openstack.atlas.docs.loadbalancers.api.v1.SuggestedCaPath;
import java.util.Set;
import org.openstack.atlas.service.domain.services.helpers.RootCAHelper;
import java.util.HashSet;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;
import java.util.List;
import org.openstack.atlas.util.ca.zeus.ErrorType;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.ca.exceptions.X509PathBuildException;
import org.openstack.atlas.util.staticutils.StaticDateTimeUtils;
import java.util.Calendar;
import org.openstack.atlas.util.ca.util.X509PathBuilder;
import org.openstack.atlas.util.ca.zeus.ErrorEntry;
import java.security.cert.X509Certificate;
import org.openstack.atlas.util.ca.util.X509BuiltPath;
import org.apache.abdera.model.Feed;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.MethodNotAllowedException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class SslTerminationResource extends CommonDependencyProvider {

    private int id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createSsl(SslTermination ssl) {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.ssl_termination)) {
            return ResponseFactory.getErrorResponse(new MethodNotAllowedException("Resource not implemented yet..."), null, null);
        }

        ValidatorResult result = ValidatorRepository.getValidatorFor(SslTermination.class).validate(ssl, HttpRequestType.PUT);
        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        //need to get previous ssl termination state
        org.openstack.atlas.service.domain.entities.SslTermination previousSslTerm;
        try {
            previousSslTerm = sslTerminationService.getSslTermination(loadBalancerId, accountId);
        } catch (EntityNotFoundException e) {
            previousSslTerm = new org.openstack.atlas.service.domain.entities.SslTermination();
            previousSslTerm.setEnabled(false);
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }

        try {
            ZeusSslTermination zeusSslTermination = sslTerminationService.updateSslTermination(loadBalancerId, accountId, ssl);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setZeusSslTermination(zeusSslTermination);
            dataContainer.setPreviousSslTermination(previousSslTerm);

            SslTermination returnTermination = dozerMapper.map(zeusSslTermination.getSslTermination(), SslTermination.class);

            asyncService.callAsyncLoadBalancingOperation(Operation.UPDATE_SSL_TERMINATION, dataContainer);
            return Response.status(Response.Status.ACCEPTED).entity(returnTermination).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @DELETE
    public Response removeSsl() {
        try {
            sslTerminationService.pseudoDeleteSslTermination(loadBalancerId, accountId);
            org.openstack.atlas.service.domain.entities.SslTermination previousSslTerm = sslTerminationService.getSslTermination(loadBalancerId, accountId);

            MessageDataContainer dataContainer = new MessageDataContainer();
            dataContainer.setAccountId(accountId);
            dataContainer.setLoadBalancerId(loadBalancerId);
            dataContainer.setUserName(getUserName(requestHeaders));
            dataContainer.setPreviousSslTermination(previousSslTerm);

            asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_SSL_TERMINATION, dataContainer);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    public Response getSsl() {
        try {
            org.openstack.atlas.service.domain.entities.SslTermination sslTermination = sslTerminationService.getSslTermination(loadBalancerId, accountId);
            SslTermination returnTermination = dozerMapper.map(sslTermination, SslTermination.class);

            return Response.status(Response.Status.OK).entity(returnTermination).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", FeedType.NODE_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("loadBalancerId", loadBalancerId);
        feedAttributes.put("nodeId", id);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                nodeService.getNodeByAccountIdLoadBalancerIdNodeId(accountId, loadBalancerId, id);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    @PUT
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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
