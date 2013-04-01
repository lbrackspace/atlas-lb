package org.openstack.atlas.service.domain.services;

import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;

import java.util.List;


public interface AlertService {
    public Alert getById(Integer id) throws EntityNotFoundException;

    public List<Alert> getByAccountId(Integer marker, Integer limit, Integer accountId, String startDate, String endDate) throws BadRequestException;

    public List<Alert> getByLoadBalancerId(Integer id);

    public List<Alert> getForAccount();

    public List<Alert> getByLoadBalancerIds(List<Integer> ids, String startDate, String endDate) throws BadRequestException;

    public List<Alert> getAtomHopperByLoadBalancersByIds(List<Integer> ids, String startDate, String endDate, String queryName) throws BadRequestException;

    public List<Alert> getAll(String status, Integer marker, Integer limit);

    public List<Alert> getAllUnacknowledged(Integer marker, Integer limit);

    public List<Alert> getAllAtomHopperUnacknowledged(String type, String name, Integer marker, Integer limit);

    public List<Alert> getByClusterId(Integer clusterId, String startDate, String endDate) throws BadRequestException;

    public List<Alert> getByAccountId(Integer accountId, String startDate, String endDate) throws BadRequestException;

}


