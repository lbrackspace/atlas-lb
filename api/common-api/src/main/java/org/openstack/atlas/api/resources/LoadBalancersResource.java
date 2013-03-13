package org.openstack.atlas.api.resources;

import org.apache.abdera.model.Feed;
import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mapper.DomainToRestModel;
import org.openstack.atlas.api.mapper.UsageMapper;
import org.openstack.atlas.api.repository.ValidatorRepository;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.api.validation.context.HttpRequestType;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.docs.loadbalancers.api.v1.*;
import org.openstack.atlas.service.domain.entities.AccountLimitType;
import org.openstack.atlas.service.domain.entities.LimitType;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.operations.Operation;
import org.openstack.atlas.service.domain.pojos.LbQueryStatus;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.w3.atom.Link;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.MediaType.*;
import static org.openstack.atlas.api.atom.FeedType.PARENT_FEED;
import static org.openstack.atlas.service.domain.operations.Operation.CREATE_LOADBALANCER;
import static org.openstack.atlas.service.domain.util.Constants.NUM_DAYS_OF_USAGE;
import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class LoadBalancersResource extends CommonDependencyProvider {

    private LoadBalancerResource loadBalancerResource;
    private ProtocolsResource protocolsResource;
    private AlgorithmsResource algorithmsResource;
    private StubResource stubResource;
    private BounceResource bounceResource;
    private Integer accountId;
    private HttpHeaders requestHeaders;
    private AllowedDomainsResource allowedDomainsResource;

    @GET
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancers(@QueryParam("status") String status, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit, @QueryParam("marker") Integer marker, @QueryParam("page") Integer page, @QueryParam("changes-since") String changedSince, @QueryParam("nodeaddress") String nodeAddress) {
        if (requestHeaders.getRequestHeader("Accept").get(0).equals(APPLICATION_ATOM_XML)) {
            return getFeedResponse(page);
        }

        List<org.openstack.atlas.service.domain.entities.LoadBalancer> domainLbs;
        org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers dataModelLbs = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers();
        Calendar changedCal = null;
        LbQueryStatus qs = null;

        try {
            if (nodeAddress != null) {
                domainLbs = loadBalancerService.getLoadBalancersWithNode(nodeAddress, accountId);
                for (org.openstack.atlas.service.domain.entities.LoadBalancer domainLb : domainLbs) {
                    dataModelLbs.getLoadBalancers().add(dozerMapper.map(domainLb, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "LB_NAME_ID_STATUS"));
                }
            } else {

                if (status != null) {
                    qs = LbQueryStatus.INCLUDE;
                } else {
                    qs = LbQueryStatus.EXCLUDE;
                    status = "DELETED";
                }

                if (changedSince != null) {
                    changedCal = isoTocal(changedSince);
                }

                if (limit == null || limit < 0 || limit > 100) {
                    limit = 100;
                }

                domainLbs = loadBalancerService.getLoadbalancersGeneric(accountId, status, qs, changedCal, offset, limit, marker);

                for (org.openstack.atlas.service.domain.entities.LoadBalancer domainLb : domainLbs) {
                    dataModelLbs.getLoadBalancers().add(dozerMapper.map(domainLb, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB"));
                }
            }
            return Response.status(200).entity(dataModelLbs).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({APPLICATION_XML, APPLICATION_JSON})
    public Response createLoadBalancer(LoadBalancer loadBalancer) {
        ValidatorResult result = ValidatorRepository.getValidatorFor(LoadBalancer.class).validate(loadBalancer, HttpRequestType.POST);

        if (!result.passedValidation()) {
            return getValidationFaultResponse(result);
        }

        try {
            List<Node> nodes = loadBalancer.getNodes();
            List<String> errors = verifyNodeDomains(nodes);
            if (errors.size() > 0) {
                return getValidationFaultResponse(errors);
            }
            org.openstack.atlas.service.domain.entities.LoadBalancer domainLb = dozerMapper.map(loadBalancer, org.openstack.atlas.service.domain.entities.LoadBalancer.class);
            domainLb.setAccountId(accountId);
            if (requestHeaders != null) {
                domainLb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
            }

            virtualIpService.addAccountRecord(accountId);
            org.openstack.atlas.service.domain.entities.LoadBalancer returnLb = loadBalancerService.create(domainLb);
            asyncService.callAsyncLoadBalancingOperation(CREATE_LOADBALANCER, returnLb);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(returnLb, LoadBalancer.class)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("usage")
    public Response retrieveAccountBilling(@QueryParam("startTime") String startTimeParam, @QueryParam("endTime") String endTimeParam) {
        org.openstack.atlas.service.domain.pojos.AccountBilling daccountBilling;
        AccountBilling raccountBilling;
        Calendar startTime;
        Calendar endTime;
        final String badRequestMessage = "Date parameters must follow ISO-8601 format";

        if (endTimeParam == null) {
            endTime = Calendar.getInstance(); // Default to right now
        } else {
            try {
                endTime = isoTocal(endTimeParam);
            } catch (ConverterException ex) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
            }
        }

        if (startTimeParam == null) {
            startTime = (Calendar) endTime.clone();
            startTime.add(Calendar.DAY_OF_MONTH, -NUM_DAYS_OF_USAGE); // default to NUM_DAYS_OF_USAGE days ago
        } else {
            try {
                startTime = isoTocal(startTimeParam);
            } catch (ConverterException ex) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
            }
        }

        try {
            daccountBilling = loadBalancerService.getAccountBilling(accountId, startTime, endTime);
            raccountBilling = UsageMapper.toDataModelAccountBilling(daccountBilling);
            return Response.status(200).entity(raccountBilling).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @GET
    @Path("billable")
    public Response retrieveBillableLoadBalancers(@QueryParam("startTime") String startTimeParam, @QueryParam("endTime") String endTimeParam, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        Calendar startTime;
        Calendar endTime;
        List<org.openstack.atlas.service.domain.entities.LoadBalancer> domainLbs;
        org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers dataModelLbs = new org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers();

        if (startTimeParam == null || endTimeParam == null) {
            final String badRequestMessage = "'startTime' and 'endTime' query parameters are required";
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
        } else {
            try {
                startTime = isoTocal(startTimeParam);
                endTime = isoTocal(endTimeParam);
            } catch (ConverterException ex) {
                final String badRequestMessage = "Date parameters must follow ISO-8601 format";
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
            }
        }

        try {
            limit = PaginationHelper.determinePageLimit(limit);
            offset = PaginationHelper.determinePageOffset(offset);
            domainLbs = loadBalancerService.getLoadBalancersWithUsage(accountId, startTime, endTime, offset, limit);

            for (org.openstack.atlas.service.domain.entities.LoadBalancer domainLb : domainLbs) {
                dataModelLbs.getLoadBalancers().add(dozerMapper.map(domainLb, org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer.class, "SIMPLE_LB"));
            }

            if (dataModelLbs.getLoadBalancers().size() > limit) {
                String relativeUri = String.format("/%d/loadbalancers/billable?startTime=%s&endTime=%s&offset=%d&limit=%d", accountId, startTimeParam, endTimeParam, PaginationHelper.calculateNextOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.NEXT, relativeUri);
                dataModelLbs.getLinks().add(nextLink);
                dataModelLbs.getLoadBalancers().remove(limit.intValue()); // Remove limit+1 item
            }

            if (offset > 0) {
                String relativeUri = String.format("/%d/loadbalancers/billable?startTime=%s&endTime=%s&offset=%d&limit=%d", accountId, startTimeParam, endTimeParam, PaginationHelper.calculatePreviousOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.PREVIOUS, relativeUri);
                dataModelLbs.getLinks().add(nextLink);
            }

            return Response.status(200).entity(dataModelLbs).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @DELETE
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response deleteLoadbalancer(@QueryParam("id") Set<Integer> loadBalancerIds) {
        try {
            if (loadBalancerIds.isEmpty()) {
                BadRequestException badRequestException = new BadRequestException("Must supply one or more id's to process this request.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            Integer limit = accountLimitService.getLimit(accountId, AccountLimitType.BATCH_DELETE_LIMIT);
            if (loadBalancerIds.size() > limit) {
                BadRequestException badRequestException = new BadRequestException(String.format("Currently, the limit of accepted parameters is: %s :please supply a valid parameter list.", limit));
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }

            List<org.openstack.atlas.service.domain.entities.LoadBalancer> verifiedLbs = loadBalancerService.prepareForDelete(accountId, new ArrayList<Integer>(loadBalancerIds));

            for (org.openstack.atlas.service.domain.entities.LoadBalancer dblb : verifiedLbs) {
                if (requestHeaders != null) {
                    if (requestHeaders.getRequestHeader("X-PP-User") != null && requestHeaders.getRequestHeader("X-PP-User").size() > 0) {
                        dblb.setUserName(requestHeaders.getRequestHeader("X-PP-User").get(0));
                    }
                }
                //TODO: Use this till we update other methods to use the dataContainer
                asyncService.callAsyncLoadBalancingOperation(Operation.DELETE_LOADBALANCER, dblb);
            }

            return Response.status(202).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("{id: [-+]?[0-9][0-9]*}")
    public LoadBalancerResource retrieveLoadBalancerResource(@PathParam("id") int id) {
        loadBalancerResource.setId(id);
        loadBalancerResource.setAccountId(accountId);
        loadBalancerResource.setRequestHeaders(requestHeaders);
        return loadBalancerResource;
    }

    @Path("protocols")
    public ProtocolsResource retrieveProtocolsResource() {
        return protocolsResource;
    }

    @Path("algorithms")
    public AlgorithmsResource retrieveAlgorithmsResource() {
        return algorithmsResource;
    }

    @Path("stub")
    public StubResource retrieveStubResource() {
        return stubResource;
    }

    @Path("bounce")
    public BounceResource retrieveBounceResource() {
        return bounceResource;
    }

    @GET
    @Path("limittypes")
    public Response getAllLimitTypes() {
        List<LimitType> allLimites = accountLimitService.getAllLimitTypes();
        LimitTypes rLimitTypes = DomainToRestModel.LimitTypeList2LimitType(allLimites);
        return Response.status(200).entity(rLimitTypes).build();
    }

    @GET
    @Path("absolutelimits")
    public Response getAllLimitsForAccount() {
        Map<String, Integer> accountLimits = accountLimitService.getAllLimitsForAccount(accountId);
        Limits rLimits = DomainToRestModel.AccountLimitMap2Limits(accountLimits);
        return Response.status(200).entity(rLimits).build();
    }

    @Path("alloweddomains")
    public AllowedDomainsResource retrieveAllowedDomainsResource() {
        return allowedDomainsResource;
    }

    private Response getFeedResponse(Integer page) {
        Map<String, Object> feedAttributes = new HashMap<String, Object>();
        feedAttributes.put("feedType", PARENT_FEED);
        feedAttributes.put("accountId", accountId);
        feedAttributes.put("page", page);
        Feed feed = atomFeedAdapter.getFeed(feedAttributes);

        if (feed.getEntries().isEmpty()) {
            try {
                lbRepository.getByAccountId(accountId, null);
            } catch (Exception e) {
                return ResponseFactory.getErrorResponse(e, null, null);
            }
        }

        return Response.status(200).entity(feed).build();
    }

    public void setProtocolsResource(ProtocolsResource protocolsResource) {
        this.protocolsResource = protocolsResource;
    }

    public void setAlgorithmsResource(AlgorithmsResource algorithmsResource) {
        this.algorithmsResource = algorithmsResource;
    }

    public void setLoadBalancerResource(LoadBalancerResource loadBalancerResource) {
        this.loadBalancerResource = loadBalancerResource;
    }

    public void setStubResource(StubResource stubResource) {
        this.stubResource = stubResource;
    }

    public void setBounceResource(BounceResource bounceResource) {
        this.bounceResource = bounceResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setAllowedDomainsResource(AllowedDomainsResource allowedDomainsResource) {
        this.allowedDomainsResource = allowedDomainsResource;
    }
}
