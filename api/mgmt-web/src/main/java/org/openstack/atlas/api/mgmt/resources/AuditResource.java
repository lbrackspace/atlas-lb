package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudit;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.LoadBalancerAudits;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfStrings;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;
import static javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class AuditResource extends ManagementDependencyProvider {

    private static Log LOG = LogFactory.getLog(AuditResource.class.getName());

    @GET
    @Path("config")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response retrieveConfigs() {
        ListOfStrings lstr = new ListOfStrings();
        lstr.getStrings().add("Test");
        return Response.status(200).entity(lstr).build();
    }

    @GET
    @Path("status")
    @Produces({APPLICATION_XML, APPLICATION_JSON, APPLICATION_ATOM_XML})
    public Response retrieveLoadBalancersByStatus(@QueryParam("status") String status, @QueryParam("changes-since") String changedSince) {
        Calendar changedCal = null;
        try {
            if (changedSince != null) {
                changedCal = isoTocal(changedSince);
            }

            LoadBalancerAudits loadBalancerAudits = new LoadBalancerAudits();
            List<LoadBalancerAudit> loadBalancerAuditList = new ArrayList<LoadBalancerAudit>();
            LoadBalancerAudit loadBalancerAudit;
            //TODO: dozer map it...
            if (status != null) {
                List<LoadBalancer> loadBalancers = loadBalancerService.getLoadBalancersForAudit(status, changedCal);
                for (LoadBalancer lb : loadBalancers) {
                    loadBalancerAudit = new LoadBalancerAudit();
                    org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts alerts = null;
                    for (Alert domainCl : alertService.getByLoadBalancerId(lb.getId())) {
                        alerts = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Alerts();
                        alerts.getAlerts().add(getDozerMapper().map(domainCl, org.openstack.atlas.docs.loadbalancers.api.management.v1.Alert.class, "SIMPLE_ALERT"));
                    }
                    loadBalancerAudit.setId(lb.getId());
                    loadBalancerAudit.setCreated(lb.getCreated());
                    loadBalancerAudit.setUpdated(lb.getUpdated());
                    loadBalancerAudit.setStatus(lb.getStatus().toString());
                    if (alerts == null) {
                        alerts = new Alerts();
                    }
                    loadBalancerAudit.getAlertAudits().add(alerts);
                    loadBalancerAudits.getLoadBalancerAudits().add(loadBalancerAudit);
                }
                return Response.status(200).entity(loadBalancerAudits).build();
            } else {
                BadRequestException badRequestException = new BadRequestException("Must supply a status to query against.");
                return ResponseFactory.getErrorResponse(badRequestException, null, null);
            }
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }
}
