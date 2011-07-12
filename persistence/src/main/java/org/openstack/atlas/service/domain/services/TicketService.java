package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.Ticket;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import java.util.List;


public interface TicketService {

    public Ticket createTicket(LoadBalancer queueLb) throws BadRequestException, EntityNotFoundException;
    public List<org.openstack.atlas.docs.loadbalancers.api.management.v1.Ticket> customTicketMapper(List<Ticket> tickets);
}
