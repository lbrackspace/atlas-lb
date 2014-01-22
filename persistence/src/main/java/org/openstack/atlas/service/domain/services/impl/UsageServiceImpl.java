package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.Usage;
import org.openstack.atlas.service.domain.exceptions.DeletedStatusException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.services.UsageService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class UsageServiceImpl extends BaseService implements UsageService {

    @Override
    public List<Usage> getUsageByAccountIdandLbId(Integer accountId, Integer loadBalancerId, Calendar startTime, Calendar endTime) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getUsageByAccountIdandLbId(accountId, loadBalancerId, startTime, endTime);
    }
}
