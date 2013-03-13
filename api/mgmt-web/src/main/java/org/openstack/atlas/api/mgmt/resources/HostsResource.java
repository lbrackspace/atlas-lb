package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.*;
import org.openstack.atlas.docs.loadbalancers.api.v1.faults.ValidationErrors;
import org.openstack.atlas.service.domain.usage.entities.HostUsage;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.util.common.exceptions.ConverterException;
import org.openstack.atlas.api.validation.results.ValidatorResult;
import org.openstack.atlas.api.mgmt.validation.contexts.HostContext;
import org.openstack.atlas.api.mgmt.helpers.HostUsageProcessor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class HostsResource extends ManagementDependencyProvider {

    private HostResource hostResource;
    private Integer accountId;
    private Integer loadBalancerId;

    @GET
    public Response retrieveHosts(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.Host> domainHosts;
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts dataModelHosts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Hosts();
        try {
            domainHosts = hostService.getAll(offset, limit);
            for (org.openstack.atlas.service.domain.entities.Host domainHost : domainHosts) {
                dataModelHosts.getHosts().add(getDozerMapper().map(domainHost, org.openstack.atlas.docs.loadbalancers.api.management.v1.Host.class, "SIMPLE_HOST"));
            }
            return Response.status(200).entity(dataModelHosts).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createHost(Host host) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        ValidatorResult res = ValidatorRepository.getValidatorFor(Host.class).validate(host, HostContext.POST);
        expandIpv6InHost(host);
        if (!res.passedValidation()) {
            return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault", res.getValidationErrorMessages())).build();
        }

        try {
            org.openstack.atlas.service.domain.entities.Host domainHost = getDozerMapper().map(host, org.openstack.atlas.service.domain.entities.Host.class);
            hostService.create(domainHost);
            return Response.status(Response.Status.ACCEPTED).entity(dozerMapper.map(domainHost, Host.class, "SIMPLE_HOST")).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @GET
    @Path("usage")
    public Response retrieveZeusUsage(@QueryParam("startDate") String startDateParam, @QueryParam("endDate") String endDateParam) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }

        if (startDateParam == null || endDateParam == null) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must provide startDate and endDate query parameters");
        }

        Calendar startTime;
        Calendar endTime;

        try {
            startTime = isoTocal(startDateParam);
            endTime =  isoTocal(endDateParam);

            final long timeDiff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
            final long millisecondsIn31Days = 2678400000l;

            if (timeDiff > millisecondsIn31Days) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Time range cannot be greater than 31 days.");
            }

            if (timeDiff < 0) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must specify an earlier startDate than endDate.");
            }
        } catch (ConverterException ex) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Date parameter must follow ISO-8601 format.");
        }

        try {
            org.openstack.atlas.service.domain.pojos.HostUsageList hostUsageList = new org.openstack.atlas.service.domain.pojos.HostUsageList();

            List<HostUsage> rawUsageList = hostUsageRepository.getByDateRange(startTime, endTime);
            List<org.openstack.atlas.service.domain.pojos.HostUsageRecord> hostUsageRecords = HostUsageProcessor.processRawHostUsageData(rawUsageList);
            hostUsageList.setHostUsageRecords(hostUsageRecords);

            org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageList displayList = getDozerMapper().map(hostUsageList, org.openstack.atlas.docs.loadbalancers.api.management.v1.HostUsageList.class);
            return Response.status(200).entity(displayList).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    // According to Jira:https://jira.mosso.com/browse/SITESLB-235                                                      
    @POST
    @Path("customers")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCustomersList(ByIdOrName idOrName) {
        if (!isUserInRole("cp,ops,support")) {
            return ResponseFactory.accessDenied();
        }
        Object key;
        List<org.openstack.atlas.service.domain.pojos.Customer> dcustomerList;
        CustomerList rcustomerList = new CustomerList();
        Customer rcustomer;

        try {
            if (idOrName.getId() == null && idOrName.getName() != null) {
                key = (String) idOrName.getName();
                dcustomerList = hostService.getCustomerList(key);
            } else if (idOrName.getId() != null && idOrName.getName() == null) {
                key = (Integer) idOrName.getId();
                dcustomerList = hostService.getCustomerList(key);
            } else {
                ValidationErrors validationFault = new ValidationErrors();
                String errMsg = "Choose only the Id attribute or Name attribute, but not both. Using neither is also invalid.";
                validationFault.getMessages().add(errMsg);
                return Response.status(400).entity(validationFault).build();
            }
            for (org.openstack.atlas.service.domain.pojos.Customer dcustomer : dcustomerList) {
                rcustomer = new Customer();
                rcustomer.setAccountId(dcustomer.getAccountId());
                for (org.openstack.atlas.service.domain.entities.LoadBalancer dloadbalancer : dcustomer.getLoadBalancers()) {
                    rcustomer.getLoadBalancers().add(getDozerMapper().map(dloadbalancer, LoadBalancer.class, "SIMPLE_CUSTOMER_LB"));
                }
                rcustomerList.getCustomers().add(rcustomer);
            }

            return Response.status(200).entity(rcustomerList).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }

    @Path("backups")
    @GET
    public Response retrieveBackups() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        List<org.openstack.atlas.service.domain.entities.Backup> domainBackups;
        Backups apiBackups = new Backups();
        try {
            domainBackups = getHostRepository().getAllBackups();
            for (org.openstack.atlas.service.domain.entities.Backup domainBackup : domainBackups) {
                apiBackups.getBackups().add(getDozerMapper().map(domainBackup, Backup.class, "FULL_BACKUP"));
            }
            return Response.status(200).entity(apiBackups).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @Path("capacityreport")
    @GET
    public Response getAllHostCapacityReports() {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }
        HostCapacityReports mHcrs = new HostCapacityReports();
        HostCapacityReport mHcr = new HostCapacityReport();
        org.openstack.atlas.service.domain.entities.LoadBalancer dLb = new org.openstack.atlas.service.domain.entities.LoadBalancer();

        try {
            List<org.openstack.atlas.service.domain.entities.Host> dHosts = hostService.getAll();
            Host rHost = getDozerMapper().map(dHosts, Host.class);
            mHcr.setHostName(rHost.getName());
            mHcr.setHostId(rHost.getId());
            mHcr.setTotalConcurrentConnectionCapacity(rHost.getMaxConcurrentConnections());

            mHcrs.getHostCapacityReports().add(mHcr);

            return Response.status(200).entity(mHcrs).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }



    @Path("{id: [1-9][0-9]*}")
    public HostResource retrieveHostResource(@PathParam("id") int id) {
        hostResource.setId(id);
        return hostResource;
    }

    public void setHostResource(HostResource hostResource) {
        this.hostResource = hostResource;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setLoadBalancerId(Integer loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }
}
