package org.openstack.atlas.service.domain.services.impl;


import org.openstack.atlas.service.domain.events.entities.Alert;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.AlertService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertServiceImpl extends BaseService implements AlertService {
    private final Log LOG = LogFactory.getLog(AlertServiceImpl.class);

    @Override
    public Alert getById(Integer id) throws EntityNotFoundException {
        return alertRepository.getById(id);
    }

    @Override
    public List<Alert> getByAccountId(Integer marker, Integer limit, Integer id, String startDate, String endDate) throws BadRequestException {
        return alertRepository.getByAccountId(marker, limit, id, startDate, endDate);
    }

    @Override
    public List<Alert> getByLoadBalancerId(Integer id) {
        return alertRepository.getForLoadBalancer(id);
    }

    @Override public List<Alert> getForAccount() {
        return alertRepository.getForAccount();
    }

    @Override
    public List<Alert> getByLoadBalancerIds(List<Integer> ids, String startDate, String endDate) throws BadRequestException {
        return alertRepository.getByLoadBalancersByIds(ids, startDate, endDate);
    }

    @Override
    public List<Alert> getAtomHopperByLoadBalancersByIds(List<Integer> ids, String startDate, String endDate, String queryName) throws BadRequestException {
        return alertRepository.getAtomHopperByLoadBalancersByIds(ids, startDate, endDate, queryName);
    }

    @Override
    public List<Alert> getAll(String status, Integer marker, Integer limit) {
        return alertRepository.getAll(status, marker, limit);
    }

    @Override
    public List<Alert> getAllUnacknowledged(Integer marker, Integer limit) {
        return alertRepository.getAllUnacknowledged(marker, limit);
    }

    @Override
    public List<Alert> getAllAtomHopperUnacknowledged(String type, String name, Integer marker, Integer limit) {
        return alertRepository.getAllUnacknowledgedByName(type, name, marker, limit);
    }

    @Override
    public List<Alert> getByClusterId(Integer clusterId, String startDate, String endDate) throws BadRequestException {
        return alertRepository.getByClusterId(clusterId, startDate, endDate);
    }

    @Override
    public List<Alert> getByAccountId(Integer accountId, String startDate, String endDate) throws BadRequestException{
        return alertRepository.getByAccountId(accountId,startDate,endDate);
    }
}
