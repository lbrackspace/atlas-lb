package org.openstack.atlas.service.domain.services.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.exceptions.BadRequestException;
import org.openstack.atlas.service.domain.exceptions.EntityNotFoundException;
import org.openstack.atlas.service.domain.exceptions.ImmutableEntityException;
import org.openstack.atlas.service.domain.exceptions.UnprocessableEntityException;
import org.openstack.atlas.service.domain.services.ContentCachingService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentCachingServiceImpl extends BaseService implements ContentCachingService {
    private final Log LOG = LogFactory.getLog(ContentCachingServiceImpl.class);

    @Override
    public boolean get(Integer accountId, Integer lbId) throws EntityNotFoundException {
        return loadBalancerRepository.getContentCachingByAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    @Transactional
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());

        if (queueLb.isContentCaching() && dbLoadBalancer.getProtocol() != LoadBalancerProtocol.HTTP) {
            String msg ="Content caching cannot be enabled for non HTTP load balancers.";
            LOG.error(msg);
            throw new BadRequestException(msg);
        }

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        }

        dbLoadBalancer.setContentCaching(queueLb.isContentCaching());
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Leaving " + getClass());
    }
}
