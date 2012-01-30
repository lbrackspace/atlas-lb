package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.events.entities.EventType;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent;
import org.openstack.atlas.service.domain.usage.entities.LoadBalancerUsageEvent_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.List;


public interface UsageService {
    List<Usage> getUsageByAccountIdandLbId(Integer accountId, Integer loadBalancerId, Calendar startTime, Calendar endTime) throws EntityNotFoundException, DeletedStatusException;

    void createUsageEvent(LoadBalancerUsageEvent loadBalancerUsageEvent);
}
