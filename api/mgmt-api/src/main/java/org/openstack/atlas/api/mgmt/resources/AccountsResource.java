package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.PaginationHelper;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mapper.UsageMapper;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountBillings;
import org.openstack.atlas.service.domain.entities.AccountUsage;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.w3.atom.Link;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class AccountsResource extends ManagementDependencyProvider {

    private AccountResource accountResource;

    public void setAccountResource(AccountResource accountResource) {
        this.accountResource = accountResource;
    }

    @Path("{id:[1-9][0-9]*}")
    public AccountResource retrieveAccountResource(@PathParam("id") int id) {
        accountResource.setId(id);
        return accountResource;
    }

    @GET
    @Path("usage")
    public Response retrieveAllAccountUsage(@QueryParam("startTime") String startTimeParam, @QueryParam("endTime") String endTimeParam, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support,billing")) {
            return ResponseFactory.accessDenied();
        }

        Calendar startTime;
        Calendar endTime;
        List<AccountUsage> domainAccountUsageList;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecords accountUsageRecords = new org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecords();

        if (startTimeParam == null || endTimeParam == null) {
            final String badRequestMessage = "'startTime' and 'endTime' query parameters are required";
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
        } else {
            try {
                startTime = isoTocal(startTimeParam);
                endTime = isoTocal(endTimeParam);
            } catch (ConverterException ex) {
                final String badRequestMessage = "Date parameters must follow ISO-8601 (yyyy-MM-dd'T'HH:mm:ss) format";
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, badRequestMessage);
            }
        }

        try {
            limit = PaginationHelper.determinePageLimit(limit);
            offset = PaginationHelper.determinePageOffset(offset);
            domainAccountUsageList = accountUsageRepository.getAccountUsageRecords(startTime, endTime, offset, limit);

            for (AccountUsage accountUsageRecord : domainAccountUsageList) {
                accountUsageRecords.getAccountUsageRecords().add(getDozerMapper().map(accountUsageRecord, org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountUsageRecord.class));
            }

            if (accountUsageRecords.getAccountUsageRecords().size() > limit) {
                String relativeUri = String.format("/management/accounts/usage?startTime=%s&endTime=%s&offset=%d&limit=%d", startTimeParam, endTimeParam, PaginationHelper.calculateNextOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.NEXT, relativeUri);
                accountUsageRecords.getLinks().add(nextLink);
                accountUsageRecords.getAccountUsageRecords().remove(limit.intValue()); // Remove limit+1 item
            }

            if (offset > 0) {
                String relativeUri = String.format("/management/accounts/usage?startTime=%s&endTime=%s&offset=%d&limit=%d", startTimeParam, endTimeParam, PaginationHelper.calculatePreviousOffset(offset, limit), limit);
                Link nextLink = PaginationHelper.createLink(PaginationHelper.PREVIOUS, relativeUri);
                accountUsageRecords.getLinks().add(nextLink);
            }

            return Response.status(200).entity(accountUsageRecords).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @GET
    @Path("billing")
    public Response retrieveAccountBilling(@QueryParam("startTime") String startTimeString, @QueryParam("endTime") String endTimeString) {
        if (!isUserInRole("cp,ops,support,billing")) {
            return ResponseFactory.accessDenied();
        }

        if (startTimeString == null || endTimeString == null) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must provide startTime and endTime query parameters");
        }

        Collection<org.openstack.atlas.service.domain.pojos.AccountBilling> dAccountBillings;
        AccountBillings rAccountBillings = new AccountBillings();
        Calendar startTime;
        Calendar endTime;

        try {
            startTime = isoTocal(startTimeString);
            endTime = isoTocal(endTimeString);

            final long timeDiff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
            final int millisecondsInADay = 86400000;

            if (timeDiff > millisecondsInADay) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Time range cannot be greater than one day.");
            }

            if (timeDiff < 0) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must specify an earlier startTime than endTime.");
            }

        } catch (ConverterException ce) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Date parameter(s) must follow ISO-8601 format.");
        }

        try {
            dAccountBillings = getLoadBalancerRepository().getAccountBillingForAllAccounts(startTime, endTime);
            for (org.openstack.atlas.service.domain.pojos.AccountBilling dAccountBilling : dAccountBillings) {
                rAccountBillings.getAccountBillings().add(UsageMapper.toDataModelAccountBilling(dAccountBilling));
            }
            return Response.status(200).entity(rAccountBillings).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }
}
