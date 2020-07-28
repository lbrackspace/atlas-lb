package org.openstack.atlas.api.resources;

import org.apache.abdera.model.Feed;
import org.apache.commons.lang3.StringUtils;
import org.openstack.atlas.api.atom.FeedType;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.Ciphers;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.MethodNotAllowedException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.MessageDataContainer;
import org.openstack.atlas.service.domain.pojos.ZeusSslTermination;
import javax.ws.rs.PUT;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import org.openstack.atlas.util.ca.zeus.ZeusCrtFile;
import org.openstack.atlas.util.ca.zeus.ZeusUtils;

public class SslTerminationResource extends CommonDependencyProvider {

    private int id;
    private Integer accountId;
    private Integer loadBalancerId;
    private HttpHeaders requestHeaders;

    private CertificateMappingsResource certificateMappingsResource;

    protected String REST = "REST";


    @PUT
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createSsl(SslTermination ssl) {
        ZeusUtils zeusUtils = new ZeusUtils();
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

        // Use database as default values. Also getSslTermination already does a null check :)
        String pemKey = previousSslTerm.getPrivatekey();
        String imdCrts = previousSslTerm.getIntermediateCertificate();
        String userCrt = previousSslTerm.getCertificate();

        // But if the values are present in the Rest Object they override the default values
        if (ssl.getPrivatekey() != null) {
            pemKey = ssl.getPrivatekey();
        }
        if (ssl.getIntermediateCertificate() != null) {
            imdCrts = ssl.getIntermediateCertificate();
        }
        if (ssl.getCertificate() != null) {
            userCrt = ssl.getCertificate();
        }
        ZeusCrtFile zcf = zeusUtils.buildZeusCrtFileLbassValidation(pemKey, userCrt, imdCrts);
        if (zcf.hasFatalErrors()) {
            Response resp = getValidationFaultResponse(zcf.getFatalErrorList());
            return resp;
        }

        try {
            ZeusSslTermination zeusSslTermination = sslTerminationService.updateSslTermination(loadBalancerId, accountId, ssl, false);

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

    @Path("certificatemappings")
    public CertificateMappingsResource retrieveCertificateMappingsResource() {
        certificateMappingsResource.setAccountId(accountId);
        certificateMappingsResource.setLoadBalancerId(loadBalancerId);
        certificateMappingsResource.setRequestHeaders(requestHeaders);
        return certificateMappingsResource;
    }

    @GET
    @Path("ciphers")
    @Produces({APPLICATION_XML, APPLICATION_JSON})
    /**
     * End point url to fetch the list of ciphers enabled for the load balancer.
     */
    public Response retrieveSupportedCiphers() {
        try {
            org.openstack.atlas.service.domain.entities.SslTermination dbSslTermination = sslTerminationService.getSslTermination(loadBalancerId, accountId);
            String cipherList = StringUtils.EMPTY;
            if (dbSslTermination != null) {
                if (StringUtils.isNotBlank(dbSslTermination.getCipherList())) {
                    cipherList = dbSslTermination.getCipherList();
                } else {
                    // Defaults not set by API grab defaults from vtm global settings
                    //TODO need to confirm if we need to get this from the cache or some configuration.

                    if (restApiConfiguration.getString(PublicApiServiceConfigurationKeys.adapter_soap_rest) != null
                            && restApiConfiguration.getString(PublicApiServiceConfigurationKeys.adapter_soap_rest).equalsIgnoreCase(REST)) {
                        cipherList = reverseProxyLoadBalancerVTMService.getSsl3CiphersForLB(loadBalancerId);
                    } else {
                        cipherList = reverseProxyLoadBalancerService.getSsl3CiphersForLB(loadBalancerId);
                    }
                }
            }
            //Convert the list into JAXB pojo Ciphers.java
            Ciphers supportedCiphers = dozerMapper.map(cipherList, Ciphers.class);
            //supportedCiphers.setCipherList(cipherList);
            return Response.status(Response.Status.OK).entity(supportedCiphers).build();
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

    public void setCertificateMappingsResource(CertificateMappingsResource certificateMappingsResource) {
        this.certificateMappingsResource = certificateMappingsResource;
    }
}
