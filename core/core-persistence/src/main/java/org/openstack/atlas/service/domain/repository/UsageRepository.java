package org.openstack.atlas.service.domain.repository;

import org.openstack.atlas.service.domain.entity.UsageRecord;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;

import java.util.List;

public interface UsageRepository {
    List<UsageRecord> getByLoadBalancerId(Integer loadBalancerId) throws EntityNotFoundException;
}
