package org.openstack.atlas.service.domain.services.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus;
import org.openstack.atlas.service.domain.entities.SessionPersistence;
import org.openstack.atlas.service.domain.exceptions.*;
import org.openstack.atlas.service.domain.services.LoadBalancerStatusHistoryService;
import org.openstack.atlas.service.domain.services.SessionPersistenceService;
import org.openstack.atlas.service.domain.services.helpers.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;

import static org.openstack.atlas.service.domain.entities.LoadBalancerProtocol.*;
import static org.openstack.atlas.service.domain.entities.SessionPersistence.*;

@Service
public class SessionPersistenceServiceImpl extends BaseService implements SessionPersistenceService {
    private final Log LOG = LogFactory.getLog(SessionPersistenceServiceImpl.class);

    @Autowired
    private LoadBalancerStatusHistoryService loadBalancerStatusHistoryService;

    @Override
    public SessionPersistence get(Integer accountId, Integer lbId) throws EntityNotFoundException, BadRequestException, DeletedStatusException {
        return loadBalancerRepository.getSessionPersistenceByAccountIdLoadBalancerId(accountId, lbId);
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, BadRequestException.class})
    public void update(LoadBalancer queueLb) throws EntityNotFoundException, ImmutableEntityException, BadRequestException, UnprocessableEntityException {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getByIdAndAccountId(queueLb.getId(), queueLb.getAccountId());

        validateSessionPersistenceProtocolCompatibility(queueLb, dbLoadBalancer);

        LOG.debug("Updating the lb status to pending_update");
        if (!loadBalancerRepository.testAndSetStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLoadBalancer);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }

        dbLoadBalancer.setSessionPersistence(queueLb.getSessionPersistence());
        loadBalancerRepository.update(dbLoadBalancer);

        LOG.debug("Leaving " + getClass());
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public void delete(LoadBalancer requestLb) throws Exception {
        LOG.debug("Entering " + getClass());
        LoadBalancer dbLb = loadBalancerRepository.getByIdAndAccountId(requestLb.getId(), requestLb.getAccountId());

        if (dbLb.getSessionPersistence().equals(SessionPersistence.NONE)) {
            throw new UnprocessableEntityException("Session persistence is already deleted.");
        }

        if (!loadBalancerRepository.testAndSetStatus(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE, false)) {
            String message = StringHelper.immutableLoadBalancer(dbLb);
            LOG.warn(message);
            throw new ImmutableEntityException(message);
        } else {
            //Set status record
            loadBalancerStatusHistoryService.save(dbLb.getAccountId(), dbLb.getId(), LoadBalancerStatus.PENDING_UPDATE);
        }
    }

    public void validateSessionPersistenceProtocolCompatibility(LoadBalancer inLb, LoadBalancer dbLb) throws BadRequestException, UnprocessableEntityException {
        SessionPersistence inpersist = inLb.getSessionPersistence();
        LoadBalancerProtocol dbProtocol = dbLb.getProtocol();

        String httpErrMsg = "HTTP_COOKIE Session persistence is only valid with HTTP and HTTP(SSL Termination) protocols. ";
        String sipErrMsg = "SOURCE_IP Session persistence is only valid with non-HTTP protocols. ";

        LOG.info("Verifying session persistence protocol..." + inpersist);
        if (inpersist != NONE) {
            if (inpersist == HTTP_COOKIE &&
                    (dbProtocol != HTTP)) {
                LOG.info(httpErrMsg);
                throw new BadRequestException(httpErrMsg);
            }

            if (inpersist == SOURCE_IP &&
                    (dbProtocol == HTTP)) {
                LOG.info(httpErrMsg);
                throw new BadRequestException(sipErrMsg);
            }
        }
        LOG.info("Successfully verified session persistence protocol..." + inLb.getSessionPersistence());
    }
}

