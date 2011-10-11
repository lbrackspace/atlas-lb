package org.openstack.atlas.service.domain.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.datamodel.CorePersistenceType;
import org.openstack.atlas.datamodel.CoreProtocolType;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.BadRequestException;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.ImmutableEntityException;
import org.openstack.atlas.service.domain.exception.UnprocessableEntityException;
import org.openstack.atlas.service.domain.repository.LoadBalancerRepository;
import org.openstack.atlas.service.domain.repository.SessionPersistenceRepository;
import org.openstack.atlas.service.domain.service.SessionPersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionPersistenceServiceImpl implements SessionPersistenceService {
    private final Log LOG = LogFactory.getLog(SessionPersistenceServiceImpl.class);

    @Autowired
    protected LoadBalancerRepository loadBalancerRepository;
    @Autowired
    protected SessionPersistenceRepository sessionPersistenceRepository;

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class, ImmutableEntityException.class, UnprocessableEntityException.class})
    public SessionPersistence update(Integer loadBalancerId, SessionPersistence sessionPersistence) throws EntityNotFoundException, ImmutableEntityException, UnprocessableEntityException, BadRequestException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        SessionPersistence dbSessionPersistence = dbLoadBalancer.getSessionPersistence();
        SessionPersistence sessionPersistenceToUpdate = dbSessionPersistence == null ? sessionPersistence : dbSessionPersistence;
        sessionPersistenceToUpdate.setLoadBalancer(dbLoadBalancer); // Needs to be set for hibernate

        verifyProtocol(sessionPersistence, dbLoadBalancer);
        setPropertiesForUpdate(sessionPersistence, dbLoadBalancer.getSessionPersistence(), sessionPersistenceToUpdate);

        loadBalancerRepository.changeStatus(dbLoadBalancer.getAccountId(), dbLoadBalancer.getId(), CoreLoadBalancerStatus.PENDING_UPDATE, false);
        dbLoadBalancer.setSessionPersistence(sessionPersistenceToUpdate);
        dbLoadBalancer = loadBalancerRepository.update(dbLoadBalancer);
        return dbLoadBalancer.getSessionPersistence();
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void preDelete(Integer loadBalancerId) throws EntityNotFoundException {
        LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancerId);
        if (dbLoadBalancer.getSessionPersistence() == null) throw new EntityNotFoundException("Session persistence not found");
    }

    @Override
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public void delete(Integer loadBalancerId) throws EntityNotFoundException {
        sessionPersistenceRepository.delete(sessionPersistenceRepository.getByLoadBalancerId(loadBalancerId));
    }

    protected void verifyProtocol(final SessionPersistence sessionPersistence, final LoadBalancer dbLoadBalancer) throws UnprocessableEntityException {
        if (!(dbLoadBalancer.getProtocol().equals(CoreProtocolType.HTTP) || dbLoadBalancer.getProtocol().equals(CoreProtocolType.HTTPS))
                && sessionPersistence.getPersistenceType().equals(CorePersistenceType.HTTP_COOKIE)) {
            throw new UnprocessableEntityException("HTTP_COOKIE session persistence can only be enabled with the HTTP/HTTPS protocol");
        }
    }

    protected void setPropertiesForUpdate(final SessionPersistence requestPersistence, final SessionPersistence dbPersistence, SessionPersistence persistenceToUpdate) throws BadRequestException {
        if (requestPersistence.getPersistenceType() != null) persistenceToUpdate.setPersistenceType(requestPersistence.getPersistenceType());
        else if (dbPersistence != null) persistenceToUpdate.setPersistenceType(dbPersistence.getPersistenceType());
        else throw new BadRequestException("Must provide a persistence type for the request");
    }
}
