package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.entity.SessionPersistence;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.SessionPersistenceRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(Enclosed.class)
public class SessionPersistenceServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenAddingSessionPersistence extends Base {
        @Autowired
        private SessionPersistenceService sessionPersistenceService;

        @Autowired
        private SessionPersistenceRepository sessionPersistenceRepository;

        private SessionPersistence sessionPersistence;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            sessionPersistence = StubFactory.createHydratedDomainSessionPersistence();
        }

        @Test
        public void shouldAssignIdAndValuesWhenUpdateSucceeds() throws PersistenceServiceException {
            SessionPersistence dbSessionPersistence = sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            Assert.assertNotNull(dbSessionPersistence.getId());
            Assert.assertEquals(loadBalancer.getId(), dbSessionPersistence.getLoadBalancer().getId());
            Assert.assertEquals(sessionPersistence.getPersistenceType(), dbSessionPersistence.getPersistenceType());
        }

        @Test
        public void shouldUseDefaultValuesWhenSessionPersistenceIsEmpty() throws PersistenceServiceException {
            sessionPersistence = new SessionPersistence();
            sessionPersistence = sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);

            SessionPersistence defaultMonitor = new SessionPersistence();
            Assert.assertEquals(defaultMonitor.getPersistenceType(), sessionPersistence.getPersistenceType());
        }

        @Test
        public void shouldPutLbInPendingUpdateStatusWhenCreateSucceeds() throws Exception {
            sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test
        public void shouldRetrieveSessionPersistenceById() throws PersistenceServiceException {
            sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            Assert.assertNotNull(sessionPersistenceRepository.getByLoadBalancerId(loadBalancer.getId()));
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrievingByWrongLoadBalancerId() throws PersistenceServiceException {
            sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            sessionPersistenceRepository.getByLoadBalancerId(-99999);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenUpdatingWithWrongLoadBalancerId() throws PersistenceServiceException {
            sessionPersistenceService.update(-99999, sessionPersistence);
        }

        @Test(expected = NullPointerException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullSessionPersistence() throws PersistenceServiceException {
            sessionPersistenceService.update(loadBalancer.getId(), null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullLoadBalancerId() throws PersistenceServiceException {
            sessionPersistenceService.update(null, sessionPersistence);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenPreDeletingSessionPersistence extends Base {
        @Autowired
        private SessionPersistenceService sessionPersistenceService;

        private SessionPersistence sessionPersistence;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            sessionPersistence = StubFactory.createHydratedDomainSessionPersistence();
        }

        @Test
        public void shouldSetStatusToPendingUpdateWhenOperationSucceeds() throws PersistenceServiceException {
            sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            sessionPersistenceService.preDelete(loadBalancer.getId());
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenSessionPersistenceDoesNotExist() throws PersistenceServiceException {
            sessionPersistenceService.preDelete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenLoadBalancerDoesNotExist() throws PersistenceServiceException {
            sessionPersistenceService.preDelete(-99999);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenDeletingSessionPersistence extends Base {
        @Autowired
        private SessionPersistenceService sessionPersistenceService;

        @Autowired
        private SessionPersistenceRepository sessionPersistenceRepository;

        private SessionPersistence sessionPersistence;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            sessionPersistence = StubFactory.createHydratedDomainSessionPersistence();
            sessionPersistence = sessionPersistenceService.update(loadBalancer.getId(), sessionPersistence);
            sessionPersistenceService.preDelete(loadBalancer.getId());
            sessionPersistenceService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldNotExistWhenDeleteSucceeds() throws PersistenceServiceException {
            sessionPersistence = sessionPersistenceRepository.getByLoadBalancerId(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenSessionPersistenceDoesntExist() throws PersistenceServiceException {
            sessionPersistenceService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenDeletingFromNonExistentLoadBalancer() throws PersistenceServiceException {
            sessionPersistenceService.delete(-99999);
        }
    }
}
