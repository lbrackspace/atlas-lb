package org.openstack.atlas.api.resources;

import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mapper.UsageMapper;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.w3.atom.Link;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.service.domain.util.Constants.NUM_DAYS_OF_USAGE;
import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class UsageResource extends CommonDependencyProvider {

    private int loadBalancerId;
    private int accountId;

    @GET
    public Response retrieveUsage(@QueryParam("startTime") String startTimeParam, @QueryParam("endTime") String endTimeParam, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        Calendar startTime;
        Calendar endTime;
        List<Usage> usages;
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

        if ((startTimeParam != null && endTimeParam != null) && startTime.compareTo(endTime) > 0) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must specify an earlier startTime than endTime.");
        }

        try {
            limit = PaginationHelper.determinePageLimit(limit);
            offset = PaginationHelper.determinePageOffset(offset);
            usages = usageService.getUsageByAccountIdandLbId(accountId, loadBalancerId, startTime, endTime, offset, limit);
            LoadBalancerUsage loadBalancerUsage = UsageMapper.toRestApiServiceUsage(usages);
            int size = loadBalancerUsage.getLoadBalancerUsageRecords().size();
            String dateString = org.apache.commons.lang3.StringUtils.EMPTY;
            if (size > limit || offset > 0){//startTime and endTime are optional, prepare the dateString for the relativeUri
                if (startTimeParam != null && endTimeParam != null){
                    dateString = String.format("startTime=%s&endTime=%s&", startTimeParam, endTimeParam);
                } else if (startTimeParam != null){
                    dateString = String.format("startTime=%s&", startTimeParam);
                } else if (endTimeParam != null){
                    dateString = String.format("endTime=%s&", endTimeParam);
                }
            }
            if (size > limit) {
                String relativeUri = String.format("/%d/loadbalancers/%d/usage?%soffset=%d&limit=%d", accountId, loadBalancerId, dateString, PaginationHelper.calculateNextOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.NEXT, relativeUri, true);
                loadBalancerUsage.getLinks().add(nextLink);
                loadBalancerUsage.getLoadBalancerUsageRecords().remove(limit.intValue()); // Remove limit+1 item
            }
            if (offset > 0) {
                String relativeUri = String.format("/%d/loadbalancers/%d/usage?%soffset=%d&limit=%d", accountId, loadBalancerId, dateString, PaginationHelper.calculatePreviousOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.PREVIOUS, relativeUri, true);
                loadBalancerUsage.getLinks().add(nextLink);
            }
            return Response.status(200).entity(loadBalancerUsage).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("current")
    public Response retrieveCurrentUsage(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        Calendar now = Calendar.getInstance();
        Calendar nowYesterday = (Calendar) now.clone();
        nowYesterday.add(Calendar.DAY_OF_MONTH, -1);

        try {
            limit = PaginationHelper.determinePageLimit(limit);
            offset = PaginationHelper.determinePageOffset(offset);
            List<Usage> cusage = usageService.getUsageByAccountIdandLbId(accountId, loadBalancerId, nowYesterday, now, offset, limit);
            LoadBalancerUsage loadBalancerUsage = UsageMapper.toRestApiCurrentUsage(cusage);

            if (loadBalancerUsage.getLoadBalancerUsageRecords().size() > limit) {
                String relativeUri = String.format("/%d/loadbalancers/%d/usage/current?offset=%d&limit=%d", accountId, loadBalancerId, PaginationHelper.calculateNextOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.NEXT, relativeUri, true);
                loadBalancerUsage.getLinks().add(nextLink);
                loadBalancerUsage.getLoadBalancerUsageRecords().remove(limit.intValue()); // Remove limit+1 item
            }
            if (offset > 0) {
                String relativeUri = String.format("/%d/loadbalancers/%d/usage/current?offset=%d&limit=%d", accountId, loadBalancerId, PaginationHelper.calculatePreviousOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.PREVIOUS, relativeUri, true);
                loadBalancerUsage.getLinks().add(nextLink);
            }
            return Response.status(200).entity(loadBalancerUsage).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    public int getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(int loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
