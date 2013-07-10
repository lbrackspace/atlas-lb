package org.openstack.atlas.service.domain.services.impl;

import org.openstack.atlas.service.domain.entities.*;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.RateLimitingService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Service
public class RateLimitingServiceImpl extends BaseService implements RateLimitingService {
    private final Log LOG = LogFactory.getLog(RateLimitingServiceImpl.class);

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public RateLimit get(Integer id) throws EntityNotFoundException, DeletedStatusException {
        return loadBalancerRepository.getRateLimitByLoadBalancerId(id);
    }

    @Override
    public List<RateLimit> retrieveLoadBalancerRateLimits() {
        return loadBalancerRepository.getRateLimitByExpiration();
    }

    @Override
    @Transactional
    public void create(LoadBalancer dblb) throws UnprocessableEntityException, EntityNotFoundException, BadRequestException, ImmutableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(dblb.getId());
        dblb.setAccountId(dbLoadBalancer.getAccountId());

        if (isRateLimitValid(dblb)) {
             LOG.debug(String.format("Rate limit is invalid, expirationTime cannot be set prior to the current time."));
            throw new BadRequestException("Must provide valid expiration time. cannot be set prior to the current time.");
        }

        if(dbLoadBalancer.getRateLimit() != null) {
            LOG.debug(String.format("Rate limit already exists for loadbalancer %d.", dblb.getId()));
            throw new BadRequestException("A rate limit already exists.");
        }

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);

        }
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Adding the rate limit to the database...");
        loadBalancerRepository.createRateLimit(dbLoadBalancer, dblb.getRateLimit());

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class, BadRequestException.class})
    public void update(LoadBalancer dbLb) throws EntityNotFoundException, UnprocessableEntityException, BadRequestException, ImmutableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(dbLb.getId());

        if (dbLoadBalancer.getRateLimit() == null) {
            LOG.debug(String.format("Rate limit does not exist for loadbalancer %d.", dbLb.getId()));
            throw new EntityNotFoundException("No rate limit to update.");
        }

        if (isRateLimitValid(dbLb)) {
            LOG.debug(String.format("Rate limit is invalid, expirationTime cannot be set prior to the current time."));
            throw new BadRequestException("Must provide valid expiration time. cannot be set prior to the current time.");
        }

        RateLimit queueRateLimit = dbLb.getRateLimit();
        RateLimit dbRateLimit = dbLoadBalancer.getRateLimit();
        RateLimit newRateLimit = new RateLimit();

        if(queueRateLimit.getExpirationTime() != null) newRateLimit.setExpirationTime(queueRateLimit.getExpirationTime());
        else newRateLimit.setExpirationTime(dbRateLimit.getExpirationTime());

        if(queueRateLimit.getMaxRequestsPerSecond() != null) newRateLimit.setMaxRequestsPerSecond(queueRateLimit.getMaxRequestsPerSecond());
        else newRateLimit.setMaxRequestsPerSecond(dbRateLimit.getMaxRequestsPerSecond());

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
        
        dbLb.setRateLimit(newRateLimit);
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Updating the loadbalancer and rate limit in the database...");
        loadBalancerRepository.updateRateLimit(dbLoadBalancer, dbLb.getRateLimit());

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void delete(LoadBalancer dbLb) throws EntityNotFoundException, UnprocessableEntityException, ImmutableEntityException {
         LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(dbLb.getId());

        if (dbLoadBalancer.getRateLimit() == null) {
              LOG.debug(String.format("Rate limit does not exist for loadbalancer %d.", dbLb.getId()));
            throw new EntityNotFoundException("No rate limit to delete.");
        }

        LOG.debug("Updating the lb status to pending_update");
        if(!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional
    public void pseudoDelete(LoadBalancer requestLb) throws EntityNotFoundException {
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());
        loadBalancerRepository.removeRateLimit(dbLb);
    }

    @Override
    @Transactional
    public void removeLimitByExpiration(int id) {
        loadBalancerRepository.removeRateLimitByExpiration(id);
    }

    protected boolean isRateLimitValid(LoadBalancer lb) throws UnauthorizedException, EntityNotFoundException {
        RateLimit rl = lb.getRateLimit();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -6);
        return rl.getExpirationTime().before(cal);
    }
}