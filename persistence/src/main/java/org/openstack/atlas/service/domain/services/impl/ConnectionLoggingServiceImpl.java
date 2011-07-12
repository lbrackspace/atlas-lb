package org.openstack.atlas.service.domain.services.impl;


import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.ConnectionLoggingService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectionLoggingServiceImpl extends BaseService implements ConnectionLoggingService {
    private final Log LOG = LogFactory.getLog(ConnectionLoggingServiceImpl.class);

    @Override
    public boolean get(Integer accountId, Integer lbId) throws EntityNotFoundException {
        return loadBalancerRepository.getConnectionLoggingbyAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    @Transactional
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        dbLoadBalancer.setConnectionLogging(queueLb.isConnectionLogging());
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Leaving " + getClass());
    }
}
