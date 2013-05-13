package org.openstack.atlas.service.domain.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl extends BaseService implements TicketService {
    private final Log LOG = LogFactory.getLog(TicketServiceImpl.class);

    @Override
    @Transactional
    public Ticket createTicket(LoadBalancer queueLb) throws BadRequestException, EntityNotFoundException {
        LOG.debug("Entering " + getClass());


        LoadBalancer dbLoadBalancer;
        Ticket ticket;

        try {
            dbLoadBalancer = loadBalancerRepository.getById(queueLb.getId());
        } catch (EntityNotFoundException enfe) {
            LOG.warn("EntityNotFoundException thrown. Sending error response to client...");
            throw new EntityNotFoundException(
                    String.format("Loadbalancer with id #%d not found", queueLb.getId()));

        }

        if (!queueLb.getTickets().isEmpty()) {
            Ticket ticketToAdd = queueLb.getTickets().iterator().next();
            LOG.info(String.format("Adding ticket with ticketId '%s' for load balancer '%d'", ticketToAdd.getTicketId(), queueLb.getId()));
            ticket = loadBalancerRepository.createTicket(dbLoadBalancer, ticketToAdd);
            LOG.info(String.format("Successfully added ticket with ticketId '%s' for load balancer '%d' in the database", ticketToAdd.getTicketId(), queueLb.getId()));

        } else {
            LOG.error("No ticket specified. Sending error response to client...");
            throw new BadRequestException("No ticket specified.");
        }

        LOG.info(String.format("Create ticket operation successfully completed for load balancer '%d'", queueLb.getId()));
        return ticket;
    }

    @Override
    public List<org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket> customTicketMapper(List<Ticket> tickets) {
        List<org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket> ticketList = new ArrayList<org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket>();
        org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket ticket_v1;
        for (Ticket ticket : tickets) {
            ticket_v1 = new org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket();
            ticket_v1.setTicketId(ticket.getTicketId());
            ticket_v1.setComment(ticket.getComment());
            ticket_v1.setId(ticket.getId());
            ticketList.add(ticket_v1);
        }
        return ticketList;
    }
}
