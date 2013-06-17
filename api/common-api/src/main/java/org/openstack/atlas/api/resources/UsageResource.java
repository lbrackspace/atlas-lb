package org.openstack.atlas.api.resources;

import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mapper.UsageMapper;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import org.openstack.atlas.util.common.exceptions.ConverterException;

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
    public Response retrieveUsage(@QueryParam("startTime") String startTimeParam, @QueryParam("endTime") String endTimeParam) {
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
            usages = usageService.getUsageByAccountIdandLbId(accountId, loadBalancerId, startTime, endTime);
            return Response.status(200).entity(UsageMapper.toRestApiServiceUsage(usages)).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("current")
    public Response retrieveCurrentUsage() {
        Calendar now = Calendar.getInstance();
        Calendar nowYesterday = (Calendar) now.clone();
        nowYesterday.add(Calendar.DAY_OF_MONTH, -1);

        try {
            List<Usage> cusage = usageService.getUsageByAccountIdandLbId(accountId, loadBalancerId, nowYesterday, now);
            return Response.status(200).entity(UsageMapper.toRestApiCurrentUsage(cusage)).build();
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
