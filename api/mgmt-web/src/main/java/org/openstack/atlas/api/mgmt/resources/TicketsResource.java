package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.docs.loadbalancers.api.management.v1.Tickets;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.management.operations.EsbRequest;
import org.openstack.atlas.api.faults.HttpResponseBuilder;
import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.repository.ValidatorRepository;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.api.validation.results.ValidatorResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.openstack.atlas.api.validation.context.HttpRequestType.POST;

public class TicketsResource extends ManagementDependencyProvider {
    private Integer loadBalancerId;

    @GET
    public Response retrieveTickets(@QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {
        if (!isUserInRole("cp,ops")) {
            return ResponseFactory.accessDenied();
        }

        Tickets apiTickets = new Tickets();

        try {
            List<Ticket> tickets = loadBalancerRepository.getTickets(loadBalancerId, offset, limit);
            for (Ticket ticket : tickets) {
                org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket apiTicket = getDozerMapper().map(ticket, org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket.class);
                apiTickets.getTickets().add(apiTicket);
            }
            return Response.status(200).entity(apiTickets).build();
        } catch (Exception e) {
            return ResponseFactory.getErrorResponse(e, null, null);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createTicket(org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket ticket) {
          if (!isUserInRole("cp,ops")) {
              return ResponseFactory.accessDenied();
          }

          ValidatorResult result = ValidatorRepository.getValidatorFor(org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket.class).validate(ticket, POST);

          if (!result.passedValidation()) {
              return Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault",
                      result.getValidationErrorMessages())).build();
          }
          try {
              LoadBalancer domainLb = new  LoadBalancer();
              domainLb.setId(loadBalancerId);
              domainLb.getTickets().add(dozerMapper.map(ticket, Ticket.class));

              EsbRequest req = new EsbRequest();
              req.setLoadBalancer(domainLb);
              Ticket ticketCreated = ticketService.createTicket(domainLb);

              org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket responseTicket = null;
              if (ticketCreated != null){
                 responseTicket = dozerMapper.map(ticketCreated, org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket.class);

              }

              return Response.status(Response.Status.OK).entity(responseTicket).build();


          } catch (Exception e) {
              return ResponseFactory.getErrorResponse(e, null, null);
          }
      }


    public void setLoadBalancerId(Integer id) {
        this.loadBalancerId = id;
    }
}
