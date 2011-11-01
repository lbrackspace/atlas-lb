package org.openstack.atlas.service.domain.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.datamodel.CoreLoadBalancerStatus;
import org.openstack.atlas.service.domain.entity.ConnectionThrottle;
import org.openstack.atlas.service.domain.entity.LoadBalancer;
import org.openstack.atlas.service.domain.exception.EntityNotFoundException;
import org.openstack.atlas.service.domain.exception.PersistenceServiceException;
import org.openstack.atlas.service.domain.repository.ConnectionThrottleRepository;
import org.openstack.atlas.service.domain.stub.StubFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(Enclosed.class)
public class ConnectionThrottleServiceITest {

    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenAddingSessionPersistence extends Base {
        @Autowired
        private ConnectionThrottleService connectionThrottleService;

        @Autowired
        private ConnectionThrottleRepository connectionThrottleRepository;

        private ConnectionThrottle connectionThrottle;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            connectionThrottle = StubFactory.createHydratedDomainConnectionThrottle();
        }

        @Test
        public void shouldAssignIdAndValuesWhenUpdateSucceeds() throws PersistenceServiceException {
            ConnectionThrottle dbConnectionThrottle = connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            Assert.assertNotNull(dbConnectionThrottle.getId());
            Assert.assertEquals(loadBalancer.getId(), dbConnectionThrottle.getLoadBalancer().getId());
            Assert.assertEquals(connectionThrottle.getMaxRequestRate(), dbConnectionThrottle.getMaxRequestRate());
            Assert.assertEquals(connectionThrottle.getRateInterval(), dbConnectionThrottle.getRateInterval());
        }

        @Test
        public void shouldUseDefaultValuesWhenConnectionThrottleIsEmpty() throws PersistenceServiceException {
            connectionThrottle = new ConnectionThrottle();
            connectionThrottle = connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);

            ConnectionThrottle defaultThrottle = new ConnectionThrottle();
            Assert.assertEquals(defaultThrottle.getMaxRequestRate(), connectionThrottle.getMaxRequestRate());
            Assert.assertEquals(defaultThrottle.getRateInterval(), connectionThrottle.getRateInterval());
        }

        @Test
        public void shouldPutLbInPendingUpdateStatusWhenCreateSucceeds() throws Exception {
            connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test
        public void shouldRetrieveConnectionThrottleById() throws PersistenceServiceException {
            connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            Assert.assertNotNull(connectionThrottleRepository.getByLoadBalancerId(loadBalancer.getId()));
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenRetrievingByWrongLoadBalancerId() throws PersistenceServiceException {
            connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            connectionThrottleRepository.getByLoadBalancerId(-99999);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenUpdatingWithWrongLoadBalancerId() throws PersistenceServiceException {
            connectionThrottleService.update(-99999, connectionThrottle);
        }

        @Test(expected = NullPointerException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullConnectionThrottle() throws PersistenceServiceException {
            connectionThrottleService.update(loadBalancer.getId(), null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowExceptionWhenUpdatingWithNullLoadBalancerId() throws PersistenceServiceException {
            connectionThrottleService.update(null, connectionThrottle);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenPreDeletingAConnectionThrottle extends Base {
        @Autowired
        private ConnectionThrottleService connectionThrottleService;

        private ConnectionThrottle connectionThrottle;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            connectionThrottle = StubFactory.createHydratedDomainConnectionThrottle();
        }

        @Test
        public void shouldSetStatusToPendingUpdateWhenOperationSucceeds() throws PersistenceServiceException {
            connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            connectionThrottleService.preDelete(loadBalancer.getId());
            LoadBalancer dbLoadBalancer = loadBalancerRepository.getById(loadBalancer.getId());
            Assert.assertEquals(dbLoadBalancer.getStatus(), CoreLoadBalancerStatus.PENDING_UPDATE);
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenConnectionThrottleDoesNotExist() throws PersistenceServiceException {
            connectionThrottleService.preDelete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenLoadBalancerDoesNotExist() throws PersistenceServiceException {
            connectionThrottleService.preDelete(-99999);
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    public static class WhenDeletingAConnectionThrottle extends Base {
        @Autowired
        private ConnectionThrottleService connectionThrottleService;

        @Autowired
        private ConnectionThrottleRepository connectionThrottleRepository;

        private ConnectionThrottle connectionThrottle;

        @Before
        public void setUp() throws PersistenceServiceException {
            loadBalancer = loadBalancerService.create(loadBalancer);
            loadBalancerRepository.changeStatus(loadBalancer, CoreLoadBalancerStatus.ACTIVE);
            connectionThrottle = StubFactory.createHydratedDomainConnectionThrottle();
            connectionThrottle = connectionThrottleService.update(loadBalancer.getId(), connectionThrottle);
            connectionThrottleService.preDelete(loadBalancer.getId());
            connectionThrottleService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldNotExistWhenDeleteSucceeds() throws PersistenceServiceException {
            connectionThrottle = connectionThrottleRepository.getByLoadBalancerId(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenConnectionThrottleDoesntExist() throws PersistenceServiceException {
            connectionThrottleService.delete(loadBalancer.getId());
        }

        @Test(expected = EntityNotFoundException.class)
        public void shouldThrowExceptionWhenDeletingFromNonExistentLoadBalancer() throws PersistenceServiceException {
            connectionThrottleService.delete(-99999);
        }
    }
}
